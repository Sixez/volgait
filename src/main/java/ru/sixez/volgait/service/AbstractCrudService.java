package ru.sixez.volgait.service;


import org.springframework.data.jpa.repository.JpaRepository;
import ru.sixez.volgait.entity.AbstractEntity;

import java.util.List;

public abstract class AbstractCrudService<D extends Record, E extends AbstractEntity<D, E>> {
    protected abstract <R extends JpaRepository<E, Long>> R repo();

    public boolean exists(long id) {
        return repo().existsById(id);
    }

    public E getById(long id) {
        return repo().findById(id).orElse(null);
    }
    public List<E> getAll() {
        return repo().findAll();
    }
    public E update(E entity) {
        return update(entity.getId(), entity.toDto());
    }
    public abstract E update(long id, D dto);
    public void delete(E entity) {
        repo().delete(entity);
    }

    public void delete(long id) {
        repo().deleteById(id);
    }
}
