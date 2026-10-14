package ru.student.dataanalysis.repository;

import org.springframework.data.repository.CrudRepository;
import ru.student.dataanalysis.model.Category;

import java.util.List;

public interface CategoryRepository extends CrudRepository<Category, Long> {

    List<Category> findByUserId(Long userId);
}
