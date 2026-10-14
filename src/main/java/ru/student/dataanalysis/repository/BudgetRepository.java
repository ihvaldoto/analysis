package ru.student.dataanalysis.repository;

import org.springframework.data.repository.CrudRepository;
import ru.student.dataanalysis.model.Budget;

import java.util.List;

public interface BudgetRepository extends CrudRepository<Budget, Long> {

    List<Budget> findByCategoryId(Long categoryId);
}
