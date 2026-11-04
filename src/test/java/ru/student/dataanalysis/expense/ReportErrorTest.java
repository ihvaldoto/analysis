package ru.student.dataanalysis.expense;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.student.dataanalysis.expense.entity.Report;
import ru.student.dataanalysis.expense.entity.ReportStatus;
import ru.student.dataanalysis.expense.repository.ExpenseRepository;
import ru.student.dataanalysis.expense.service.ReportService;

/**
 * Отрицательный кейс: сбой в одном из потоков даёт статус «ошибка».
 */
@SpringBootTest
class ReportErrorTest {

    @Autowired
    private ReportService reportService;

    @MockitoBean
    private ExpenseRepository expenseRepository;

    @Test
    void testFailureInThreadMarksReportAsError() {
        Mockito.when(expenseRepository.findAll()).thenThrow(new IllegalStateException("база недоступна"));
        Long id = reportService.createReport();

        reportService.generateReportAsync(id).join();

        Report report = reportService.getReport(id);
        Assertions.assertEquals(ReportStatus.ERROR, report.getStatus());
        Assertions.assertTrue(report.getContent().contains("база недоступна"), report.getContent());
    }
}
