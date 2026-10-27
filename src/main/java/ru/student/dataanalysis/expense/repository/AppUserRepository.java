package ru.student.dataanalysis.expense.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.student.dataanalysis.expense.entity.AppUser;

@RepositoryRestResource(path = "users")
public interface AppUserRepository extends CrudRepository<AppUser, Long> {
}
