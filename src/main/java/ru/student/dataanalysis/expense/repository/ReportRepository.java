package ru.student.dataanalysis.expense.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.student.dataanalysis.expense.entity.Report;

/**
 * Не публикуется через Spring Data REST: отчёты отдаёт ReportController.
 */
@RepositoryRestResource(exported = false)
public interface ReportRepository extends CrudRepository<Report, Long> {
}
