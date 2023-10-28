package ru.sixez.volgait.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.sixez.volgait.dto.TransportDto;
import ru.sixez.volgait.entity.Account;
import ru.sixez.volgait.entity.Transport;
import ru.sixez.volgait.entity.TransportTypeEnum;
import ru.sixez.volgait.exception.TransportException;
import ru.sixez.volgait.repo.TransportRepo;
import ru.sixez.volgait.service.TransportService;

import java.util.List;
import java.util.Optional;

@Service
public class TransportServiceImpl implements TransportService {
    @Autowired
    private TransportRepo repo;

    // TransportService impl
    @Override
    public boolean transportExists(long id) {
        return repo.existsById(id);
    }

    @Override
    public Transport createTransport(TransportDto data, Account owner) {
        if (data.transportType() == TransportTypeEnum.All) {
            throw new TransportException("Transport type cannot be \"All\"!");
        }

        Transport transport = fromDto(data);
        transport.setId(null);
        transport.setOwner(owner);

        repo.saveAndFlush(transport);

        List<Transport> all = repo.findAllByOwnerId(owner.getId());
        return all.isEmpty() ? transport : all.get(all.size() - 1);
    }

    @Override
    public Transport getById(long id) {
        return repo.findById(id)
                .orElseThrow(() -> new TransportException("Transport with id %d doesn't exist!".formatted(id)));
    }

    @Override
    public List<Transport> getTransportList() {
        return repo.findAll();
    }

    public List<Transport> getTransportList(long start, int count) {
        return repo.findByIdGreaterThan(start, count);
    }

    @Override
    public List<Transport> getTransportList(long start, int count, TransportTypeEnum type) {
        if (type == TransportTypeEnum.All) {
            return getTransportList(start, count);
        }
        return repo.findByIdGreaterThanAndType(start, count, type);
    }

    @Override
    public List<Transport> searchInRadius(double latitude, double longitude, double radius) {
        return repo.findInRadius(latitude, longitude, radius);
    }

    @Override
    public List<Transport> searchInRadius(double latitude, double longitude, double radius, TransportTypeEnum type) {
        if (type == TransportTypeEnum.All) {
            return searchInRadius(latitude, longitude, radius);
        }
        return repo.findInRadiusByType(latitude, longitude, radius, type);
    }

    @Override
    public Transport updateTransport(Transport transport) {
        return repo.saveAndFlush(transport);
    }

    @Override
    public Transport updateTransport(long id, TransportDto newData) {
        if (newData.transportType() == TransportTypeEnum.All) {
            throw new TransportException("Transport type cannot be \"All\"!");
        }
        Transport transport = getById(id);
        Transport newTransport = fromDto(newData);

        newTransport.setId(id);
        newTransport.setOwner(transport.getOwner());
        newTransport.setTransportType(transport.getTransportType());

        return repo.saveAndFlush(newTransport);
    }

    @Override
    public void deleteTransport(long id) {
        repo.deleteById(id);
    }

    @Override
    public Transport fromDto(TransportDto dto) {
        Account owner = new Account();
        owner.setId(dto.owner_id());
        Transport transport = new Transport(
                owner,
                dto.description(),
                dto.transportType(),
                dto.model(),
                dto.identifier(),
                dto.color(),
                dto.canBeRented(),
                dto.longitude(),
                dto.latitude(),
                dto.minutePrice(),
                dto.dayPrice()
        );
        transport.setId(dto.id());
        return transport;
    }

    @Override
    public TransportDto toDto(Transport transport) {
        return new TransportDto(
                transport.getId(),
                transport.getOwner().getId(),
                transport.getDescription(),
                transport.getTransportType(),
                transport.getModel(),
                transport.getIdentifier(),
                transport.getColor(),
                transport.isCanBeRented(),
                transport.getLongitude(),
                transport.getLatitude(),
                transport.getMinutePrice(),
                transport.getDayPrice()
        );
    }
}
