package ru.student.dataanalysis.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import ru.student.dataanalysis.config.ApplicationInfo;
import ru.student.dataanalysis.model.DataSet;
import ru.student.dataanalysis.repository.DataPointRepository;
import ru.student.dataanalysis.repository.DataSetRepository;
import ru.student.dataanalysis.repository.UserRepository;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class DataSetServiceImpl implements DataSetService {
    private final DataSetRepository repository;
    private final DataPointRepository dataPointRepository;
    private final UserRepository userRepository;
    private final ApplicationInfo applicationInfo;
    private final AtomicLong ids = new AtomicLong(1);

    public DataSetServiceImpl(DataSetRepository repository, DataPointRepository dataPointRepository,
                              UserRepository userRepository, ApplicationInfo applicationInfo) {
        this.repository = repository;
        this.dataPointRepository = dataPointRepository;
        this.userRepository = userRepository;
        this.applicationInfo = applicationInfo;
    }

    @PostConstruct
    public void printApplicationInfo() {
        System.out.printf("%s, version %s is starting%n", applicationInfo.getName(), applicationInfo.getVersion());
    }

    @Override
    public DataSet create(String name, String description, Long ownerId) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name cannot be empty");
        userRepository.read(ownerId);
        DataSet dataSet = new DataSet(ids.getAndIncrement(), name, description, ownerId);
        repository.create(dataSet);
        return dataSet;
    }

    @Override
    public DataSet findById(Long id) { return repository.read(id); }

    @Override
    public List<DataSet> findAll() { return repository.findAll(); }

    @Override
    public List<DataSet> findByOwner(Long ownerId) {
        return repository.findAll().stream().filter(dataSet -> dataSet.getOwnerId().equals(ownerId)).toList();
    }

    @Override
    public void update(Long id, String name, String description, Long ownerId) {
        DataSet existing = repository.read(id);
        if (!existing.getOwnerId().equals(ownerId)) throw new IllegalArgumentException("This data set belongs to another user");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name cannot be empty");
        existing.setName(name);
        existing.setDescription(description);
        repository.update(existing);
    }

    @Override
    public void delete(Long id, Long ownerId) {
        DataSet existing = repository.read(id);
        if (!existing.getOwnerId().equals(ownerId)) throw new IllegalArgumentException("This data set belongs to another user");
        if (dataPointRepository.findAll().stream().anyMatch(point -> point.getDataSetId().equals(id))) {
            throw new IllegalArgumentException("Delete the data points before deleting this data set");
        }
        repository.delete(id);
    }
}
