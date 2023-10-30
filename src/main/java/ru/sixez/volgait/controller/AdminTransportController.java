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
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.sixez.volgait.Swagger2Config;
import ru.sixez.volgait.dto.TransportDto;
import ru.sixez.volgait.entity.Account;
import ru.sixez.volgait.entity.Transport;
import ru.sixez.volgait.entity.TransportTypeEnum;
import ru.sixez.volgait.exception.AccountException;
import ru.sixez.volgait.exception.TransportException;
import ru.sixez.volgait.service.AccountService;
import ru.sixez.volgait.service.TransportService;

import java.util.List;

@Tag(name = "Admin transport", description = "Admin-access \"Transport\" manipulation")
@RestController
@RequestMapping(AdminTransportController.ROUTE)
public class AdminTransportController extends AdminController {
    public static final String ROUTE = AdminController.ROUTE + "/Transport";

    @Autowired
    private TransportService service;
    @Autowired
    private AccountService accountService;

    @Operation(
            summary = "Transport list",
            description = "Retrieve transport list with offset, amount and/or type specified",
            security = @SecurityRequirement(name = Swagger2Config.SECURITY_JWT)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = TransportDto.class)), mediaType = "application/json")),
            @ApiResponse(responseCode = "204", content = @Content),
            @ApiResponse(responseCode = "403", description = "Token not valid", content = @Content)
    })
    @GetMapping
    public ResponseEntity<?> listTransport(@RequestParam @Min(0) Long start, @RequestParam @Min(0) Integer count, @RequestParam @NotNull TransportTypeEnum type) {
        if (start == null || start <= 0) start = 0L;
        if (count == null || count <= 0) count = 10;

        List<TransportDto> list = service.getList(start, count, type).stream()
                .map(Transport::toDto)
                .toList();

        if (list.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(list);
    }

    @Operation(
            summary = "Transport info",
            description = "Get information about transport based on id",
            security = @SecurityRequirement(name = Swagger2Config.SECURITY_JWT)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = TransportDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Token not valid", content = @Content),
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
            @ApiResponse(responseCode = "403", description = "Token not valid", content = @Content),
            @ApiResponse(responseCode = "404", content = @Content)
    })
    @PostMapping
    public ResponseEntity<?> createTransport(@RequestBody @Valid TransportDto data) {
        Account owner = accountService.getById(data.owner_id());

        if (owner == null) {
            return ResponseEntity.badRequest().body("Account with id %d doesn't exist".formatted(data.owner_id()));
        }

        try {
            Transport transport = service.createTransport(data, owner);
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
            @ApiResponse(responseCode = "403", description = "Token not valid", content = @Content),
            @ApiResponse(responseCode = "404", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTransport(@PathVariable @Min(0) Long id, @RequestBody @Valid TransportDto transportDto) {
        if (!service.exists(id)) {
            return ResponseEntity.notFound().build();
        }

        try {
            Transport transport = service.update(id, transportDto);
            return new ResponseEntity<>(transport.toDto(), HttpStatus.ACCEPTED);
        } catch (TransportException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(
            summary = "Delete transport",
            description = "Delete transport of specified id",
            security = @SecurityRequirement(name = Swagger2Config.SECURITY_JWT)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", content = @Content),
            @ApiResponse(responseCode = "400", content = @Content),
            @ApiResponse(responseCode = "403", description = "Token not valid", content = @Content),
            @ApiResponse(responseCode = "404", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTransport(@PathVariable @Min(0) Long id) {
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
