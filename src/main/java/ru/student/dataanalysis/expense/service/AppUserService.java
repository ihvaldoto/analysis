package ru.student.dataanalysis.expense.service;

import ru.student.dataanalysis.expense.entity.AppUser;
import ru.student.dataanalysis.expense.entity.Role;

import java.util.Optional;

public interface AppUserService {

    /**
     * Находит пользователя по логину.
     *
     * @param login логин
     */
    Optional<AppUser> getByLogin(String login);

    /**
     * Регистрирует обычного пользователя (роль USER), пароль сохраняется в виде BCrypt-хеша.
     *
     * @param login       логин
     * @param rawPassword пароль в открытом виде
     * @throws IllegalArgumentException   если логин или пароль пустые
     * @throws ru.student.dataanalysis.expense.exception.UserAlreadyExistsException если логин занят
     */
    AppUser addUser(String login, String rawPassword);

    /**
     * То же, но с явной ролью. Используется только внутри приложения, из формы роль выбрать нельзя.
     */
    AppUser addUser(String login, String rawPassword, Role role);
}
