package ru.student.dataanalysis.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.student.dataanalysis.model.User;
import ru.student.dataanalysis.repository.DataSetRepository;
import ru.student.dataanalysis.repository.UserRepository;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository repository;
    private final DataSetRepository dataSetRepository;
    private final PasswordEncoder passwordEncoder;
    private final AtomicLong ids = new AtomicLong(1);

    public UserServiceImpl(UserRepository repository, DataSetRepository dataSetRepository,
                           PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.dataSetRepository = dataSetRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User register(String login, String password, String displayName) {
        requireText(login, "Login");
        requireText(password, "Password");
        requireText(displayName, "Display name");
        if (repository.findByLogin(login).isPresent()) {
            throw new IllegalArgumentException("Login is already taken");
        }
        User user = new User(ids.getAndIncrement(), login, passwordEncoder.encode(password), displayName);
        repository.create(user);
        return user;
    }

    @Override
    public User authenticate(String login, String password) {
        User user = repository.findByLogin(login)
                .orElseThrow(() -> new IllegalArgumentException("Invalid login or password"));
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid login or password");
        }
        return user;
    }

    @Override
    public User findById(Long id) { return repository.read(id); }

    @Override
    public List<User> findAll() { return repository.findAll(); }

    @Override
    public void updateDisplayName(Long id, String displayName) {
        requireText(displayName, "Display name");
        User user = repository.read(id);
        user.setDisplayName(displayName);
        repository.update(user);
    }

    @Override
    public void delete(Long id) {
        if (dataSetRepository.findAll().stream().anyMatch(dataSet -> dataSet.getOwnerId().equals(id))) {
            throw new IllegalArgumentException("Delete your data sets before deleting this account");
        }
        repository.delete(id);
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " cannot be empty");
    }
}
