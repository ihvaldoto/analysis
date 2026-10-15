package ru.student.dataanalysis.expense.dao;

import ru.student.dataanalysis.expense.entity.Expense;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Те же запросы, что и в ExpenseRepository, но на Criteria API.
 */
public interface ExpenseCriteriaRepository {

    /**
     * Находит траты за период (включительно) с суммой не меньше заданной.
     *
     * @param from      начало периода
     * @param to        конец периода
     * @param minAmount минимальная сумма траты
     */
    List<Expense> findByPeriodAndMinAmount(LocalDate from, LocalDate to, BigDecimal minAmount);

    /**
     * Находит траты по названию связанной категории.
     *
     * @param categoryName название категории
     */
    List<Expense> findByCategoryName(String categoryName);
}
