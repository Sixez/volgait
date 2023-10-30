package ru.sixez.volgait.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.sixez.volgait.Swagger2Config;
import ru.sixez.volgait.dto.AccountDto;
import ru.sixez.volgait.entity.Account;
import ru.sixez.volgait.exception.AccountException;
import ru.sixez.volgait.service.AccountService;
import ru.sixez.volgait.service.AuthenticationService;

import java.util.List;

@Tag(name = "Admin account", description = "Admin-access \"Account\" manipulation")
@RestController
@RequestMapping(AdminAccountController.ROUTE)
public class AdminAccountController extends AdminController {
    public static final String ROUTE = AdminController.ROUTE + "/Account";

    @Autowired
    private AccountService service;
    @Autowired
    private AuthenticationService authService;

    @Operation(
            summary = "Account list",
            description = "Retrieve account list with offset and amount specified",
            security = @SecurityRequirement(name = Swagger2Config.SECURITY_JWT)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = AccountDto.class)), mediaType = "application/json")),
            @ApiResponse(responseCode = "204", content = @Content),
            @ApiResponse(responseCode = "403", description = "Token not valid", content = @Content)
    })
    @GetMapping
    public ResponseEntity<?> accountList(@RequestParam(required = false) @Min(0) Long start, @RequestParam(required = false) @Min(0) Integer count) {
        if (start == null || start <= 0) start = 0L;
        if (count == null || count <= 0) count = 10;

        List<AccountDto> list = service.getList(start, count).stream()
                .peek(acc -> acc.setPassword("*****"))
                .map(Account::toDto)
                .toList();

        if (list.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(list);
    }

    @Operation(
            summary = "Create account",
            description = "Create new account with given credentials and data",
            security = @SecurityRequirement(name = Swagger2Config.SECURITY_JWT)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = AccountDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Account registration failed", content = @Content),
            @ApiResponse(responseCode = "403", description = "Token is not valid", content = @Content)
    })
    @PostMapping
    public ResponseEntity<?> createAccount(@RequestBody @Valid AccountDto accountDto) {
        try {
            Account created = authService.register(accountDto);

            return new ResponseEntity<>(created.toDto(), HttpStatus.CREATED);
        } catch (AccountException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(
            summary = "Account info",
            description = "Get account info based on id",
            security = @SecurityRequirement(name = Swagger2Config.SECURITY_JWT)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = AccountDto.class))),
            @ApiResponse(responseCode = "403", description = "Token is not valid", content = @Content),
            @ApiResponse(responseCode = "404", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> accountData(@PathVariable @Min(0) Long id) {
        Account account = service.getById(id);

        if (account == null) {
            return ResponseEntity.notFound().build();
        }

        account.setPassword("*****");
        return ResponseEntity.ok(account.toDto());
    }

    @Operation(
            summary = "Update account data",
            description = "Update account data based on id",
            security = @SecurityRequirement(name = Swagger2Config.SECURITY_JWT)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "202", content = @Content(schema = @Schema(implementation = AccountDto.class))),
            @ApiResponse(responseCode = "400", description = "Account data update failed", content = @Content),
            @ApiResponse(responseCode = "403", description = "Token is not valid", content = @Content),
            @ApiResponse(responseCode = "404", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAccount(@PathVariable @Min(0) Long id, @RequestBody @Valid AccountDto data) {
        if (!service.exists(id)) {
            return ResponseEntity.notFound().build();
        }

        try {
            Account updated = service.update(id, data);
            return new ResponseEntity<>(updated.toDto(), HttpStatus.ACCEPTED);
        } catch (AccountException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(
            summary = "Delete account data",
            description = "Delete account of given id from datasource and all other data relative to account, such as transport, rents, etc.",
            security = @SecurityRequirement(name = Swagger2Config.SECURITY_JWT)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", content = @Content),
            @ApiResponse(responseCode = "403", description = "Token is not valid", content = @Content),
            @ApiResponse(responseCode = "404", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAccount(@PathVariable @Min(0) Long id) {
        if (!service.exists(id)) {
            return ResponseEntity.notFound().build();
        }

        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (AccountException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
