package ru.sixez.volgait.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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

    public AuthResponse signIn(AuthRequest request) {
        Account acc = accountRepo.findByUsername(request.username()).orElse(null);
        if (acc == null) {
            throw new AccountException("User with username %s does not exists!".formatted(request.username()));
        }

        UserDetails user = new AccountDetails(acc);
        Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.username(),
                request.password()
        ));

        if (auth.isAuthenticated()) {
            return new AuthResponse(request.username(), jwtService.generateToken(user));
        } else {
            throw new AccountException("Given invalid credentials for username %s".formatted(request.username()));
        }
    }

    public void register(AuthRequest request) {
        AccountDto newAccount = new AccountDto(0, request.username(), request.password(), false, 0);
        register(newAccount);
    }

    public void register(AccountDto account) {
        if (accountRepo.existsByUsername(account.username())) {
            throw new AccountException("User with username %s already exists!".formatted(account.username()));
        }
        Account acc = new Account().fromDto(account);
        acc.setPassword(passwordEncoder.encode(account.password()));
        accountRepo.saveAndFlush(acc);
    }

    public void signOut(String token) {
        jwtService.revoke(token);
    }
}
