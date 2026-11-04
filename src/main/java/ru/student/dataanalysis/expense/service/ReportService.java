package ru.student.dataanalysis.expense.service;

import ru.student.dataanalysis.expense.entity.Report;

import java.util.concurrent.CompletableFuture;

public interface ReportService {

    /**
     * Создаёт отчёт в статусе «создан».
     *
     * @return идентификатор отчёта
     */
    Long createReport();

    /**
     * Возвращает отчёт (статус и содержимое).
     *
     * @param id идентификатор отчёта
     * @throws ru.student.dataanalysis.expense.exception.ResourceNotFoundException если отчёта нет
     */
    Report getReport(Long id);

    /**
     * Запускает формирование отчёта асинхронно и сразу возвращает управление.
     * Количество пользователей и список трат считаются в двух отдельных потоках.
     * По окончании статус отчёта становится «завершён» или «ошибка».
     *
     * @param id идентификатор отчёта
     * @return future, завершающийся после сохранения результата
     */
    CompletableFuture<Void> generateReportAsync(Long id);
}
