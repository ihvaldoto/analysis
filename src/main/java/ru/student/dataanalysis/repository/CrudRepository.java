package ru.student.dataanalysis.repository;

import ru.student.dataanalysis.model.Identifiable;

import java.util.List;

public interface CrudRepository<T extends Identifiable<ID>, ID> {
    void create(T entity);
    T read(ID id);
    List<T> findAll();
    void update(T entity);
    void delete(ID id);
}
