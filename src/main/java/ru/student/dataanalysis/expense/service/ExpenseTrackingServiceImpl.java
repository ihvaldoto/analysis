package ru.student.dataanalysis.expense.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import ru.student.dataanalysis.expense.entity.Account;
import ru.student.dataanalysis.expense.entity.Category;
import ru.student.dataanalysis.expense.entity.Expense;
import ru.student.dataanalysis.expense.repository.AccountRepository;
import ru.student.dataanalysis.expense.repository.CategoryRepository;
import ru.student.dataanalysis.expense.repository.ExpenseRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class ExpenseTrackingServiceImpl implements ExpenseTrackingService {

    private final ExpenseRepository expenseRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final PlatformTransactionManager transactionManager;

    public ExpenseTrackingServiceImpl(ExpenseRepository expenseRepository,
                                      AccountRepository accountRepository,
                                      CategoryRepository categoryRepository,
                                      PlatformTransactionManager transactionManager) {
        this.expenseRepository = expenseRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.transactionManager = transactionManager;
    }

    @Override
    public Expense registerExpense(Long accountId, Long categoryId, BigDecimal amount,
                                   String description, LocalDate spentAt) {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new IllegalArgumentException("Счёт не найден: " + accountId));
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Категория не найдена: " + categoryId));

            Expense expense = new Expense();
            expense.setAccount(account);
            expense.setCategory(category);
            expense.setAmount(amount);
            expense.setDescription(description);
            expense.setSpentAt(spentAt);
            Expense saved = expenseRepository.save(expense);

            // Проверка после сохранения: при нехватке средств откатывается и сама трата
            BigDecimal newBalance = account.getBalance().subtract(amount);
            if (newBalance.signum() < 0) {
                throw new IllegalStateException("Недостаточно средств на счёте " + account.getName());
            }
            account.setBalance(newBalance);
            accountRepository.save(account);

            transactionManager.commit(status);
            return saved;
        } catch (RuntimeException ex) {
            transactionManager.rollback(status);
            throw ex;
        }
    }
}
