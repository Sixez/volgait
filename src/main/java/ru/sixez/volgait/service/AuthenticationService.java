package ru.sixez.volgait.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
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

@Service
public class AuthenticationService {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private AccountRepo accountRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public AuthResponse signIn(AuthRequest request) throws AuthenticationException {
        Account acc = accountRepo.findByUsername(request.username()).orElse(null);
        if (acc == null) {
            throw new AuthenticationCredentialsNotFoundException("User with username %s does not exists!".formatted(request.username()));
        }

        UserDetails user = new AccountDetails(acc);
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.username(),
                request.password()
        ));

        return new AuthResponse(request.username(), jwtService.generateToken(user));
    }

    public Account register(AuthRequest request) {
        AccountDto newAccount = new AccountDto(0, request.username(), request.password(), false, 0);
        return register(newAccount);
    }

    public Account register(AccountDto account) {
        if (accountRepo.existsByUsername(account.username())) {
            throw new AccountException("User with username %s already exist".formatted(account.username()));
        }
        Account acc = new Account().fromDto(account);
        acc.setId(null);
        acc.setPassword(passwordEncoder.encode(account.password()));
        return accountRepo.saveAndFlush(acc);
    }

    public void signOut(String token) {
        jwtService.revoke(token);
    }
}
