package ru.student.dataanalysis.repository;

import ru.student.dataanalysis.model.Identifiable;

import java.util.List;
import java.util.Objects;

public abstract class InMemoryCrudRepository<T extends Identifiable<Long>> implements CrudRepository<T, Long> {
    protected List<T> storage;

    protected InMemoryCrudRepository() {
    }

    protected void setStorage(List<T> storage) {
        this.storage = storage;
    }

    @Override
    public void create(T entity) {
        Objects.requireNonNull(entity, "Entity cannot be null");
        if (entity.getId() == null) throw new IllegalArgumentException("Entity id cannot be null");
        if (storage.stream().anyMatch(item -> item.getId().equals(entity.getId()))) {
            throw new IllegalArgumentException("Entity with id " + entity.getId() + " already exists");
        }
        storage.add(entity);
    }

    @Override
    public T read(Long id) {
        return storage.stream().filter(item -> item.getId().equals(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Entity with id " + id + " not found"));
    }

    @Override
    public List<T> findAll() {
        return List.copyOf(storage);
    }

    @Override
    public void update(T entity) {
        Objects.requireNonNull(entity, "Entity cannot be null");
        int index = indexOf(entity.getId());
        storage.set(index, entity);
    }

    @Override
    public void delete(Long id) {
        storage.remove(indexOf(id));
    }

    private int indexOf(Long id) {
        for (int i = 0; i < storage.size(); i++) {
            if (storage.get(i).getId().equals(id)) return i;
        }
        throw new IllegalArgumentException("Entity with id " + id + " not found");
    }
}
