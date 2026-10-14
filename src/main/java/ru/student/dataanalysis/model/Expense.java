package ru.student.dataanalysis.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Отдельная трата: сумма, дата и комментарий, привязанные к счёту и категории.
 */
@Entity
@Table(name = "expenses")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime spentAt;

    @Column
    private String comment;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    public Expense() {
    }

    public Expense(BigDecimal amount, LocalDateTime spentAt, String comment,
                   User user, Account account, Category category) {
        this.amount = amount;
        this.spentAt = spentAt;
        this.comment = comment;
        this.user = user;
        this.account = account;
        this.category = category;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDateTime getSpentAt() { return spentAt; }
    public void setSpentAt(LocalDateTime spentAt) { this.spentAt = spentAt; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    @Override
    public String toString() {
        return "Expense{id=" + id + ", amount=" + amount + ", spentAt=" + spentAt
                + ", comment='" + comment + "'}";
    }
}
