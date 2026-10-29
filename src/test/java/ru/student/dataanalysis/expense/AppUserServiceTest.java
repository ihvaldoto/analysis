package ru.student.dataanalysis.expense;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.student.dataanalysis.expense.entity.AppUser;
import ru.student.dataanalysis.expense.entity.Role;
import ru.student.dataanalysis.expense.exception.UserAlreadyExistsException;
import ru.student.dataanalysis.expense.service.AppUserService;

import java.util.UUID;

@SpringBootTest
class AppUserServiceTest {

    @Autowired
    private AppUserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void testAddUserStoresHashedPasswordAndUserRole() {
        String login = UUID.randomUUID().toString();

        userService.addUser(login, "secret");

        AppUser found = userService.getByLogin(login).orElseThrow();
        Assertions.assertEquals(Role.USER, found.getRole());
        Assertions.assertNotEquals("secret", found.getPassword());
        Assertions.assertTrue(passwordEncoder.matches("secret", found.getPassword()));
    }

    @Test
    void testAddUserWithAdminRole() {
        String login = UUID.randomUUID().toString();

        userService.addUser(login, "secret", Role.ADMIN);

        Assertions.assertEquals(Role.ADMIN, userService.getByLogin(login).orElseThrow().getRole());
    }

    @Test
    void testAddUserDuplicateLoginFails() {
        String login = UUID.randomUUID().toString();
        userService.addUser(login, "secret");

        Assertions.assertThrows(UserAlreadyExistsException.class, () -> userService.addUser(login, "other"));
    }

    @Test
    void testAddUserBlankDataFails() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> userService.addUser(" ", "secret"));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> userService.addUser(UUID.randomUUID().toString(), ""));
    }

    @Test
    void testGetByLoginUnknownIsEmpty() {
        Assertions.assertTrue(userService.getByLogin(UUID.randomUUID().toString()).isEmpty());
    }
}
