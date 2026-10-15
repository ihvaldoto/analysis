package ru.student.dataanalysis.expense.repository;

import org.springframework.data.repository.CrudRepository;
import ru.student.dataanalysis.expense.entity.Category;

public interface CategoryRepository extends CrudRepository<Category, Long> {
}
