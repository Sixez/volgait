package ru.sixez.volgait.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import ru.sixez.volgait.dto.RentDto;
import ru.sixez.volgait.entity.Account;
import ru.sixez.volgait.entity.Rent;
import ru.sixez.volgait.entity.Transport;
import ru.sixez.volgait.exception.AccountException;
import ru.sixez.volgait.exception.RentException;
import ru.sixez.volgait.service.AccountService;
import ru.sixez.volgait.service.RentService;
import ru.sixez.volgait.service.TransportService;

import java.util.List;

@Tag(name = "Admin rent", description = "Admin-access \"Rent\" manipulation")
@RestController
@RequestMapping(AdminRentController.ROUTE)
public class AdminRentController extends AdminController {
    private static final String RENT_MAP = "/Rent";
    private static final String USER_HISTORY = "/UserHistory";
    private static final String TRANSPORT_HISTORY = "/TransportHistory";
    private static final String END = RENT_MAP + "/End";

    @Autowired
    private TransportService transportService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private RentService service;

    @Operation(
            summary = "Rent info",
            description = "Retrieve rent info based on id",
            security = @SecurityRequirement(name = Swagger2Config.SECURITY_JWT)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = @Content),
            @ApiResponse(responseCode = "403", description = "Token not valid", content = @Content),
            @ApiResponse(responseCode = "404", content = @Content)
    })
    @GetMapping(RENT_MAP + "/{rentId}")
    public ResponseEntity<?> rentInfo(@PathVariable @Min(0) Long rentId) {
        if (!service.exists(rentId)) {
            return ResponseEntity.notFound().build();
        }

        Rent rent = service.getById(rentId);
        RentDto data = service.toDto(rent);
        return ResponseEntity.ok(data);
    }

    @Operation(
            summary = "User history",
            description = "Get all rents of user with id",
            security = @SecurityRequirement(name = Swagger2Config.SECURITY_JWT)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = @Content),
            @ApiResponse(responseCode = "204", content = @Content),
            @ApiResponse(responseCode = "403", description = "Token not valid", content = @Content),
            @ApiResponse(responseCode = "404", content = @Content)
    })
    @GetMapping(USER_HISTORY + "/{userId}")
    public ResponseEntity<?> userHistory(@PathVariable @Min(0) Long userId) {
        if (accountService.exists(userId)) {
            return ResponseEntity.notFound().build();
        }
        Account user = accountService.getById(userId);

        List<RentDto> rents = service.getListByUserId(user.getId()).stream()
                .map(service::toDto)
                .toList();

        if (rents.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(rents);
    }

    @Operation(
            summary = "Transport history",
            description = "Get all rents of transport with id",
            security = @SecurityRequirement(name = Swagger2Config.SECURITY_JWT)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = @Content),
            @ApiResponse(responseCode = "204", content = @Content),
            @ApiResponse(responseCode = "403", description = "Token not valid", content = @Content),
            @ApiResponse(responseCode = "404", content = @Content)
    })
    @GetMapping(TRANSPORT_HISTORY + "/{transportId}")
    public ResponseEntity<?> transportHistory(@PathVariable @Min(0) Long transportId) {
        if (!transportService.exists(transportId)) {
            return ResponseEntity.notFound().build();
        }

        List<RentDto> rents = service.getListByTransportId(transportId).stream()
                .map(service::toDto)
                .toList();

        if (rents.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(rents);
    }

    @Operation(
            summary = "Rent transport",
            description = "Create new user to transport relation with renting",
            security = @SecurityRequirement(name = Swagger2Config.SECURITY_JWT)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = RentDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "400", content = @Content),
            @ApiResponse(responseCode = "403", description = "Token not valid", content = @Content)
    })
    @PostMapping(RENT_MAP)
    public ResponseEntity<?> createRent(@RequestBody @Valid RentDto data) {
        if (!transportService.exists(data.transportId()) || !accountService.exists(data.userId())) {
            return ResponseEntity.badRequest().build();
        }
        try {
            Transport transport = transportService.getById(data.transportId());
            Account user = accountService.getById(data.userId());

            Rent rent = service.rent(data.priceType(), user, transport);
            RentDto created = service.toDto(rent);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (RentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(
            summary = "End rent",
            description = "Close rent, update transport location and withdraw payment",
            security = @SecurityRequirement(name = Swagger2Config.SECURITY_JWT)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "202", content = @Content(schema = @Schema(implementation = RentDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "400", content = @Content),
            @ApiResponse(responseCode = "403", description = "Token not valid", content = @Content),
            @ApiResponse(responseCode = "404", content = @Content)
    })
    @PostMapping(END + "/{rentId}")
    public ResponseEntity<?> endRent(
            @PathVariable @Min(0) Long rentId,
            @Parameter(description = "latitude") @RequestParam("lat") @Min(0) Double latitude,
            @Parameter(description = "longitude") @RequestParam("long") @Min(0) Double longitude)
    {
        if (!service.exists(rentId)) {
            return ResponseEntity.notFound().build();
        }

        Rent rent = service.getById(rentId);

        try {
            rent = service.endRent(rent, latitude, longitude);
            transportService.update(rent.getTransport());

            accountService.withdraw(rent.getUser().getId(), rent.getFinalPrice());
            RentDto data = service.toDto(rent);

            return new ResponseEntity<>(data, HttpStatus.ACCEPTED);
        } catch (RentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(
            summary = "Update rent",
            description = "Update rent details with given data",
            security = @SecurityRequirement(name = Swagger2Config.SECURITY_JWT)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "202", content = @Content(schema = @Schema(implementation = RentDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Token not valid", content = @Content),
            @ApiResponse(responseCode = "404", content = @Content)
    })
    @PutMapping(RENT_MAP + "/{id}")
    public ResponseEntity<?> updateRent(@PathVariable @Min(0) Long id, @RequestBody @Valid RentDto data) {
        if (!service.exists(id)) {
            return ResponseEntity.notFound().build();
        }

        try {
            Rent updated = service.update(id, data);
            RentDto newData = service.toDto(updated);
            return new ResponseEntity<>(newData, HttpStatus.ACCEPTED);
        } catch (RentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(
            summary = "Delete rent",
            description = "Delete rent based on id",
            security = @SecurityRequirement(name = Swagger2Config.SECURITY_JWT)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", content = @Content),
            @ApiResponse(responseCode = "400", content = @Content),
            @ApiResponse(responseCode = "403", description = "Token not valid", content = @Content),
            @ApiResponse(responseCode = "404", content = @Content)
    })
    @DeleteMapping(RENT_MAP + "/{rentId}")
    public ResponseEntity<?> deleteRent(@PathVariable @Min(0) Long rentId) {
        if (!service.exists(rentId)) {
            return ResponseEntity.notFound().build();
        }

        try {
            service.delete(rentId);
            return ResponseEntity.noContent().build();
        } catch (AccountException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
