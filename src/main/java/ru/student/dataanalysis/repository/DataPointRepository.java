package ru.student.dataanalysis.repository;

import org.springframework.stereotype.Repository;
import ru.student.dataanalysis.model.DataPoint;

import jakarta.annotation.Resource;
import java.util.List;

@Repository
public class DataPointRepository extends InMemoryCrudRepository<DataPoint> {
    @Resource(name = "dataPointStorage")
    public void configureStorage(List<DataPoint> dataPointStorage) {
        setStorage(dataPointStorage);
    }
}
