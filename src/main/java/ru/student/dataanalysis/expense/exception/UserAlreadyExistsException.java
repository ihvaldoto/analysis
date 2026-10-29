package ru.student.dataanalysis.expense.exception;

/**
 * Пользователь с таким логином уже зарегистрирован.
 */
public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String login) {
        super("Пользователь уже существует: " + login);
    }
}
