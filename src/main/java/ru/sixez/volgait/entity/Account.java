package ru.sixez.volgait.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = Account.TABLE_NAME)
public class Account extends AbstractEntity {
    public static final String TABLE_NAME =  AbstractEntity.DB_PREFIX + "accounts";
    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    private boolean admin;

    @PositiveOrZero
    private double balance;
}
