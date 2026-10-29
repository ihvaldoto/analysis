package ru.student.dataanalysis.expense.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.student.dataanalysis.expense.entity.AppUser;

import java.util.Optional;

@RepositoryRestResource(path = "users")
public interface AppUserRepository extends CrudRepository<AppUser, Long> {

    /**
     * Находит пользователя по логину.
     *
     * @param login логин
     */
    Optional<AppUser> findByLogin(@Param("login") String login);
}
