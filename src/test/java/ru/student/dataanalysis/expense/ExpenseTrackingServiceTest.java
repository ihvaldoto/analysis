package ru.student.dataanalysis.expense;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.student.dataanalysis.expense.entity.Account;
import ru.student.dataanalysis.expense.entity.AppUser;
import ru.student.dataanalysis.expense.entity.Category;
import ru.student.dataanalysis.expense.entity.Expense;
import ru.student.dataanalysis.expense.repository.AccountRepository;
import ru.student.dataanalysis.expense.repository.AppUserRepository;
import ru.student.dataanalysis.expense.repository.CategoryRepository;
import ru.student.dataanalysis.expense.repository.ExpenseRepository;
import ru.student.dataanalysis.expense.service.ExpenseTrackingService;

import java.math.BigDecimal;
import java.time.LocalDate;

@SpringBootTest
class ExpenseTrackingServiceTest {

    @Autowired
    private ExpenseTrackingService service;
    @Autowired
    private AppUserRepository userRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ExpenseRepository expenseRepository;

    private Account account;
    private Category category;

    @BeforeEach
    void setUp() {
        AppUser user = new AppUser();
        user.setLogin(java.util.UUID.randomUUID().toString());
        user.setDisplayName("Тестовый пользователь");
        userRepository.save(user);

        account = new Account();
        account.setName("Карта");
        account.setCurrency("RUB");
        account.setBalance(new BigDecimal("1000.00"));
        account.setOwner(user);
        accountRepository.save(account);

        category = new Category();
        category.setName("Еда");
        category.setOwner(user);
        categoryRepository.save(category);
    }

    @Test
    void testRegisterExpenseCommitsExpenseAndBalance() {
        Expense saved = service.registerExpense(account.getId(), category.getId(),
                new BigDecimal("300.00"), "Продукты", LocalDate.of(2031, 5, 1));

        Assertions.assertTrue(expenseRepository.findById(saved.getId()).isPresent());
        BigDecimal balance = accountRepository.findById(account.getId()).orElseThrow().getBalance();
        Assertions.assertEquals(0, new BigDecimal("700.00").compareTo(balance));
    }

    @Test
    void testRegisterExpenseRollsBackWhenFundsAreInsufficient() {
        Assertions.assertThrows(IllegalStateException.class, () ->
                service.registerExpense(account.getId(), category.getId(),
                        new BigDecimal("5000.00"), "Слишком дорого", LocalDate.of(2031, 5, 2)));

        // трата не должна сохраниться, баланс не должен измениться
        Assertions.assertTrue(expenseRepository.findByAccountId(account.getId()).isEmpty());
        BigDecimal balance = accountRepository.findById(account.getId()).orElseThrow().getBalance();
        Assertions.assertEquals(0, new BigDecimal("1000.00").compareTo(balance));
    }

    @Test
    void testRegisterExpenseUnknownAccountFails() {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                service.registerExpense(-1L, category.getId(),
                        new BigDecimal("10.00"), "x", LocalDate.of(2031, 5, 3)));
    }
}
