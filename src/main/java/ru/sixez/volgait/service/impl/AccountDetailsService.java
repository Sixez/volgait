package ru.sixez.volgait.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.sixez.volgait.entity.Account;
import ru.sixez.volgait.entity.AccountDetails;
import ru.sixez.volgait.exception.AccountException;
import ru.sixez.volgait.repo.AccountRepo;

@Service
public class AccountDetailsService implements UserDetailsService {
    @Autowired
    private AccountRepo repo;

    public Account getAccountByUsername(String username) {
        return repo.findByUsername(username)
                .orElseThrow(() -> new AccountException("User with username " + username + " not found!"));
    }

    // UserDetailsService impl
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (repo.existsByUsername(username)) {
            return new AccountDetails(getAccountByUsername(username));
        }
        return null;
    }
}
