package ru.student.dataanalysis.expense.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.student.dataanalysis.expense.entity.Account;

@RepositoryRestResource(path = "accounts")
public interface AccountRepository extends CrudRepository<Account, Long> {
}
