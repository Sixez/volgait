package ru.sixez.volgait.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.sixez.volgait.Swagger2Config;
import ru.sixez.volgait.dto.RentDto;
import ru.sixez.volgait.dto.TransportDto;
import ru.sixez.volgait.entity.*;
import ru.sixez.volgait.exception.RentException;
import ru.sixez.volgait.service.AccountService;
import ru.sixez.volgait.service.RentService;
import ru.sixez.volgait.service.TransportService;

import java.util.List;

@Tag(name = "Rent", description = "Search available transport and rent processing")
@RestController
@RequestMapping(RentController.ROUTE)
public class RentController extends ApiController {
    public static final String ROUTE = ApiController.ROUTE + "/Rent";

    private static final String TRANSPORT = "/Transport";
    public static final String TRANSPORT_ENDPOINT = ROUTE + TRANSPORT;
    private static final String MY_HISTORY = "/MyHistory";
    public static final String MY_HISTORY_ENDPOINT = ROUTE + MY_HISTORY;
    private static final String TRANSPORT_HISTORY = "/TransportHistory";
    public static final String TRANSPORT_HISTORY_ENDPOINT = ROUTE + TRANSPORT_HISTORY;
    private static final String NEW_RENT = "/New";
    public static final String NEW_RENT_ENDPOINT = ROUTE + NEW_RENT;
    private static final String END_RENT = "/End";
    public static final String END_RENT_ENDPOINT = ROUTE + END_RENT;

    @Autowired
    private TransportService transportService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private RentService service;

    @Operation(summary = "Search rentable transport", description = "Search transport available for rent based on geo position and search radius")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = TransportDto.class)), mediaType = "application/json")),
            @ApiResponse(responseCode = "204", content = @Content)
    })
    @GetMapping(TRANSPORT)
    public ResponseEntity<?> searchTransport(
            @Parameter(description = "latitude") @RequestParam("lat") @Min(0) Double latitude,
            @Parameter(description = "longitude") @RequestParam("long") @Min(0) Double longitude,
            @Parameter(description = "search radius in km") @RequestParam("radius") @Min(0) Double radius,
            @RequestParam("type") TransportTypeEnum type)
    {
        List<Transport> searchResult = transportService.searchInRadius(latitude, longitude, radius, type);

        List<TransportDto> responseList = searchResult.stream()
                .filter(Transport::isCanBeRented)
                .filter(transport -> !service.isTransportRented(transport.getId()))
                .map(Transport::toDto)
                .toList();

        if (responseList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(responseList);
    }

    @Operation(
            summary = "Rent info",
            description = "Retrieve current information about rent",
            security = @SecurityRequirement(name = Swagger2Config.SECURITY_JWT)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = @Content),
            @ApiResponse(responseCode = "403", description = "Token not valid or no access to rent info", content = @Content),
            @ApiResponse(responseCode = "404", content = @Content)
    })
    @GetMapping("/{rentId}")
    public ResponseEntity<?> rentInfo(@PathVariable @Min(0) Long rentId) {
        Rent rent = service.getById(rentId);

        if (rent == null) {
            return ResponseEntity.notFound().build();
        }

        long requesterId = accountService.getCurrentAccount().getId();
        long userId = rent.getUser().getId();
        long transportOwnerId = rent.getTransport().getOwner().getId();

        if (requesterId != userId && requesterId != transportOwnerId) {
            return new ResponseEntity<>("You have no access to this rent", HttpStatus.FORBIDDEN);
        }

        return ResponseEntity.ok(rent.toDto());
    }

    @Operation(
            summary = "Account rent history",
            description = "Get rents related to logged in user",
            security = @SecurityRequirement(name = Swagger2Config.SECURITY_JWT)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = @Content),
            @ApiResponse(responseCode = "204", content = @Content),
            @ApiResponse(responseCode = "403", description = "Token not valid", content = @Content)
    })
    @GetMapping(MY_HISTORY)
    public ResponseEntity<?> myHistory() {
        Account user = accountService.getCurrentAccount();

        List<RentDto> rents = service.getListByUserId(user.getId()).stream()
                .map(Rent::toDto)
                .toList();

        if (rents.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(rents);
    }

    @Operation(
            summary = "Transport rent history",
            description = "Get rents related to user's transport",
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
        Transport transport = transportService.getById(transportId);

        if (transport == null) {
            return ResponseEntity.notFound().build();
        }

        long requesterId = accountService.getCurrentAccount().getId();
        long transportOwnerId = transport.getOwner().getId();

        if (requesterId != transportOwnerId) {
            return new ResponseEntity<>("You can not get rents of foreign transport", HttpStatus.FORBIDDEN);
        }

        List<RentDto> rents = service.getListByTransportId(transportId).stream()
                .map(Rent::toDto)
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
            @ApiResponse(responseCode = "403", description = "Token not valid", content = @Content),
            @ApiResponse(responseCode = "404", content = @Content)
    })
    @PostMapping(NEW_RENT + "/{transportId}")
    public ResponseEntity<?> newRent(@PathVariable @Min(0) Long transportId, @RequestParam RentTypeEnum rentType) {
        Transport transport = transportService.getById(transportId);
        Account user = accountService.getCurrentAccount();

        if (transport == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            Rent rent = service.rent(rentType, user, transport);
            return new ResponseEntity<>(rent.toDto(), HttpStatus.CREATED);
        } catch (RentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(
            summary = "Close rent",
            description = "Close rent, update transport location and withdraw payment",
            security = @SecurityRequirement(name = Swagger2Config.SECURITY_JWT)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "202", content = @Content(schema = @Schema(implementation = RentDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "400", content = @Content),
            @ApiResponse(responseCode = "403", description = "Token not valid or this rent not belongs to user", content = @Content),
            @ApiResponse(responseCode = "404", content = @Content)
    })
    @PostMapping(END_RENT + "/{rentId}")
    public ResponseEntity<?> endRent(
            @PathVariable @Min(0) Long rentId,
            @Parameter(description = "latitude") @RequestParam("lat") @Min(0) Double latitude,
            @Parameter(description = "longitude") @RequestParam("long") @Min(0) Double longitude)
    {
        Rent rent = service.getById(rentId);

        if (rent == null) {
            return ResponseEntity.notFound().build();
        }

        if (rent.isEnded()) {
            return ResponseEntity.ok("This rent is already closed");
        }

        long requesterId = accountService.getCurrentAccount().getId();
        long userId = rent.getUser().getId();
        long transportOwnerId = rent.getUser().getId();
        if (requesterId != userId) {
            return new ResponseEntity<>("You have no access to this rent", HttpStatus.FORBIDDEN);
        }

        try {
            if (rent.getUser().getBalance() < service.calculatePrice(rent)) {
                return new ResponseEntity<>("Not enough money to end this rent", HttpStatus.FORBIDDEN);
            }

            Rent updated = service.endRent(rent, latitude, longitude);
            transportService.update(updated.getTransport());

            accountService.withdraw(userId, updated.getFinalPrice());
            accountService.pay(transportOwnerId, updated.getFinalPrice() * 0.9);

            return new ResponseEntity<>(updated.toDto(), HttpStatus.ACCEPTED);
        } catch (RentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
