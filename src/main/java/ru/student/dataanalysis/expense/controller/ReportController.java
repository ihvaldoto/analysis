package ru.student.dataanalysis.expense.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.student.dataanalysis.expense.entity.Report;
import ru.student.dataanalysis.expense.exception.ApiError;
import ru.student.dataanalysis.expense.service.ReportService;

/**
 * REST для отчётов: создать и запустить формирование, получить результат.
 */
@RestController
@RequestMapping("/custom/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Создаёт отчёт, запускает формирование и сразу возвращает его id, не дожидаясь результата.
     */
    @PostMapping
    public ResponseEntity<Long> createReport() {
        Long id = reportService.createReport();
        reportService.generateReportAsync(id);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(id);
    }

    /**
     * Возвращает HTML готового отчёта. Если отчёт ещё формируется или завершился ошибкой,
     * возвращает JSON с описанием состояния.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getReport(@PathVariable("id") Long id) {
        Report report = reportService.getReport(id);
        switch (report.getStatus()) {
            case COMPLETED:
                return ResponseEntity.ok()
                        .contentType(new MediaType(MediaType.TEXT_HTML, java.nio.charset.StandardCharsets.UTF_8))
                        .body(report.getContent());
            case ERROR:
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(ApiError.create("Отчёт завершился с ошибкой: " + report.getContent()));
            default:
                return ResponseEntity.status(HttpStatus.ACCEPTED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(ApiError.create("Отчёт ещё формируется"));
        }
    }
}
