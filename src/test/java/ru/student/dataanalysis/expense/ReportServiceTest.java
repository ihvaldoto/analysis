package ru.student.dataanalysis.expense;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.student.dataanalysis.expense.entity.Account;
import ru.student.dataanalysis.expense.entity.AppUser;
import ru.student.dataanalysis.expense.entity.Category;
import ru.student.dataanalysis.expense.entity.Expense;
import ru.student.dataanalysis.expense.entity.Report;
import ru.student.dataanalysis.expense.entity.ReportStatus;
import ru.student.dataanalysis.expense.exception.ResourceNotFoundException;
import ru.student.dataanalysis.expense.repository.AccountRepository;
import ru.student.dataanalysis.expense.repository.AppUserRepository;
import ru.student.dataanalysis.expense.repository.CategoryRepository;
import ru.student.dataanalysis.expense.repository.ExpenseRepository;
import ru.student.dataanalysis.expense.service.ReportService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@SpringBootTest
class ReportServiceTest {

    @Autowired
    private ReportService reportService;
    @Autowired
    private AppUserRepository userRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ExpenseRepository expenseRepository;

    private void saveExpense(String description) {
        AppUser user = new AppUser();
        user.setLogin(UUID.randomUUID().toString());
        user.setDisplayName("Тестовый пользователь");
        userRepository.save(user);

        Account account = new Account();
        account.setName("Карта");
        account.setCurrency("RUB");
        account.setOwner(user);
        accountRepository.save(account);

        Category category = new Category();
        category.setName("Еда");
        category.setOwner(user);
        categoryRepository.save(category);

        Expense expense = new Expense();
        expense.setAccount(account);
        expense.setCategory(category);
        expense.setAmount(new BigDecimal("123.45"));
        expense.setDescription(description);
        expense.setSpentAt(LocalDate.of(2034, 1, 2));
        expenseRepository.save(expense);
    }

    @Test
    void testCreateReportHasCreatedStatusAndNoContent() {
        Long id = reportService.createReport();

        Report report = reportService.getReport(id);
        Assertions.assertEquals(ReportStatus.CREATED, report.getStatus());
        Assertions.assertNull(report.getContent());
    }

    @Test
    void testGenerateReportCompletesWithAllRequiredData() {
        String description = UUID.randomUUID().toString();
        saveExpense(description);
        Long id = reportService.createReport();

        reportService.generateReportAsync(id).join();

        Report report = reportService.getReport(id);
        Assertions.assertEquals(ReportStatus.COMPLETED, report.getStatus());
        String html = report.getContent();
        Assertions.assertTrue(html.contains("<table"), "нет таблицы");
        Assertions.assertTrue(html.contains("Зарегистрировано пользователей: " + userRepository.count()));
        Assertions.assertTrue(html.contains(description), "нет траты в списке");
        Assertions.assertTrue(html.contains("Время подсчёта пользователей: "));
        Assertions.assertTrue(html.contains("Время получения списка трат: "));
        Assertions.assertTrue(html.contains("Общее время формирования отчёта: "));
    }

    @Test
    void testReportEscapesHtmlInData() {
        saveExpense("<script>alert(1)</script>");
        Long id = reportService.createReport();

        reportService.generateReportAsync(id).join();

        String html = reportService.getReport(id).getContent();
        Assertions.assertFalse(html.contains("<script>alert(1)</script>"));
        Assertions.assertTrue(html.contains("&lt;script&gt;"));
    }

    @Test
    void testGetUnknownReportFails() {
        Assertions.assertThrows(ResourceNotFoundException.class, () -> reportService.getReport(-1L));
    }
}
