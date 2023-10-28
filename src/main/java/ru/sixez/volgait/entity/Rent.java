package ru.sixez.volgait.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = Rent.TABLE_NAME)
public class Rent extends AbstractEntity {
    public static final String TABLE_NAME =  AbstractEntity.DB_PREFIX + "rents";

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
}
