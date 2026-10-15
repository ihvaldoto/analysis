package ru.student.dataanalysis.expense.repository;

import org.springframework.data.repository.CrudRepository;
import ru.student.dataanalysis.expense.entity.AppUser;

public interface AppUserRepository extends CrudRepository<AppUser, Long> {
}
