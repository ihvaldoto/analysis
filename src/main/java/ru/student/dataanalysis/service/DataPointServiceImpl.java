package ru.student.dataanalysis.service;

import org.springframework.stereotype.Service;
import ru.student.dataanalysis.model.DataPoint;
import ru.student.dataanalysis.repository.DataPointRepository;
import ru.student.dataanalysis.repository.DataSetRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class DataPointServiceImpl implements DataPointService {
    private final DataPointRepository repository;
    private final DataSetRepository dataSetRepository;
    private final AtomicLong ids = new AtomicLong(1);

    public DataPointServiceImpl(DataPointRepository repository, DataSetRepository dataSetRepository) {
        this.repository = repository;
        this.dataSetRepository = dataSetRepository;
    }

    @Override
    public DataPoint create(Long dataSetId, String metric, double value, String unit) {
        dataSetRepository.read(dataSetId);
        if (metric == null || metric.isBlank()) throw new IllegalArgumentException("Metric cannot be empty");
        if (!Double.isFinite(value)) throw new IllegalArgumentException("Value must be finite");
        DataPoint point = new DataPoint(ids.getAndIncrement(), dataSetId, metric, value, unit, LocalDateTime.now());
        repository.create(point);
        return point;
    }

    @Override
    public DataPoint findById(Long id) { return repository.read(id); }

    @Override
    public List<DataPoint> findAll() { return repository.findAll(); }

    @Override
    public List<DataPoint> findByDataSet(Long dataSetId) {
        dataSetRepository.read(dataSetId);
        return repository.findAll().stream().filter(point -> point.getDataSetId().equals(dataSetId)).toList();
    }

    @Override
    public void update(Long id, Long dataSetId, String metric, double value, String unit) {
        dataSetRepository.read(dataSetId);
        DataPoint point = repository.read(id);
        if (metric == null || metric.isBlank()) throw new IllegalArgumentException("Metric cannot be empty");
        if (!Double.isFinite(value)) throw new IllegalArgumentException("Value must be finite");
        point.setDataSetId(dataSetId);
        point.setMetric(metric);
        point.setValue(value);
        point.setUnit(unit);
        repository.update(point);
    }

    @Override
    public void delete(Long id) { repository.delete(id); }
}
