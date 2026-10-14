package ru.student.dataanalysis.repository;

import org.springframework.data.repository.CrudRepository;
import ru.student.dataanalysis.model.User;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> findByLogin(String login);
}
