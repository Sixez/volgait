package ru.sixez.volgait.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.sixez.volgait.Swagger2Config;
import ru.sixez.volgait.dto.AccountDto;
import ru.sixez.volgait.dto.AuthRequest;
import ru.sixez.volgait.dto.AuthResponse;
import ru.sixez.volgait.entity.Account;
import ru.sixez.volgait.exception.AccountException;
import ru.sixez.volgait.service.AccountService;
import ru.sixez.volgait.service.AuthenticationService;

@Tag(name = "Account", description = "Account data manipulation and auth")
@RestController
@RequestMapping(AccountController.ROUTE)
public class AccountController extends ApiController {
    public static final String ROUTE = ApiController.ROUTE + "/Account";

    private static final String ME = "/Me";
    public static final String ME_ENDPOINT = ROUTE + ME;
    private static final String SIGN_IN = "/SignIn";
    public static final String SIGN_IN_ENDPOINT = ROUTE + SIGN_IN;
    private static final String SIGN_UP = "/SignUp";
    public static final String SIGN_UP_ENDPOINT = ROUTE + SIGN_UP;
    private static final String SIGN_OUT = "/SignOut";
    public static final String SIGN_OUT_ENDPOINT = ROUTE + SIGN_OUT;
    private static final String UPDATE = "/Update";
    public static final String UPDATE_ENDPOINT = ROUTE + UPDATE;

    @Autowired
    private AccountService service;
    @Autowired
    private AuthenticationService authService;

    @Operation(
            summary = "Get currently logged in account info",
            description = "Retrieve account info regarding to JWT holder",
            security = @SecurityRequirement(name = Swagger2Config.SECURITY_JWT)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = AccountDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Token is not valid", content = @Content)
    })
    @GetMapping(ME)
    public ResponseEntity<?> me() {
        Account user = service.getCurrentAccount();
        user.setPassword("*****");
        AccountDto userDto = service.toDto(user);
        return ResponseEntity.ok(userDto);
    }

    @Operation(summary = "Authentication and retrieval of JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = AuthResponse.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid credentials", content = @Content)
    })
    @PostMapping(SIGN_IN)
    public ResponseEntity<?> signIn(@RequestBody @Valid AuthRequest request) {
        try {
            AuthResponse response = authService.signIn(request);
            return ResponseEntity.ok(response);
        } catch (AccountException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Register new user account", description = "Registration point for new users")
    @ApiResponses({
            @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = AuthRequest.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Account registration failed", content = @Content)
    })
    @PostMapping(SIGN_UP)
    public ResponseEntity<?> signUp(@RequestBody @Valid AuthRequest request) {
        try {
            authService.register(request);
            return new ResponseEntity<>(request, HttpStatus.CREATED);
        } catch (AccountException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(
            summary = "Revoking of JWT",
            description = "Server-sided revoking of corresponding JWT",
            security = @SecurityRequirement(name = Swagger2Config.SECURITY_JWT)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "202", content = @Content),
            @ApiResponse(responseCode = "403", description = "Token is not valid", content = @Content)
    })
    @PostMapping(SIGN_OUT)
    public ResponseEntity<?> signOut(@RequestHeader("Authorization") String authHeader) {
        if (!authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Invalid \"Authentication\" header");
        }

        String token = authHeader.substring(7);
        authService.signOut(token);
        return new ResponseEntity<>("Token revoked", HttpStatus.ACCEPTED);
    }

    @Operation(
            summary = "Update account credentials",
            description = "Update username or/and password of logged in user. Cannot set username of already existing user.",
            security = @SecurityRequirement(name = Swagger2Config.SECURITY_JWT)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "202", content = @Content(schema = @Schema(implementation = AccountDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Account data update failed", content = @Content),
            @ApiResponse(responseCode = "403", description = "Token is not valid", content = @Content)
    })
    @PutMapping(UPDATE)
    public ResponseEntity<?> update(@RequestBody @Valid AuthRequest request) {
        try {
            Account user = service.updateCredentials(service.getCurrentAccount(), request.username(), request.password());
            AccountDto newData = service.toDto(user);
            return new ResponseEntity<>(newData, HttpStatus.ACCEPTED);
        } catch (AccountException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
