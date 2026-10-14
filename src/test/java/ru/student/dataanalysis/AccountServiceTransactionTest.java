package ru.student.dataanalysis;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.student.dataanalysis.model.Account;
import ru.student.dataanalysis.model.Category;
import ru.student.dataanalysis.model.Expense;
import ru.student.dataanalysis.model.User;
import ru.student.dataanalysis.repository.AccountRepository;
import ru.student.dataanalysis.repository.CategoryRepository;
import ru.student.dataanalysis.repository.ExpenseRepository;
import ru.student.dataanalysis.repository.UserRepository;
import ru.student.dataanalysis.service.AccountService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Тесты транзакционного метода createAccountWithExpense:
 * положительный сценарий и откат транзакции при ошибке.
 */
@SpringBootTest
class AccountServiceTransactionTest {

    private final AccountService accountService;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;

    @Autowired
    AccountServiceTransactionTest(AccountService accountService,
                                  UserRepository userRepository,
                                  AccountRepository accountRepository,
                                  CategoryRepository categoryRepository,
                                  ExpenseRepository expenseRepository) {
        this.accountService = accountService;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.expenseRepository = expenseRepository;
    }

    /**
     * Положительный сценарий: корректная трата — счёт и трата сохраняются.
     */
    @Test
    void testCreateAccountWithExpenseSuccess() {
        User user = userRepository.save(new User(rnd(), "hash", "Тест"));
        Category category = categoryRepository.save(new Category(rnd(), "desc", new BigDecimal("5000"), user));

        String accountName = rnd();
        Account account = new Account(accountName, new BigDecimal("1000"), "RUB", user);
        Expense expense = new Expense(new BigDecimal("300"), LocalDateTime.now(), "покупка",
                user, null, category);

        accountService.createAccountWithExpense(account, expense);

        List<Account> saved = accountRepository.findByUserId(user.getId());
        Assertions.assertTrue(saved.stream().anyMatch(a -> accountName.equals(a.getName())));
        Assertions.assertEquals(1, expenseRepository.findByCategoryName(category.getName()).size());
    }

    /**
     * Отрицательный сценарий: некорректная сумма траты — транзакция откатывается,
     * счёт в базе не появляется.
     */
    @Test
    void testCreateAccountWithExpenseRollback() {
        User user = userRepository.save(new User(rnd(), "hash", "Тест"));
        Category category = categoryRepository.save(new Category(rnd(), "desc", new BigDecimal("5000"), user));

        String accountName = rnd();
        Account account = new Account(accountName, new BigDecimal("1000"), "RUB", user);
        Expense invalid = new Expense(BigDecimal.ZERO, LocalDateTime.now(), "ошибка",
                user, null, category);

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> accountService.createAccountWithExpense(account, invalid));

        List<Account> saved = accountRepository.findByUserId(user.getId());
        Assertions.assertTrue(saved.stream().noneMatch(a -> accountName.equals(a.getName())),
                "Счёт не должен сохраниться после отката транзакции");
    }

    private static String rnd() {
        return UUID.randomUUID().toString();
    }
}
