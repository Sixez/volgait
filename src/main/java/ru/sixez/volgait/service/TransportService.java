package ru.sixez.volgait.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.sixez.volgait.dto.TransportDto;
import ru.sixez.volgait.entity.Account;
import ru.sixez.volgait.entity.Transport;
import ru.sixez.volgait.entity.TransportTypeEnum;
import ru.sixez.volgait.exception.TransportException;
import ru.sixez.volgait.repo.TransportRepo;

import java.util.List;

@Service
public class TransportService extends AbstractCrudService<TransportDto, Transport> {
    @Autowired
    private TransportRepo repo;

    @Override
    protected TransportRepo repo() {
        return repo;
    }

    public boolean exists(String identifier) {
        return repo.existsByIdentifier(identifier);
    }

    public Transport createTransport(TransportDto data, Account owner) {
        if (data.transportType() == TransportTypeEnum.All) {
            throw new TransportException("Transport type cannot be \"All\"!");
        }

        Transport transport = new Transport().fromDto(data);
        transport.setId(null);
        transport.setOwner(owner);

        return repo.saveAndFlush(transport);
    }


    public List<Transport> getList(long start, int count) {
        return repo.findByIdGreaterThan(start, count);
    }

    public List<Transport> getList(long start, int count, TransportTypeEnum type) {
        if (type == TransportTypeEnum.All) {
            return getList(start, count);
        }
        return repo.findByIdGreaterThanAndType(start, count, type);
    }

    public List<Transport> searchInRadius(double latitude, double longitude, double radius) {
        return repo.findInRadius(latitude, longitude, radius);
    }

    public List<Transport> searchInRadius(double latitude, double longitude, double radius, TransportTypeEnum type) {
        if (type == TransportTypeEnum.All) {
            return searchInRadius(latitude, longitude, radius);
        }
        return repo.findInRadiusByType(latitude, longitude, radius, type);
    }

    @Override
    public Transport update(long id, TransportDto dto) {
        if (dto.transportType() == TransportTypeEnum.All) {
            throw new TransportException("Transport type cannot be \"All\"!");
        }

        Transport transport = getById(id);
        Transport newTransport = new Transport().fromDto(dto);

        newTransport.setId(id);
        newTransport.setOwner(transport.getOwner());
        newTransport.setTransportType(transport.getTransportType());

        return repo.saveAndFlush(newTransport);
    }
}
