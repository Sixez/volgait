package ru.sixez.volgait.controller;

import io.swagger.v3.oas.annotations.Operation;
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
import ru.sixez.volgait.dto.TransportDto;
import ru.sixez.volgait.entity.Account;
import ru.sixez.volgait.entity.Transport;
import ru.sixez.volgait.exception.AccountException;
import ru.sixez.volgait.exception.TransportException;
import ru.sixez.volgait.service.AccountService;
import ru.sixez.volgait.service.TransportService;

@Tag(name = "Transport", description = "Transport manipulation")
@RestController
@RequestMapping(TransportController.ROUTE)
public class TransportController extends ApiController {
    public static final String ROUTE = ApiController.ROUTE + "/Transport";

    @Autowired
    private TransportService service;
    @Autowired
    private AccountService accountService;

    @Operation(summary = "Transport info", description = "Get information about transport based on id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = TransportDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "404", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> transportInfo(@PathVariable @Min(0) Long id) {
        Transport transport = service.getById(id);

        if (transport == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(transport.toDto());
    }

    @Operation(
            summary = "Create transport",
            description = "Create new transport with given data",
            security = @SecurityRequirement(name = Swagger2Config.SECURITY_JWT)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = TransportDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Transport creation failed", content = @Content),
            @ApiResponse(responseCode = "403", description = "Token not valid", content = @Content)
    })
    @PostMapping
    public ResponseEntity<?> createTransport(@RequestBody @Valid TransportDto transportDto) {
        if (service.exists(transportDto.identifier())) {
            return ResponseEntity.badRequest().body("Transport with given identifier is already in db");
        }
        try {
            Account owner = accountService.getCurrentAccount();
            Transport transport = service.createTransport(transportDto, owner);

            return new ResponseEntity<>(transport.toDto(), HttpStatus.CREATED);
        } catch (AccountException | TransportException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(
            summary = "Update transport data",
            description = "Update specified id's transport data\nTransport type is unchangeable",
            security = @SecurityRequirement(name = Swagger2Config.SECURITY_JWT)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "202", content = @Content),
            @ApiResponse(responseCode = "400", description = "Transport update failed", content = @Content),
            @ApiResponse(responseCode = "403", description = "Token not valid or attempt to modify foreign transport", content = @Content),
            @ApiResponse(responseCode = "404", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTransport(@PathVariable @Min(0) Long id, @RequestBody @Valid TransportDto transportDto) {
        if (!service.exists(id)) {
            return ResponseEntity.notFound().build();
        }

        long requesterId = accountService.getCurrentAccount().getId();
        long ownerId = service.getById(id).getOwner().getId();
        if (requesterId != ownerId) {
            return new ResponseEntity<>("Only transport owner can modify it!", HttpStatus.FORBIDDEN);
        }

        try {
            Transport updated = service.update(id, transportDto);
            return new ResponseEntity<>(updated.toDto(), HttpStatus.ACCEPTED);
        } catch (TransportException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(
            summary = "Delete transport",
            security = @SecurityRequirement(name = Swagger2Config.SECURITY_JWT)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", content = @Content),
            @ApiResponse(responseCode = "403", description = "Token not valid or attempt to delete foreign transport", content = @Content),
            @ApiResponse(responseCode = "404", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTransport(@PathVariable @Min(0) Long id) {
        if (!service.exists(id)) {
            return ResponseEntity.notFound().build();
        }

        long requesterId = accountService.getCurrentAccount().getId();
        long ownerId = service.getById(id).getOwner().getId();
        if (requesterId != ownerId) {
            return new ResponseEntity<>("Only transport owner can delete it!", HttpStatus.FORBIDDEN);
        }

        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (AccountException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
