package ru.student.dataanalysis.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import ru.student.dataanalysis.model.Expense;

import java.math.BigDecimal;
import java.util.List;

public interface ExpenseRepository extends CrudRepository<Expense, Long>, ExpenseRepositoryCustom {

    /**
     * Query Method: поиск трат пользователя в заданном диапазоне суммы.
     * Использует ключевые слова And и Between.
     */
    List<Expense> findByUserIdAndAmountBetween(Long userId, BigDecimal min, BigDecimal max);

    /**
     * JPQL-запрос через связанную сущность Category.
     */
    @Query("SELECT e FROM Expense e WHERE e.category.name = :categoryName")
    List<Expense> findByCategoryName(@Param("categoryName") String categoryName);
}
