package org.delcom.ifs25001pbo2026springbootcashflow.adapter.repository;

import org.delcom.ifs25001pbo2026springbootcashflow.domain.repository.IRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/** Basis generik repository in-memory. */
public abstract class InMemoryRepository<T, ID> implements IRepository<T, ID> {
    protected final List<T> data = new ArrayList<>();
    private final Function<T, ID> idExtractor;

    protected InMemoryRepository(Function<T, ID> idExtractor) {
        this.idExtractor = idExtractor;
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(data); // salinan defensif
    }

    @Override
    public Optional<T> findById(ID id) {
        return data.stream().filter(i -> idExtractor.apply(i).equals(id)).findFirst();
    }

    @Override
    public T save(T entity) {
        data.add(entity);
        return entity;
    }

    @Override
    public boolean deleteById(ID id) {
        return data.removeIf(i -> idExtractor.apply(i).equals(id));
    }

    @Override
    public void update(T entity) {
        // objek mutable disimpan by-reference
    }

    @Override
    public List<T> findBy(Predicate<T> predicate) {
        return data.stream().filter(predicate).toList();
    }
}
