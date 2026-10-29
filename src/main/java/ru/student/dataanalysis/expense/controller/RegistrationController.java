package ru.student.dataanalysis.expense.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.student.dataanalysis.expense.exception.UserAlreadyExistsException;
import ru.student.dataanalysis.expense.service.AppUserService;

/**
 * Регистрация новых пользователей. Роль всегда USER и из формы не берётся.
 */
@Controller
public class RegistrationController {

    private final AppUserService userService;

    public RegistrationController(AppUserService userService) {
        this.userService = userService;
    }

    @GetMapping("/registration")
    public String registration() {
        return "registration";
    }

    @PostMapping("/registration")
    public String addUser(@RequestParam(name = "username", defaultValue = "") String username,
                          @RequestParam(name = "password", defaultValue = "") String password,
                          Model model) {
        try {
            userService.addUser(username, password);
            return "redirect:/login?registered";
        } catch (UserAlreadyExistsException | IllegalArgumentException ex) {
            model.addAttribute("message", ex.getMessage());
            return "registration";
        }
    }
}
