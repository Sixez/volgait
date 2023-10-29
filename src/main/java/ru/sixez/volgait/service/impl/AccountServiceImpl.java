package ru.sixez.volgait.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.sixez.volgait.dto.AccountDto;
import ru.sixez.volgait.entity.Account;
import ru.sixez.volgait.exception.AccountException;
import ru.sixez.volgait.repo.AccountRepo;
import ru.sixez.volgait.service.AccountService;

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
        return getByUsername(getCurrentUsername());
    }

    @Override
    public boolean exists(long id) {
        return repo.existsById(id);
    }

    @Override
    public boolean exists(String username) {
        return repo.existsByUsername(username);
    }

    @Override
    public Account getById(long id) {
        return repo.findById(id)
                .orElseThrow(() -> new AccountException("Account with id %d not found!".formatted(id)));
    }

    @Override
    public Account getByUsername(String username) {
        return repo.findByUsername(username)
                .orElseThrow(() -> new AccountException("Account with username %s not found!".formatted(username)));
    }

    @Override
    public List<Account> getList() {
        return repo.findAll();
    }

    @Override
    public List<Account> getList(long start, int count) {
        return repo.findByIdGreaterThan(start, count);
    }

    @Override
    public Account updateCredentials(Account account, String newUsername, String newPassword) {
        AccountDto newAcc = new AccountDto(account.getId(), newUsername, newPassword, account.isAdmin(), account.getBalance());
        return update(account.getId(), newAcc);
    }

    @Override
    public Account update(long id, AccountDto newData) {
        if (!exists(id)) {
            throw new AccountException("Account with id %d doesn't exist!".formatted(id));
        }

        Account account = getById(id);

        if (!account.getUsername().equals(newData.username()) && exists(newData.username())) {
            throw new AccountException("Account with username %s already exists!".formatted(newData.username()));
        }
        Account newAccount = fromDto(newData);

        if (newData.password() != null && !newData.password().isEmpty()) {
            newAccount.setPassword(passwordEncoder.encode(newData.password()));
        }

        return repo.saveAndFlush(newAccount);
    }

    @Override
    public void pay(long id, double amount) {
        Account account = getById(id);
        account.setBalance(account.getBalance() + amount);
        update(account);
    }

    @Override
    public void delete(long id) {
        repo.deleteById(id);
    }

    @Override
    public void delete(String username) {
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
