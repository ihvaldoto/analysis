package ru.student.dataanalysis.service;

import ru.student.dataanalysis.model.User;

import java.util.List;

public interface UserService {
    User register(String login, String password, String displayName);

    User authenticate(String login, String password);

    User findById(Long id);

    List<User> findAll();

    void updateDisplayName(Long id, String displayName);

    void delete(Long id);
}
