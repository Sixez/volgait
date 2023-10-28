package ru.sixez.volgait.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@MappedSuperclass
public abstract class AbstractEntity {
    protected static final String DB_PREFIX = "vit_";

    @Id
    @GeneratedValue()
    @Column(name = "id", nullable = false, unique = true)
    private Long id;
}
