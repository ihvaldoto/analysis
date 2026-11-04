package ru.student.dataanalysis.expense.service;

import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import ru.student.dataanalysis.expense.entity.Expense;
import ru.student.dataanalysis.expense.entity.Report;
import ru.student.dataanalysis.expense.entity.ReportStatus;
import ru.student.dataanalysis.expense.exception.ResourceNotFoundException;
import ru.student.dataanalysis.expense.repository.AppUserRepository;
import ru.student.dataanalysis.expense.repository.ExpenseRepository;
import ru.student.dataanalysis.expense.repository.ReportRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Service
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final AppUserRepository userRepository;
    private final ExpenseRepository expenseRepository;

    public ReportServiceImpl(ReportRepository reportRepository,
                             AppUserRepository userRepository,
                             ExpenseRepository expenseRepository) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.expenseRepository = expenseRepository;
    }

    @Override
    public Long createReport() {
        Report report = new Report();
        report.setStatus(ReportStatus.CREATED);
        return reportRepository.save(report).getId();
    }

    @Override
    public Report getReport(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Отчёт не найден: " + id));
    }

    @Override
    public CompletableFuture<Void> generateReportAsync(Long id) {
        return CompletableFuture
                .supplyAsync(this::buildReportHtml)
                .handle((html, error) -> {
                    finishReport(id, html, error);
                    return null;
                });
    }

    /**
     * Сохраняет результат: «завершён» с HTML или «ошибка» с текстом ошибки.
     */
    private void finishReport(Long id, String html, Throwable error) {
        Report report = getReport(id);
        if (error == null) {
            report.setStatus(ReportStatus.COMPLETED);
            report.setContent(html);
        } else {
            Throwable cause = error.getCause() != null ? error.getCause() : error;
            report.setStatus(ReportStatus.ERROR);
            report.setContent(String.valueOf(cause.getMessage()));
        }
        reportRepository.save(report);
    }

    /**
     * Считает пользователей и получает список трат в двух отдельных потоках, ждёт их через join()
     * и собирает HTML с затраченным временем.
     */
    private String buildReportHtml() {
        long startTime = System.currentTimeMillis();

        TimedTask<Long> usersTask = new TimedTask<>("report-users-count", userRepository::count);
        TimedTask<List<Expense>> expensesTask = new TimedTask<>("report-expenses-list", () -> {
            List<Expense> expenses = new ArrayList<>();
            expenseRepository.findAll().forEach(expenses::add);
            return expenses;
        });

        usersTask.start();
        expensesTask.start();
        usersTask.join();
        expensesTask.join();

        long usersCount = usersTask.getResult();
        List<Expense> expenses = expensesTask.getResult();
        String expensesTable = renderExpensesTable(expenses);

        long totalElapsed = System.currentTimeMillis() - startTime;
        return renderPage(usersCount, usersTask.getElapsedMs(), expensesTable,
                expensesTask.getElapsedMs(), totalElapsed);
    }

    private static String renderExpensesTable(List<Expense> expenses) {
        StringBuilder table = new StringBuilder();
        table.append("<table border=\"1\">\n<tr><th>ID</th><th>Дата</th><th>Сумма</th>")
                .append("<th>Описание</th><th>Счёт</th><th>Категория</th></tr>\n");
        for (Expense expense : expenses) {
            table.append("<tr><td>").append(expense.getId())
                    .append("</td><td>").append(expense.getSpentAt())
                    .append("</td><td>").append(expense.getAmount())
                    .append("</td><td>").append(escape(expense.getDescription()))
                    .append("</td><td>").append(escape(expense.getAccount().getName()))
                    .append("</td><td>").append(escape(expense.getCategory().getName()))
                    .append("</td></tr>\n");
        }
        return table.append("</table>").toString();
    }

    private static String renderPage(long usersCount, long usersMs, String expensesTable,
                                     long expensesMs, long totalMs) {
        return "<!DOCTYPE html>\n<html>\n<head>\n<meta charset=\"UTF-8\">\n<title>Отчёт</title>\n</head>\n<body>\n"
                + "<h1>Отчёт по приложению</h1>\n"
                + "<h2>Пользователи</h2>\n"
                + "<p>Зарегистрировано пользователей: " + usersCount + "</p>\n"
                + "<p>Время подсчёта пользователей: " + usersMs + " мс</p>\n"
                + "<h2>Траты</h2>\n"
                + expensesTable + "\n"
                + "<p>Время получения списка трат: " + expensesMs + " мс</p>\n"
                + "<h2>Итого</h2>\n"
                + "<p>Общее время формирования отчёта: " + totalMs + " мс</p>\n"
                + "</body>\n</html>\n";
    }

    private static String escape(String text) {
        return text == null ? "" : HtmlUtils.htmlEscape(text);
    }

    /**
     * Задача в отдельном потоке Thread с замером времени. Ошибка потока не теряется:
     * она пробрасывается при чтении результата.
     */
    private static final class TimedTask<T> {

        private final Supplier<T> action;
        private final Thread thread;
        private volatile T result;
        private volatile long elapsedMs;
        private volatile Throwable error;

        TimedTask(String name, Supplier<T> action) {
            this.action = action;
            this.thread = new Thread(this::run, name);
        }

        void start() {
            thread.start();
        }

        void join() {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Формирование отчёта прервано", e);
            }
        }

        long getElapsedMs() {
            return elapsedMs;
        }

        T getResult() {
            if (error != null) {
                throw new IllegalStateException(
                        "Ошибка в потоке " + thread.getName() + ": " + error.getMessage(), error);
            }
            return result;
        }

        private void run() {
            long startTime = System.currentTimeMillis();
            try {
                result = action.get();
            } catch (Throwable t) {
                error = t;
            } finally {
                elapsedMs = System.currentTimeMillis() - startTime;
            }
        }
    }
}
