package ru.student.dataanalysis.expense;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.student.dataanalysis.expense.entity.AppUser;
import ru.student.dataanalysis.expense.entity.Role;
import ru.student.dataanalysis.expense.service.AppUserService;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AppUserService userService;

    // --- неавторизованный пользователь ---

    @Test
    void testAnonymousRedirectedToLoginFromRestApi() throws Exception {
        mockMvc.perform(get("/expenses"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void testAnonymousRedirectedToLoginFromCustomApiAndHtml() throws Exception {
        mockMvc.perform(get("/custom/expenses/findByCategoryName").param("categoryName", "x"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
        mockMvc.perform(get("/custom/expenses/view/list"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void testAnonymousSeesLoginAndRegistrationPages() throws Exception {
        mockMvc.perform(get("/login")).andExpect(status().isOk());
        mockMvc.perform(get("/registration")).andExpect(status().isOk());
    }

    // --- роли и Swagger ---

    @Test
    @WithMockUser(roles = "USER")
    void testUserHasNoAccessToSwagger() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isForbidden());
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testUserHasNoAccessToUsersList() throws Exception {
        mockMvc.perform(get("/users")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testUserCanUseApplication() throws Exception {
        mockMvc.perform(get("/custom/expenses/view/list")).andExpect(status().isOk());
        mockMvc.perform(get("/expenses")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAdminHasAccessToSwagger() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
    }

    // --- регистрация ---

    @Test
    void testRegistrationCreatesUserWithHashedPassword() throws Exception {
        String login = UUID.randomUUID().toString();

        mockMvc.perform(post("/registration").with(csrf())
                        .param("username", login).param("password", "secret"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        AppUser saved = userService.getByLogin(login).orElseThrow();
        Assertions.assertTrue(saved.getPassword().startsWith("$2"), "ожидался BCrypt-хеш");
        Assertions.assertNotEquals("secret", saved.getPassword());
    }

    @Test
    void testRegistrationIgnoresRoleFromForm() throws Exception {
        String login = UUID.randomUUID().toString();

        mockMvc.perform(post("/registration").with(csrf())
                .param("username", login).param("password", "secret").param("role", "ADMIN"));

        Assertions.assertEquals(Role.USER, userService.getByLogin(login).orElseThrow().getRole());
    }

    @Test
    void testRegistrationDuplicateShowsMessage() throws Exception {
        String login = UUID.randomUUID().toString();
        userService.addUser(login, "secret");

        mockMvc.perform(post("/registration").with(csrf())
                        .param("username", login).param("password", "other"))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"))
                .andExpect(model().attributeExists("message"));
    }

    @Test
    void testRegistrationWithoutCsrfRejected() throws Exception {
        mockMvc.perform(post("/registration").param("username", "x").param("password", "y"))
                .andExpect(status().isForbidden());
    }

    // --- вход и выход ---

    @Test
    void testRegisteredUserCanLogIn() throws Exception {
        String login = UUID.randomUUID().toString();
        userService.addUser(login, "secret");

        mockMvc.perform(formLogin().user(login).password("secret"))
                .andExpect(status().is3xxRedirection())
                .andExpect(authenticated().withUsername(login).withRoles("USER"));
    }

    @Test
    void testLoginWithWrongPasswordFails() throws Exception {
        String login = UUID.randomUUID().toString();
        userService.addUser(login, "secret");

        mockMvc.perform(formLogin().user(login).password("wrong"))
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @Test
    void testAdminRoleFromDatabaseGrantsAccessToSwagger() throws Exception {
        String login = UUID.randomUUID().toString();
        userService.addUser(login, "secret", Role.ADMIN);

        mockMvc.perform(formLogin().user(login).password("secret"))
                .andExpect(authenticated().withRoles("ADMIN"));
    }

    @Test
    void testLogout() throws Exception {
        mockMvc.perform(post("/logout").with(csrf()).with(user("someone")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"));
    }
}
