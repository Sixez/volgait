package ru.sixez.volgait.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.sixez.volgait.dto.AccountDto;
import ru.sixez.volgait.entity.Account;
import ru.sixez.volgait.exception.AccountException;
import ru.sixez.volgait.repo.AccountRepo;

import java.util.List;

@Service
public class AccountService extends AbstractCrudService<AccountDto, Account> {
    @Autowired
    private AccountRepo repo;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public Account getCurrentAccount() {
        return getByUsername(getCurrentUsername());
    }

    @Override
    protected AccountRepo repo() {
        return repo;
    }

    public boolean exists(String username) {
        return repo.existsByUsername(username);
    }

    public Account getByUsername(String username) {
        return repo.findByUsername(username).orElse(null);
    }

    public List<Account> getList(long start, int count) {
        return repo.findByIdGreaterThan(start, count);
    }

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
        Account newAccount = new Account().fromDto(newData);

        if (newData.password() != null && !newData.password().isEmpty()) {
            newAccount.setPassword(passwordEncoder.encode(newData.password()));
        }

        return repo.saveAndFlush(newAccount);
    }

    public void withdraw(long id, double amount) {
        pay(id, -amount);
    }

    public void pay(long id, double amount) {
        Account account = getById(id);
        account.setBalance(account.getBalance() + amount);
        update(account);
    }

    public void delete(String username) {
        repo.deleteByUsername(username);
    }
}
