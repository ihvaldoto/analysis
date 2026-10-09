package ru.student.dataanalysis.service;

import ru.student.dataanalysis.model.DataSet;

import java.util.List;

public interface DataSetService {
    DataSet create(String name, String description, Long ownerId);

    DataSet findById(Long id);

    List<DataSet> findAll();

    List<DataSet> findByOwner(Long ownerId);

    void update(Long id, String name, String description, Long ownerId);

    void delete(Long id, Long ownerId);
}
