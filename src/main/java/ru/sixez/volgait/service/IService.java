package ru.sixez.volgait.service;


import ru.sixez.volgait.entity.AbstractEntity;

import java.util.List;

public interface IService<E extends AbstractEntity, D extends Record> {
    boolean exists(long id);
    E getById(long id);
    List<E> getList();
    default E update(E entity) {
        return update(entity.getId(), toDto(entity));
    }
    E update(long id, D dto);
    default void delete(E entity) {
        delete(entity.getId());
    }
    void delete(long id);
    E fromDto(D dto);
    D toDto(E entity);
}
