package ru.sixez.volgait.service;

import ru.sixez.volgait.dto.TransportDto;
import ru.sixez.volgait.entity.Account;
import ru.sixez.volgait.entity.Transport;
import ru.sixez.volgait.entity.TransportTypeEnum;

import java.util.List;

public interface TransportService {
    boolean transportExists(long id);
    Transport createTransport(TransportDto data, Account owner);
    Transport getById(long id);
    List<Transport> getTransportList();
    List<Transport> getTransportList(long start, int count, TransportTypeEnum type);
    List<Transport> searchInRadius(double latitude, double longitude, double radius);
    List<Transport> searchInRadius(double latitude, double longitude, double radius, TransportTypeEnum type);
    Transport updateTransport(Transport transport);
    Transport updateTransport(long id, TransportDto newData);
    void deleteTransport(long id);

    Transport fromDto(TransportDto dto);
    TransportDto toDto(Transport transport);
}
