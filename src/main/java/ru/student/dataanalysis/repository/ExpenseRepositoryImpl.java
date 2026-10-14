package ru.student.dataanalysis.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import ru.student.dataanalysis.model.Category;
import ru.student.dataanalysis.model.Expense;

import java.math.BigDecimal;
import java.util.List;

/**
 * Реализация кастомных методов репозитория трат через Criteria API.
 * Класс именуется ExpenseRepositoryImpl, чтобы Spring Data подхватил его
 * как фрагмент для интерфейса ExpenseRepository.
 */
public class ExpenseRepositoryImpl implements ExpenseRepositoryCustom {

    private final EntityManager entityManager;

    @Autowired
    public ExpenseRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<Expense> searchByUserIdAndAmountBetween(Long userId, BigDecimal min, BigDecimal max) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Expense> cq = cb.createQuery(Expense.class);
        Root<Expense> root = cq.from(Expense.class);

        Predicate byUser = cb.equal(root.get("user").get("id"), userId);
        Predicate byAmount = cb.between(root.get("amount"), min, max);

        cq.select(root).where(cb.and(byUser, byAmount));
        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public List<Expense> searchByCategoryName(String categoryName) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Expense> cq = cb.createQuery(Expense.class);
        Root<Expense> root = cq.from(Expense.class);
        Join<Expense, Category> category = root.join("category", JoinType.INNER);

        Predicate byName = cb.equal(category.get("name"), categoryName);

        cq.select(root).where(byName);
        return entityManager.createQuery(cq).getResultList();
    }
}
