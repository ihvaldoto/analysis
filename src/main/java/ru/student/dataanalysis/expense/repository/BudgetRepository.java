package ru.student.dataanalysis.expense.repository;

import org.springframework.data.repository.CrudRepository;
import ru.student.dataanalysis.expense.entity.Budget;

public interface BudgetRepository extends CrudRepository<Budget, Long> {
}
