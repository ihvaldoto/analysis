package ru.student.dataanalysis.expense.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.student.dataanalysis.expense.dao.ExpenseCriteriaRepository;
import ru.student.dataanalysis.expense.entity.Expense;
import ru.student.dataanalysis.expense.exception.ResourceNotFoundException;
import ru.student.dataanalysis.expense.repository.ExpenseRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * REST-доступ к кастомным запросам по тратам (Spring Data и Criteria API).
 */
@RestController
@RequestMapping("/custom/expenses")
public class ExpenseRestController {

    private final ExpenseRepository expenseRepository;
    private final ExpenseCriteriaRepository criteriaRepository;

    public ExpenseRestController(ExpenseRepository expenseRepository,
                                 ExpenseCriteriaRepository criteriaRepository) {
        this.expenseRepository = expenseRepository;
        this.criteriaRepository = criteriaRepository;
    }

    /**
     * Возвращает трату по идентификатору, 404 если её нет.
     */
    @GetMapping("/{id}")
    public Expense getById(@PathVariable("id") Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Трата не найдена: " + id));
    }

    /**
     * Траты за период (включительно) с суммой не меньше заданной. Query Lookup Strategies.
     * Пример: /custom/expenses/findByPeriodAndMinAmount?from=2031-03-01&to=2031-03-31&minAmount=100
     */
    @GetMapping("/findByPeriodAndMinAmount")
    public List<Expense> findByPeriodAndMinAmount(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam("minAmount") BigDecimal minAmount) {
        return expenseRepository.findBySpentAtBetweenAndAmountGreaterThanEqual(from, to, minAmount);
    }

    /**
     * Траты по названию категории (JPQL).
     * Пример: /custom/expenses/findByCategoryName?categoryName=Еда
     */
    @GetMapping("/findByCategoryName")
    public List<Expense> findByCategoryName(@RequestParam("categoryName") String categoryName) {
        return expenseRepository.findByCategoryName(categoryName);
    }

    /**
     * То же, что findByPeriodAndMinAmount, но через Criteria API.
     */
    @GetMapping("/criteria/findByPeriodAndMinAmount")
    public List<Expense> criteriaFindByPeriodAndMinAmount(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam("minAmount") BigDecimal minAmount) {
        return criteriaRepository.findByPeriodAndMinAmount(from, to, minAmount);
    }

    /**
     * То же, что findByCategoryName, но через Criteria API.
     */
    @GetMapping("/criteria/findByCategoryName")
    public List<Expense> criteriaFindByCategoryName(@RequestParam("categoryName") String categoryName) {
        return criteriaRepository.findByCategoryName(categoryName);
    }
}
