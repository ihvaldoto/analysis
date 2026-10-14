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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Тесты кастомных методов репозитория, реализованных через Criteria API.
 * Аналогичны тестам в ExpenseRepositoryQueryTest.
 */
@SpringBootTest
class ExpenseCriteriaTest {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;

    @Autowired
    ExpenseCriteriaTest(UserRepository userRepository,
                        AccountRepository accountRepository,
                        CategoryRepository categoryRepository,
                        ExpenseRepository expenseRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.expenseRepository = expenseRepository;
    }

    @Test
    void testSearchByUserIdAndAmountBetween() {
        User user = userRepository.save(new User(rnd(), "hash", "Тест"));
        Account account = accountRepository.save(new Account("Карта", BigDecimal.TEN, "RUB", user));
        Category category = categoryRepository.save(new Category(rnd(), "desc", new BigDecimal("5000"), user));

        saveExpense(new BigDecimal("100"), user, account, category);
        Expense inRange = saveExpense(new BigDecimal("500"), user, account, category);
        saveExpense(new BigDecimal("1000"), user, account, category);

        List<Expense> result = expenseRepository
                .searchByUserIdAndAmountBetween(user.getId(), new BigDecimal("200"), new BigDecimal("800"));

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(inRange.getId(), result.get(0).getId());
    }

    @Test
    void testSearchByCategoryName() {
        User user = userRepository.save(new User(rnd(), "hash", "Тест"));
        Account account = accountRepository.save(new Account("Наличные", BigDecimal.TEN, "RUB", user));
        String categoryName = rnd();
        Category category = categoryRepository.save(new Category(categoryName, "desc", new BigDecimal("3000"), user));

        Expense expense = saveExpense(new BigDecimal("250"), user, account, category);

        List<Expense> result = expenseRepository.searchByCategoryName(categoryName);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(expense.getId(), result.get(0).getId());
    }

    private Expense saveExpense(BigDecimal amount, User user, Account account, Category category) {
        return expenseRepository.save(
                new Expense(amount, LocalDateTime.now(), "comment", user, account, category));
    }

    private static String rnd() {
        return UUID.randomUUID().toString();
    }
}
