package ru.sixez.volgait.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.sixez.volgait.dto.AccountDto;
import ru.sixez.volgait.dto.AuthRequest;
import ru.sixez.volgait.dto.AuthResponse;
import ru.sixez.volgait.entity.Account;
import ru.sixez.volgait.entity.AccountDetails;
import ru.sixez.volgait.exception.AccountException;
import ru.sixez.volgait.repo.AccountRepo;
import ru.sixez.volgait.service.AuthenticationService;
import ru.sixez.volgait.service.JwtService;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private AccountRepo accountRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public AuthResponse signIn(AuthRequest request) {
        Account acc = accountRepo.findByUsername(request.username()).orElse(null);
        if (acc == null) {
            throw new AccountException("User with username " + request.username() + " not exists!");
        }
        UserDetails user = new AccountDetails(acc);
        Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.username(),
                request.password()
        ));
        if (auth.isAuthenticated()) {
            return new AuthResponse(request.username(), jwtService.generateToken(user));
        } else {
            throw new AccountException("Given invalid credentials for username " + request.username());
        }
    }

    @Override
    public void register(AuthRequest request) {
        AccountDto newAccount = new AccountDto(0, request.username(), request.password(), false, 0);
        register(newAccount);
    }

    @Override
    public void register(AccountDto account) {
        if (accountRepo.existsByUsername(account.username())) {
            throw new AccountException("User with username " + account.username() + " already exists!");
        }
        Account acc = new Account(account.username(), passwordEncoder.encode(account.password()), account.admin(), account.balance());
        accountRepo.saveAndFlush(acc);
    }

    @Override
    public void signOut(String token) {
        jwtService.revoke(token);
    }
}
