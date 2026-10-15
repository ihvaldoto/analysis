package ru.student.dataanalysis.expense.service;

import ru.student.dataanalysis.expense.entity.Expense;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface ExpenseTrackingService {

    /**
     * Записывает трату и списывает её сумму со счёта в одной транзакции.
     * Если на счёте не хватает средств, трата не сохраняется и баланс не меняется.
     *
     * @param accountId   идентификатор счёта
     * @param categoryId  идентификатор категории
     * @param amount      сумма траты (положительная)
     * @param description описание
     * @param spentAt     дата траты
     * @return сохранённая трата
     */
    Expense registerExpense(Long accountId, Long categoryId, BigDecimal amount, String description, LocalDate spentAt);
}
