package ru.sixez.volgait.service;

import ru.sixez.volgait.dto.AccountDto;
import ru.sixez.volgait.dto.AuthRequest;
import ru.sixez.volgait.dto.AuthResponse;

public interface AuthenticationService {
    AuthResponse signIn(AuthRequest request);
    void register(AuthRequest request);
    void register(AccountDto account);
    void signOut(String token);
}
