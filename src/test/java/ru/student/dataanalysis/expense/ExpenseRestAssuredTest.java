package ru.student.dataanalysis.expense;

import io.restassured.filter.cookie.CookieFilter;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import ru.student.dataanalysis.expense.entity.Account;
import ru.student.dataanalysis.expense.entity.AppUser;
import ru.student.dataanalysis.expense.entity.Category;
import ru.student.dataanalysis.expense.entity.Expense;
import ru.student.dataanalysis.expense.repository.AccountRepository;
import ru.student.dataanalysis.expense.repository.AppUserRepository;
import ru.student.dataanalysis.expense.repository.CategoryRepository;
import ru.student.dataanalysis.expense.repository.ExpenseRepository;
import ru.student.dataanalysis.expense.service.AppUserService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:practice8_api;MODE=PostgreSQL;DB_CLOSE_DELAY=-1"
})
class ExpenseRestAssuredTest {

    private static final String EXPENSES = "/custom/expenses";
    private static final String PASSWORD = "test-password";
    private static final Pattern CSRF_INPUT = Pattern.compile("name=\"_csrf\"\\s+value=\"([^\"]+)\"");

    @LocalServerPort private int port;
    @Autowired private AppUserService userService;
    @Autowired private AppUserRepository users;
    @Autowired private AccountRepository accounts;
    @Autowired private CategoryRepository categories;
    @Autowired private ExpenseRepository expenses;

    private final List<Long> expenseIds = new ArrayList<>();
    private CookieFilter session;
    private AppUser user;
    private Account account;
    private Category category;

    @BeforeEach
    void signIn() {
        user = userService.addUser("api-" + UUID.randomUUID(), PASSWORD);
        session = new CookieFilter();
        String form = given().port(port).filter(session)
                .when().get("/login")
                .then().statusCode(200).extract().asString();
        var csrf = CSRF_INPUT.matcher(form);
        assertTrue(csrf.find(), "Login form is missing the CSRF token");

        given().port(port).filter(session).redirects().follow(false)
                .formParam("username", user.getLogin()).formParam("password", PASSWORD)
                .formParam("_csrf", csrf.group(1))
                .when().post("/login")
                .then().statusCode(302).header("Location", endsWith("/custom/expenses/view/list"));
    }

    @AfterEach
    void deleteFixtures() {
        expenses.deleteAllById(expenseIds);
        if (category != null) categories.deleteById(category.getId());
        if (account != null) accounts.deleteById(account.getId());
        if (user != null) users.deleteById(user.getId());
    }

    @Test
    void returnsExistingExpenseAsJson() {
        createAccountAndCategory();
        long id = saveExpense("250.00", LocalDate.of(2040, 6, 15));

        given().port(port).filter(session).accept(ContentType.JSON)
                .when().get(EXPENSES + "/{id}", id)
                .then().statusCode(200).contentType(ContentType.JSON)
                .body("id", equalTo((int) id))
                .body("amount", equalTo(250.0f))
                .body("description", equalTo("Practice 8 expense"))
                .body("spentAt", equalTo("2040-06-15"));
    }

    @Test
    void returnsNotFoundForUnknownId() {
        given().port(port).filter(session).accept(ContentType.JSON)
                .when().get(EXPENSES + "/{id}", -1)
                .then().statusCode(404).contentType(ContentType.JSON)
                .body("message", equalTo("Трата не найдена: -1"));
    }

    @Test
    void returnsBadRequestForInvalidDate() {
        given().port(port).filter(session).accept(ContentType.JSON)
                .queryParam("from", "not-a-date").queryParam("to", "2040-06-30")
                .queryParam("minAmount", "250.00")
                .when().get(EXPENSES + "/findByPeriodAndMinAmount")
                .then().statusCode(400).contentType(ContentType.JSON)
                .body("message", not(emptyOrNullString()));
    }

    @Test
    void includesBothDateBoundariesAndExactMinimumAmount() {
        createAccountAndCategory();
        long firstDay = saveExpense("250.00", LocalDate.of(2040, 6, 1));
        long lastDay = saveExpense("300.00", LocalDate.of(2040, 6, 30));
        saveExpense("249.99", LocalDate.of(2040, 6, 15));
        saveExpense("300.00", LocalDate.of(2040, 7, 1));

        given().port(port).filter(session).accept(ContentType.JSON)
                .queryParam("from", "2040-06-01").queryParam("to", "2040-06-30")
                .queryParam("minAmount", "250.00")
                .when().get(EXPENSES + "/findByPeriodAndMinAmount")
                .then().statusCode(200).contentType(ContentType.JSON)
                .body("id", containsInAnyOrder((int) firstDay, (int) lastDay));
    }

    @Test
    void returnsEmptyArrayForUnknownCategory() {
        given().port(port).filter(session).accept(ContentType.JSON)
                .queryParam("categoryName", "missing-category")
                .when().get(EXPENSES + "/findByCategoryName")
                .then().statusCode(200).contentType(ContentType.JSON).body("$", empty());
    }

    private void createAccountAndCategory() {
        Account newAccount = new Account();
        newAccount.setName("Test account");
        newAccount.setCurrency("RUB");
        newAccount.setBalance(new BigDecimal("10000.00"));
        newAccount.setOwner(user);
        account = accounts.save(newAccount);

        Category newCategory = new Category();
        newCategory.setName("Test category");
        newCategory.setOwner(user);
        category = categories.save(newCategory);
    }

    private long saveExpense(String amount, LocalDate date) {
        Expense expense = new Expense();
        expense.setAccount(account);
        expense.setCategory(category);
        expense.setAmount(new BigDecimal(amount));
        expense.setDescription("Practice 8 expense");
        expense.setSpentAt(date);
        Long id = expenses.save(expense).getId();
        expenseIds.add(id);
        return id;
    }
}
