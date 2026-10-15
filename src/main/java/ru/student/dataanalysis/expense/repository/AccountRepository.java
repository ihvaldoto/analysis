package ru.student.dataanalysis.expense.repository;

import org.springframework.data.repository.CrudRepository;
import ru.student.dataanalysis.expense.entity.Account;

public interface AccountRepository extends CrudRepository<Account, Long> {
}
