package ru.student.dataanalysis.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import ru.student.dataanalysis.config.ApplicationInfo;
import ru.student.dataanalysis.model.Account;
import ru.student.dataanalysis.model.Expense;
import ru.student.dataanalysis.repository.AccountRepository;
import ru.student.dataanalysis.repository.ExpenseRepository;

import java.math.BigDecimal;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final ExpenseRepository expenseRepository;
    private final PlatformTransactionManager transactionManager;
    private final ApplicationInfo applicationInfo;

    public AccountServiceImpl(AccountRepository accountRepository,
                              ExpenseRepository expenseRepository,
                              PlatformTransactionManager transactionManager,
                              ApplicationInfo applicationInfo) {
        this.accountRepository = accountRepository;
        this.expenseRepository = expenseRepository;
        this.transactionManager = transactionManager;
        this.applicationInfo = applicationInfo;
    }

    @PostConstruct
    public void printApplicationInfo() {
        System.out.printf("%s, version %s is starting%n",
                applicationInfo.getName(), applicationInfo.getVersion());
    }

    @Override
    public Account createAccountWithExpense(Account account, Expense expense) {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            accountRepository.save(account);

            expense.setAccount(account);
            if (expense.getAmount() == null || expense.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Expense amount must be positive");
            }
            expenseRepository.save(expense);

            transactionManager.commit(status);
            return account;
        } catch (RuntimeException ex) {
            transactionManager.rollback(status);
            throw ex;
        }
    }
}
