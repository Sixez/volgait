package ru.sixez.volgait.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import ru.sixez.volgait.dto.AccountDto;
import ru.sixez.volgait.dto.AuthRequest;
import ru.sixez.volgait.dto.TransportDto;
import ru.sixez.volgait.entity.Account;
import ru.sixez.volgait.entity.Transport;

import java.util.List;

public interface AccountService {
    Account getCurrentAccount();
    boolean accountExists(long id);
    boolean accountExists(String username);
    Account getAccountById(long id);
    Account getAccountByUsername(String username);
    List<Account> getAccountsList();
    List<Account> getAccountsList(long start, int count);
    Account updateAccountCredentials(Account account, String newUsername, String newPassword);
    Account updateAccount(long id, AccountDto newData);
    void withdraw(long id, double amount);
    void deleteAccount(long id);
    void deleteAccount(String username);

    AccountDto toDto(Account account);
    Account fromDto(AccountDto dto);
}
