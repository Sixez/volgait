package ru.sixez.volgait.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.sixez.volgait.dto.RentDto;
import ru.sixez.volgait.entity.Account;
import ru.sixez.volgait.entity.Rent;
import ru.sixez.volgait.entity.RentTypeEnum;
import ru.sixez.volgait.entity.Transport;
import ru.sixez.volgait.exception.RentException;
import ru.sixez.volgait.repo.RentRepo;
import ru.sixez.volgait.service.RentService;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class RentServiceImpl implements RentService {
    @Autowired
    private RentRepo repo;

    private double getPricePerUnit(Transport transport, RentTypeEnum type) {
        return type == RentTypeEnum.Minutes ? transport.getMinutePrice() : transport.getDayPrice();
    }

    private double calculatePrice(Rent rent) {
        return calculatePrice(rent.getTimeStart(), rent.getTimeEnd(), rent.getPriceType(), rent.getPriceOfUnit());
    }

    private double calculatePrice(Date start, Date end, RentTypeEnum type, double pricePerUnit) {
        long duration = end.getTime() - start.getTime();

        if (type == RentTypeEnum.Minutes) {
            return Duration.ofMillis(duration).plusMinutes(1).minusNanos(1).toMinutes() * pricePerUnit;
        } else {
            return Duration.ofMillis(duration).plusDays(1).minusNanos(1).toDays() * pricePerUnit;
        }
    }

    // RentService impl
    @Override
    public boolean rentExists(long id) {
        return repo.existsById(id);
    }

    @Override
    public boolean isTransportRented(Transport transport) {
        return isTransportRented(transport.getId());
    }

    @Override
    public boolean isTransportRented(long transportId) {
        List<Rent> result = repo.findAllByTransportId(transportId);
        return !result.isEmpty() && !result.stream().allMatch(Rent::isEnded);
    }

    @Override
    public Rent addRent(RentTypeEnum type, Account user, Transport transport) {
        if (!transport.isCanBeRented()) {
            throw new RentException("This transport is not rentable");
        }
        if (isTransportRented(transport)) {
            throw new RentException("Transport is already rented");
        }
        if (Objects.equals(transport.getOwner().getId(), user.getId())) {
            throw new RentException("You can not rent your own transport");
        }

        Rent rent = new Rent();

        rent.setUser(user);
        rent.setTransport(transport);
        rent.setPriceType(type);
        rent.setPriceOfUnit(getPricePerUnit(transport, type));
        rent.setTimeStart(new Date());

        return repo.saveAndFlush(rent);
    }

    @Override
    public Rent endRent(Rent rent, double latitude, double longitude) {
        rent.setTimeEnd(new Date());
        rent.setFinalPrice(calculatePrice(rent));
        rent.getTransport().setLatitude(latitude);
        rent.getTransport().setLongitude(longitude);

        return repo.saveAndFlush(rent);
    }

    @Override
    public Rent getById(long id) {
        return repo.findById(id)
            .orElseThrow(() -> new RentException("Rent with id %d doesn't exist!".formatted(id)));

    }

    @Override
    public List<Rent> getRentsListByUserId(long userId) {
        return repo.findAllByUserId(userId);
    }

    @Override
    public List<Rent> getRentsListByTransportId(long transportId) {
        return repo.findAllByTransportId(transportId);
    }

    @Override
    public Rent fromDto(RentDto dto) {
        Transport transport = new Transport();
        transport.setId(dto.transportId());
        Account holder = new Account();
        holder.setId(dto.userId());
        Rent rent = new Rent(
                transport,
                holder,
                dto.timeStart(),
                dto.timeEnd(),
                dto.priceOfUnit(),
                dto.priceType(),
                dto.finalPrice()
        );
        rent.setId(dto.id());
        return rent;
    }

    @Override
    public RentDto toDto(Rent rent) {
        return new RentDto(
                rent.getId(),
                rent.getTransport().getId(),
                rent.getUser().getId(),
                rent.getTimeStart(),
                rent.getTimeEnd(),
                rent.getPriceOfUnit(),
                rent.getPriceType(),
                rent.getFinalPrice()
        );
    }
}
