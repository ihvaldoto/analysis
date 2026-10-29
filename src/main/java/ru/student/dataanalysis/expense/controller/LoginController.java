package ru.student.dataanalysis.expense.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Страница входа. Саму аутентификацию (POST /login) выполняет Spring Security.
 */
@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
