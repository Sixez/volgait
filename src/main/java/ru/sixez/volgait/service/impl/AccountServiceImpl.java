package ru.sixez.volgait.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.sixez.volgait.dto.AccountDto;
import ru.sixez.volgait.entity.Account;
import ru.sixez.volgait.entity.AccountDetails;
import ru.sixez.volgait.exception.AccountException;
import ru.sixez.volgait.repo.AccountRepo;
import ru.sixez.volgait.service.AccountService;

import java.util.HashSet;
import java.util.List;

@Service
public class AccountServiceImpl implements AccountService {
    @Autowired
    private AccountRepo repo;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    //AccountService impl
    @Override
    public Account getCurrentAccount() {
        return getAccountByUsername(getCurrentUsername());
    }

    @Override
    public boolean accountExists(long id) {
        return repo.existsById(id);
    }

    @Override
    public boolean accountExists(String username) {
        return repo.existsByUsername(username);
    }

    @Override
    public Account getAccountById(long id) {
        return repo.findById(id)
                .orElseThrow(() -> new AccountException("Account with id %d not found!".formatted(id)));
    }

    @Override
    public Account getAccountByUsername(String username) {
        return repo.findByUsername(username)
                .orElseThrow(() -> new AccountException("Account with username %s not found!".formatted(username)));
    }

    @Override
    public List<Account> getAccountsList() {
        return repo.findAll();
    }

    @Override
    public List<Account> getAccountsList(long start, int count) {
        return repo.findByIdGreaterThan(start, count);
    }

    @Override
    public Account updateAccountCredentials(Account account, String newUsername, String newPassword) {
        AccountDto newAcc = new AccountDto(account.getId(), newUsername, newPassword, account.isAdmin(), account.getBalance());
        return updateAccount(account.getId(), newAcc);
    }

    @Override
    public Account updateAccount(long id, AccountDto newData) {
        if (!repo.existsById(id)) {
            throw new AccountException("Account with id %d doesn't exist!".formatted(id));
        }

        Account account = getAccountById(id);

        if (!account.getUsername().equals(newData.username()) && accountExists(newData.username())) {
            throw new AccountException("Account with username %s already exists!".formatted(newData.username()));
        }
        account.setUsername(newData.username());

        if (newData.password() != null && !newData.password().isEmpty()) {
            account.setPassword(passwordEncoder.encode(newData.password()));
        }

        account.setAdmin(newData.admin());
        account.setBalance(newData.balance());

        return repo.saveAndFlush(account);
    }

    @Override
    public void withdraw(long id, double amount) {
        Account account = getAccountById(id);
        account.setBalance(account.getBalance() - amount);
        repo.saveAndFlush(account);
    }

    @Override
    public void deleteAccount(long id) {
        repo.deleteById(id);
    }

    @Override
    public void deleteAccount(String username) {
        repo.deleteByUsername(username);
    }

    @Override
    public AccountDto toDto(Account account) {
        return new AccountDto(
                account.getId(),
                account.getUsername(),
                account.getPassword(),
                account.isAdmin(),
                account.getBalance()
        );
    }

    @Override
    public Account fromDto(AccountDto dto) {
        Account account =  new Account(
                dto.username(),
                dto.password(),
                dto.admin(),
                dto.balance()
        );
        account.setId(dto.id());
        return account;
    }
}
