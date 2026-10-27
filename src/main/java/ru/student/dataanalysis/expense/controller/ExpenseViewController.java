package ru.student.dataanalysis.expense.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.student.dataanalysis.expense.repository.ExpenseRepository;

/**
 * HTML-страница со списком трат.
 */
@Controller
@RequestMapping("/custom/expenses/view")
public class ExpenseViewController {

    private final ExpenseRepository expenseRepository;

    public ExpenseViewController(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @GetMapping("/list")
    public String expenseListView(Model model) {
        model.addAttribute("expenses", expenseRepository.findAll());
        return "expenseList";
    }
}
