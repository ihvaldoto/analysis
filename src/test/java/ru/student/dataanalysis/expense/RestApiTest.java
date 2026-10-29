package ru.student.dataanalysis.expense;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
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
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles = "ADMIN")
class RestApiTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AppUserRepository userRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ExpenseRepository expenseRepository;

    private Category category;
    private Expense expense;
    private String description;

    @BeforeEach
    void setUp() {
        AppUser user = new AppUser();
        user.setLogin(UUID.randomUUID().toString());
        user.setDisplayName("Тестовый пользователь");
        userRepository.save(user);

        Account account = new Account();
        account.setName("Карта");
        account.setCurrency("RUB");
        account.setBalance(new BigDecimal("10000.00"));
        account.setOwner(user);
        accountRepository.save(account);

        category = new Category();
        category.setName(UUID.randomUUID().toString());
        category.setOwner(user);
        categoryRepository.save(category);

        description = UUID.randomUUID().toString();
        expense = new Expense();
        expense.setAccount(account);
        expense.setCategory(category);
        expense.setAmount(new BigDecimal("250.00"));
        expense.setDescription(description);
        expense.setSpentAt(LocalDate.of(2033, 6, 15));
        expenseRepository.save(expense);
    }

    // --- Spring Data REST ---

    @Test
    void testRepositoryExposedViaDataRest() throws Exception {
        mockMvc.perform(get("/expenses/" + expense.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value(description));
    }

    @Test
    void testAllRepositoriesExposedViaDataRest() throws Exception {
        for (String path : new String[]{"users", "accounts", "categories", "expenses", "budgets"}) {
            mockMvc.perform(get("/" + path)).andExpect(status().isOk());
        }
    }

    // --- RestController ---

    @Test
    void testFindByPeriodAndMinAmount() throws Exception {
        mockMvc.perform(get("/custom/expenses/findByPeriodAndMinAmount")
                        .param("from", "2033-06-01").param("to", "2033-06-30").param("minAmount", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.description == '" + description + "')]").exists());
    }

    @Test
    void testFindByCategoryName() throws Exception {
        mockMvc.perform(get("/custom/expenses/findByCategoryName").param("categoryName", category.getName()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(expense.getId()));
    }

    @Test
    void testCriteriaFindByPeriodAndMinAmount() throws Exception {
        mockMvc.perform(get("/custom/expenses/criteria/findByPeriodAndMinAmount")
                        .param("from", "2033-06-01").param("to", "2033-06-30").param("minAmount", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.description == '" + description + "')]").exists());
    }

    @Test
    void testCriteriaFindByCategoryName() throws Exception {
        mockMvc.perform(get("/custom/expenses/criteria/findByCategoryName")
                        .param("categoryName", category.getName()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(expense.getId()));
    }

    // --- HTML ---

    @Test
    void testExpenseListPage() throws Exception {
        mockMvc.perform(get("/custom/expenses/view/list"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(description)));
    }

    // --- обработка ошибок ---

    @Test
    void testNotFoundReturnsUnifiedError() throws Exception {
        mockMvc.perform(get("/custom/expenses/-1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Трата не найдена: -1"));
    }

    @Test
    void testBadParameterReturnsUnifiedError() throws Exception {
        mockMvc.perform(get("/custom/expenses/findByPeriodAndMinAmount")
                        .param("from", "не-дата").param("to", "2033-06-30").param("minAmount", "100"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testMissingParameterReturnsUnifiedError() throws Exception {
        mockMvc.perform(get("/custom/expenses/findByCategoryName"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    // --- Swagger / OpenAPI ---

    @Test
    void testSwaggerUiAvailable() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
    }

    @Test
    void testOpenApiContainsAllKindsOfEndpoints() throws Exception {
        String docs = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        org.junit.jupiter.api.Assertions.assertTrue(docs.contains("/custom/expenses/findByCategoryName"),
                "нет ручного REST-метода");
        org.junit.jupiter.api.Assertions.assertTrue(docs.contains("/custom/expenses/criteria/findByCategoryName"),
                "нет метода на Criteria API");
        org.junit.jupiter.api.Assertions.assertTrue(docs.contains("\"/expenses\""),
                "нет метода Spring Data REST");
    }
}
