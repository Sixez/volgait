package ru.sixez.volgait.service;

import ru.sixez.volgait.dto.RentDto;
import ru.sixez.volgait.entity.Account;
import ru.sixez.volgait.entity.Rent;
import ru.sixez.volgait.entity.RentTypeEnum;
import ru.sixez.volgait.entity.Transport;

import java.util.List;

public interface RentService {
    boolean isTransportRented(Transport transport);
    boolean isTransportRented(long transportId);
    boolean rentExists(long id);
    Rent addRent(RentTypeEnum type, Account user, Transport transport);
    Rent endRent(Rent rent, double latitude, double longitude);
    Rent getRentById(long id);
    List<Rent> getRentsListByUserId(long userId);
    List<Rent> getRentsListByTransportId(long transportId);
    Rent updateRent(Rent rent);
    Rent updateRent(long id, RentDto newData);
    void deleteRent(long id);

    Rent fromDto(RentDto dto);
    RentDto toDto(Rent rent);
}
