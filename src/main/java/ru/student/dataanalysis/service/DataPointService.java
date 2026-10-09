package ru.student.dataanalysis.service;

import ru.student.dataanalysis.model.DataPoint;

import java.util.List;

public interface DataPointService {
    DataPoint create(Long dataSetId, String metric, double value, String unit);

    DataPoint findById(Long id);

    List<DataPoint> findAll();

    List<DataPoint> findByDataSet(Long dataSetId);

    void update(Long id, Long dataSetId, String metric, double value, String unit);

    void delete(Long id);
}
