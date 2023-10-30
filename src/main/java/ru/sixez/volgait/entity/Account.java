package ru.sixez.volgait.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.sixez.volgait.dto.AccountDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = Account.TABLE_NAME)
public class Account extends AbstractEntity<AccountDto, Account> {
    public static final String TABLE_NAME =  DB_PREFIX + "accounts";

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    private boolean admin;

    @PositiveOrZero
    private double balance;

    @Override
    public AccountDto toDto() {
        return new AccountDto(
                getId(),
                username,
                password,
                admin,
                balance
        );
    }

    @Override
    public Account fromDto(AccountDto dto) {
        setId(dto.id());
        username = dto.username();
        password = dto.password();
        admin = dto.admin();
        balance = dto.balance();
        return this;
    }
}
