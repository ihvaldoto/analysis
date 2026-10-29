package ru.student.dataanalysis.expense.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.student.dataanalysis.expense.entity.AppUser;
import ru.student.dataanalysis.expense.entity.Role;
import ru.student.dataanalysis.expense.exception.UserAlreadyExistsException;
import ru.student.dataanalysis.expense.repository.AppUserRepository;

import java.util.Optional;

@Service
public class AppUserServiceImpl implements AppUserService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AppUserServiceImpl(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Optional<AppUser> getByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    @Override
    public AppUser addUser(String login, String rawPassword) {
        return addUser(login, rawPassword, Role.USER);
    }

    @Override
    public AppUser addUser(String login, String rawPassword, Role role) {
        if (login == null || login.isBlank() || rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Логин и пароль не должны быть пустыми");
        }
        String normalizedLogin = login.trim();
        if (userRepository.findByLogin(normalizedLogin).isPresent()) {
            throw new UserAlreadyExistsException(normalizedLogin);
        }

        AppUser user = new AppUser();
        user.setLogin(normalizedLogin);
        user.setDisplayName(normalizedLogin);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            // одновременная регистрация с тем же логином
            throw new UserAlreadyExistsException(normalizedLogin);
        }
    }
}
