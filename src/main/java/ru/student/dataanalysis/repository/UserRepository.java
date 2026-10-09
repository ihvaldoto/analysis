package ru.student.dataanalysis.repository;

import org.springframework.stereotype.Repository;
import ru.student.dataanalysis.model.User;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository extends InMemoryCrudRepository<User> {
    @Resource(name = "userStorage")
    public void configureStorage(List<User> userStorage) {
        setStorage(userStorage);
    }

    public Optional<User> findByLogin(String login) {
        return storage.stream().filter(user -> user.getLogin().equalsIgnoreCase(login)).findFirst();
    }
}
