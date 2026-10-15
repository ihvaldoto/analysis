package ru.student.dataanalysis.expense;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.student.dataanalysis.expense.dao.ExpenseCriteriaRepository;
import ru.student.dataanalysis.expense.entity.Account;
import ru.student.dataanalysis.expense.entity.AppUser;
import ru.student.dataanalysis.expense.entity.Category;
import ru.student.dataanalysis.expense.entity.Expense;
import ru.student.dataanalysis.expense.repository.AccountRepository;
import ru.student.dataanalysis.expense.repository.AppUserRepository;
import ru.student.dataanalysis.expense.repository.CategoryRepository;
import ru.student.dataanalysis.expense.repository.ExpenseRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@SpringBootTest
class ExpenseRepositoryTest {

    @Autowired
    private AppUserRepository userRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ExpenseRepository expenseRepository;
    @Autowired
    private ExpenseCriteriaRepository criteriaRepository;

    private Category category;
    private Account account;

    @BeforeEach
    void setUp() {
        AppUser user = new AppUser();
        user.setLogin(UUID.randomUUID().toString());
        user.setDisplayName("Тестовый пользователь");
        userRepository.save(user);

        account = new Account();
        account.setName("Карта");
        account.setCurrency("RUB");
        account.setBalance(new BigDecimal("10000.00"));
        account.setOwner(user);
        accountRepository.save(account);

        category = new Category();
        category.setName(UUID.randomUUID().toString());
        category.setOwner(user);
        categoryRepository.save(category);
    }

    private Expense expense(String amount, LocalDate date) {
        Expense expense = new Expense();
        expense.setAccount(account);
        expense.setCategory(category);
        expense.setAmount(new BigDecimal(amount));
        expense.setSpentAt(date);
        return expenseRepository.save(expense);
    }

    @Test
    void testFindByPeriodAndMinAmount() {
        LocalDate day = LocalDate.of(2031, 3, 10);
        Expense matching = expense("500.00", day);
        expense("50.00", day);                 // слишком маленькая сумма
        expense("700.00", day.plusDays(30));   // вне периода

        List<Expense> found = expenseRepository.findBySpentAtBetweenAndAmountGreaterThanEqual(
                day.minusDays(1), day.plusDays(1), new BigDecimal("100.00"));

        Assertions.assertEquals(1, found.size());
        Assertions.assertEquals(matching.getId(), found.getFirst().getId());
    }

    @Test
    void testFindByCategoryNameJpql() {
        Expense saved = expense("120.00", LocalDate.of(2031, 4, 1));

        List<Expense> found = expenseRepository.findByCategoryName(category.getName());

        Assertions.assertEquals(1, found.size());
        Assertions.assertEquals(saved.getId(), found.getFirst().getId());
    }

    @Test
    void testFindByCategoryNameUnknownIsEmpty() {
        expense("120.00", LocalDate.of(2031, 4, 1));

        Assertions.assertTrue(expenseRepository.findByCategoryName(UUID.randomUUID().toString()).isEmpty());
    }

    @Test
    void testCriteriaFindByPeriodAndMinAmount() {
        LocalDate day = LocalDate.of(2032, 3, 10);
        Expense matching = expense("500.00", day);
        expense("50.00", day);
        expense("700.00", day.plusDays(30));

        List<Expense> found = criteriaRepository.findByPeriodAndMinAmount(
                day.minusDays(1), day.plusDays(1), new BigDecimal("100.00"));

        Assertions.assertEquals(1, found.size());
        Assertions.assertEquals(matching.getId(), found.getFirst().getId());
    }

    @Test
    void testCriteriaFindByCategoryName() {
        Expense saved = expense("120.00", LocalDate.of(2032, 4, 1));

        List<Expense> found = criteriaRepository.findByCategoryName(category.getName());

        Assertions.assertEquals(1, found.size());
        Assertions.assertEquals(saved.getId(), found.getFirst().getId());
    }
}
