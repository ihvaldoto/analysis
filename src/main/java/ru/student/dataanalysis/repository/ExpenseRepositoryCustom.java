package ru.student.dataanalysis.repository;

import ru.student.dataanalysis.model.Expense;

import java.math.BigDecimal;
import java.util.List;

/**
 * Кастомные методы доступа к тратам, реализованные через Criteria API.
 */
public interface ExpenseRepositoryCustom {

    /**
     * Аналог findByUserIdAndAmountBetween, но на Criteria API.
     */
    List<Expense> searchByUserIdAndAmountBetween(Long userId, BigDecimal min, BigDecimal max);

    /**
     * Аналог findByCategoryName (поиск через связанную сущность), но на Criteria API.
     */
    List<Expense> searchByCategoryName(String categoryName);
}
