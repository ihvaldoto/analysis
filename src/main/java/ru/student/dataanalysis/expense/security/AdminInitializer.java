package ru.student.dataanalysis.expense.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import ru.student.dataanalysis.expense.entity.Role;
import ru.student.dataanalysis.expense.service.AppUserService;

/**
 * Создаёт администратора при старте, если его ещё нет и в настройках задан app.admin.password.
 * Иначе ADMIN нельзя получить через регистрацию: из формы выдаётся только роль USER.
 */
@Component
@ConditionalOnProperty("app.admin.password")
public class AdminInitializer implements ApplicationRunner {

    private final AppUserService userService;
    private final String login;
    private final String password;

    public AdminInitializer(AppUserService userService,
                            @Value("${app.admin.login:admin}") String login,
                            @Value("${app.admin.password}") String password) {
        this.userService = userService;
        this.login = login;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userService.getByLogin(login).isEmpty()) {
            userService.addUser(login, password, Role.ADMIN);
        }
    }
}
