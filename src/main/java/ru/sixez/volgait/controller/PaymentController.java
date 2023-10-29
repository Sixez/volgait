package ru.sixez.volgait.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.sixez.volgait.Swagger2Config;
import ru.sixez.volgait.entity.Account;
import ru.sixez.volgait.service.AccountService;

import java.util.Objects;

@Tag(name = "Hesoyam", description = "Adds 250 000 money units on account")
@RestController
@RequestMapping(PaymentController.ROUTE)
public class PaymentController extends ApiController{
    public static final String ROUTE = ApiController.ROUTE + "/Payment";
    private static final String HESOYAM = "/Hesoyam";
    public static final String HESOYAM_ENDPOINT = ROUTE + "Payment/Hesoyam";

    @Autowired
    private AccountService accountService;

    @Operation(summary = "Free money?", security = @SecurityRequirement(name = Swagger2Config.SECURITY_JWT))
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = @Content),
            @ApiResponse(responseCode = "403", description = "Token not valid", content = @Content),
    })
    @PostMapping(HESOYAM + "/{accountId}")
    public ResponseEntity<?> hesoyam(@PathVariable @Min(0) Long accountId) {
        Account user = accountService.getCurrentAccount();

        if (!Objects.equals(accountId, user.getId()) && !user.isAdmin()) {
            return new ResponseEntity<>("Only admin can pay foreign user", HttpStatus.FORBIDDEN);
        }

        accountService.pay(accountId, 250000);

        return ResponseEntity.ok().build();
    }
}
