package ru.sixez.volgait.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@MappedSuperclass
public abstract class AbstractEntity<D extends Record, E extends AbstractEntity<D, E>> {
    protected static final String DB_PREFIX = "vit_";

    @Id
    @GeneratedValue()
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Transient
    public abstract D toDto();

    @Transient
    public abstract E fromDto(D dto);
}
