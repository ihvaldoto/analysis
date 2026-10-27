package ru.student.dataanalysis.expense.exception;

/**
 * Запрошенный ресурс не найден.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
