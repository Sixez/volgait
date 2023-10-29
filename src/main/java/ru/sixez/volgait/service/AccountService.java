package ru.sixez.volgait.service;

import ru.sixez.volgait.dto.AccountDto;
import ru.sixez.volgait.entity.Account;

import java.util.List;

public interface AccountService extends IService<Account, AccountDto> {
    Account getCurrentAccount();
    boolean exists(String username);
    Account getByUsername(String username);
    List<Account> getList(long start, int count);
    Account updateCredentials(Account account, String newUsername, String newPassword);
    void pay(long id, double amount);
    default void withdraw(long id, double amount) {
        pay(id, -amount);
    }
    void delete(String username);
}
