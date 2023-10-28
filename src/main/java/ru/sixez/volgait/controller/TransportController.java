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

import java.util.Objects;

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
        if (!service.transportExists(id)) {
            return ResponseEntity.notFound().build();
        }

        Transport transport = service.getById(id);
        TransportDto transportDto = service.toDto(transport);
        return ResponseEntity.ok(transportDto);
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
        try {
            Account owner = accountService.getCurrentAccount();
            Transport transport = service.createTransport(transportDto, owner);
            TransportDto createdDto = service.toDto(transport);
            return new ResponseEntity<>(createdDto, HttpStatus.CREATED);
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
        try {
            if (!service.transportExists(id)) {
                return ResponseEntity.notFound().build();
            }

            long callerId = accountService.getCurrentAccount().getId();
            long ownerId = service.getById(id).getOwner().getId();
            if (callerId != ownerId) {
                return new ResponseEntity<>("Only transport owner can modify it!", HttpStatus.FORBIDDEN);
            }

            Transport transport = service.updateTransport(id, transportDto);
            TransportDto newData = service.toDto(transport);
            return new ResponseEntity<>(newData, HttpStatus.ACCEPTED);
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
        if (!service.transportExists(id)) {
            return ResponseEntity.notFound().build();
        }

        long callerId = accountService.getCurrentAccount().getId();
        long ownerId = service.getById(id).getOwner().getId();
        if (callerId != ownerId) {
            return new ResponseEntity<>("Only transport owner can delete it!", HttpStatus.FORBIDDEN);
        }

        try {
            service.deleteTransport(id);
            return ResponseEntity.noContent().build();
        } catch (AccountException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
