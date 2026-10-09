package ru.student.dataanalysis.repository;

import org.springframework.stereotype.Repository;
import ru.student.dataanalysis.model.DataSet;

import jakarta.annotation.Resource;
import java.util.List;

@Repository
public class DataSetRepository extends InMemoryCrudRepository<DataSet> {
    @Resource(name = "dataSetStorage")
    public void configureStorage(List<DataSet> dataSetStorage) {
        setStorage(dataSetStorage);
    }
}
