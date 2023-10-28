package ru.sixez.volgait.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sixez.volgait.entity.Rent;

import java.util.List;

@Repository
public interface RentRepo extends JpaRepository<Rent, Long> {
    List<Rent> findAllByUserId(long userId);
    List<Rent> findAllByTransportId(long transportId);
}
