package ru.sixez.volgait.service;

import ru.sixez.volgait.dto.TransportDto;
import ru.sixez.volgait.entity.Account;
import ru.sixez.volgait.entity.Transport;
import ru.sixez.volgait.entity.TransportTypeEnum;

import java.util.List;

public interface TransportService extends IService<Transport, TransportDto> {
    Transport createTransport(TransportDto data, Account owner);
    List<Transport> getList(long start, int count, TransportTypeEnum type);
    List<Transport> searchInRadius(double latitude, double longitude, double radius);
    List<Transport> searchInRadius(double latitude, double longitude, double radius, TransportTypeEnum type);
}
