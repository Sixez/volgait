package ru.sixez.volgait.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ru.sixez.volgait.dto.RentDto;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = Rent.TABLE_NAME)
public class Rent extends AbstractEntity<RentDto, Rent> {
    public static final String TABLE_NAME =  DB_PREFIX + "rents";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transport_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Transport transport;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Account user;

    @Column(nullable = false)
    private Date timeStart;
    private Date timeEnd;

    @Column(nullable = false)
    private double priceOfUnit;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RentTypeEnum priceType;
    private double finalPrice;

    public boolean isEnded() {
        return timeEnd != null;
    }

    @Override
    public RentDto toDto() {
        return new RentDto(
                getId(),
                transport.getId(),
                user.getId(),
                timeStart,
                timeEnd,
                priceOfUnit,
                priceType,
                finalPrice
        );
    }

    @Override
    public Rent fromDto(RentDto dto) {
        setId(dto.id());

        transport = new Transport();
        transport.setId(dto.transportId());
        user = new Account();
        user.setId(dto.userId());
        timeStart = dto.timeStart();
        timeEnd = dto.timeEnd();
        priceOfUnit = dto.priceOfUnit();
        priceType = dto.priceType();
        finalPrice = dto.finalPrice();

        return this;
    }
}
