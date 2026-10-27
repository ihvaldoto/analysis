package ru.student.dataanalysis.expense.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.student.dataanalysis.expense.entity.Budget;

@RepositoryRestResource(path = "budgets")
public interface BudgetRepository extends CrudRepository<Budget, Long> {
}
