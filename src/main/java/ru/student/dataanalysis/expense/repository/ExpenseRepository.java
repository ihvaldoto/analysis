package ru.student.dataanalysis.expense.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import ru.student.dataanalysis.expense.entity.Expense;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends CrudRepository<Expense, Long> {

    /**
     * Находит траты за период (включительно) с суммой не меньше заданной.
     * Запрос строится по имени метода (Query Lookup Strategies).
     *
     * @param from      начало периода
     * @param to        конец периода
     * @param minAmount минимальная сумма траты
     */
    List<Expense> findBySpentAtBetweenAndAmountGreaterThanEqual(LocalDate from, LocalDate to, BigDecimal minAmount);

    /**
     * Находит траты по названию связанной категории (JPQL).
     *
     * @param categoryName название категории
     */
    @Query("SELECT e FROM Expense e WHERE e.category.name = :categoryName")
    List<Expense> findByCategoryName(@Param("categoryName") String categoryName);

    /**
     * Находит траты, привязанные к счёту.
     */
    List<Expense> findByAccountId(Long accountId);
}
