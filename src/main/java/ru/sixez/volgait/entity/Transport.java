package ru.sixez.volgait.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ru.sixez.volgait.dto.TransportDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = Transport.TABLE_NAME)
public class Transport extends AbstractEntity<TransportDto, Transport> {
    public static final String TABLE_NAME =  DB_PREFIX + "transport";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Account owner;
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransportTypeEnum transportType;
    @Column(nullable = false)
    private String model;
    @Column(nullable = false, unique = true, length = 12)
    private String identifier;
    @Column(nullable = false, length = 64)
    private String color;

    private boolean canBeRented;

    @Column(nullable = false)
    private double longitude;
    @Column(nullable = false)
    private double latitude;
    private double minutePrice;
    private double dayPrice;

    @Override
    public TransportDto toDto() {
        return new TransportDto(
                getId(),
                owner.getId(),
                description,
                transportType,
                model,
                identifier,
                color,
                canBeRented,
                longitude,
                latitude,
                minutePrice,
                dayPrice
        );
    }

    @Override
    public Transport fromDto(TransportDto dto) {
        setId(dto.id());
        owner = new Account();
        owner.setId(dto.owner_id());
        description = dto.description();
        transportType = dto.transportType();
        model = dto.model();
        identifier = dto.identifier();
        color = dto.color();
        canBeRented = dto.canBeRented();
        longitude = dto.longitude();
        latitude = dto.latitude();
        minutePrice = dto.minutePrice();
        dayPrice = dto.dayPrice();
        return this;
    }
}
