package ru.sixez.volgait.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.sixez.volgait.dto.RentDto;
import ru.sixez.volgait.entity.Account;
import ru.sixez.volgait.entity.Rent;
import ru.sixez.volgait.entity.RentTypeEnum;
import ru.sixez.volgait.entity.Transport;
import ru.sixez.volgait.exception.RentException;
import ru.sixez.volgait.repo.RentRepo;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class RentService extends AbstractCrudService<RentDto, Rent> {
    @Autowired
    private RentRepo repo;

    private double getPricePerUnit(Transport transport, RentTypeEnum type) {
        return type == RentTypeEnum.Minutes ? transport.getMinutePrice() : transport.getDayPrice();
    }

    public double calculatePrice(Rent rent) {
        return calculatePrice(rent.getTimeStart(), rent.getTimeEnd(), rent.getPriceType(), rent.getPriceOfUnit());
    }

    private double calculatePrice(Date start, Date end, RentTypeEnum type, double pricePerUnit) {
        if (start == null) {
            throw new IllegalArgumentException("start date can not be null");
        }

        if (end == null) {
            end = new Date();
        }

        long duration = end.getTime() - start.getTime();

        if (type == RentTypeEnum.Minutes) {
            return Duration.ofMillis(duration).plusMinutes(1).minusNanos(1).toMinutes() * pricePerUnit;
        } else {
            return Duration.ofMillis(duration).plusDays(1).minusNanos(1).toDays() * pricePerUnit;
        }
    }

    @Override
    protected RentRepo repo() {
        return repo;
    }

    public boolean isTransportRented(Transport transport) {
        return isTransportRented(transport.getId());
    }

    public boolean isTransportRented(long transportId) {
        List<Rent> result = getListByTransportId(transportId);
        return !result.isEmpty() && !result.stream().allMatch(Rent::isEnded);
    }

    public Rent rent(RentTypeEnum type, Account user, Transport transport) {
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

    public Rent endRent(Rent rent, double latitude, double longitude) {
        rent.setTimeEnd(new Date());
        rent.setFinalPrice(calculatePrice(rent));
        rent.getTransport().setLatitude(latitude);
        rent.getTransport().setLongitude(longitude);

        return repo.saveAndFlush(rent);
    }

    public List<Rent> getListByUserId(long userId) {
        return repo.findAllByUserId(userId);
    }

    public List<Rent> getListByTransportId(long transportId) {
        return repo.findAllByTransportId(transportId);
    }

    @Override
    public Rent update(long id, RentDto newData) {
        Rent rent = getById(id);
        Rent newRent = new Rent().fromDto(newData);

        newRent.setId(id);
        newRent.setUser(rent.getUser());
        newRent.setTransport(rent.getTransport());

        return repo.saveAndFlush(newRent);
    }
}
