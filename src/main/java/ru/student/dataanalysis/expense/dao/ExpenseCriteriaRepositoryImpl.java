package ru.student.dataanalysis.expense.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;
import ru.student.dataanalysis.expense.entity.Category;
import ru.student.dataanalysis.expense.entity.Expense;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public class ExpenseCriteriaRepositoryImpl implements ExpenseCriteriaRepository {

    private final EntityManager entityManager;

    public ExpenseCriteriaRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<Expense> findByPeriodAndMinAmount(LocalDate from, LocalDate to, BigDecimal minAmount) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Expense> query = cb.createQuery(Expense.class);
        Root<Expense> expense = query.from(Expense.class);

        Predicate inPeriod = cb.between(expense.<LocalDate>get("spentAt"), from, to);
        Predicate enough = cb.greaterThanOrEqualTo(expense.<BigDecimal>get("amount"), minAmount);
        query.select(expense).where(cb.and(inPeriod, enough));

        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public List<Expense> findByCategoryName(String categoryName) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Expense> query = cb.createQuery(Expense.class);
        Root<Expense> expense = query.from(Expense.class);

        Join<Expense, Category> category = expense.join("category", JoinType.INNER);
        query.select(expense).where(cb.equal(category.get("name"), categoryName));

        return entityManager.createQuery(query).getResultList();
    }
}
