package ru.student.dataanalysis.repository;

import org.springframework.data.repository.CrudRepository;
import ru.student.dataanalysis.model.Account;

import java.util.List;

public interface AccountRepository extends CrudRepository<Account, Long> {

    List<Account> findByUserId(Long userId);
}
