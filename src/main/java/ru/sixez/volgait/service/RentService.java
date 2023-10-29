package ru.sixez.volgait.service;

import ru.sixez.volgait.dto.RentDto;
import ru.sixez.volgait.entity.Account;
import ru.sixez.volgait.entity.Rent;
import ru.sixez.volgait.entity.RentTypeEnum;
import ru.sixez.volgait.entity.Transport;

import java.util.List;

public interface RentService extends IService<Rent, RentDto> {
    boolean isTransportRented(Transport transport);
    boolean isTransportRented(long transportId);
    Rent rent(RentTypeEnum type, Account user, Transport transport);
    Rent endRent(Rent rent, double latitude, double longitude);
    List<Rent> getListByUserId(long userId);
    List<Rent> getListByTransportId(long transportId);
}
