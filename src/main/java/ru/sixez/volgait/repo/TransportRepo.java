package ru.sixez.volgait.repo;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.sixez.volgait.entity.Transport;
import ru.sixez.volgait.entity.TransportTypeEnum;

import java.util.List;

@Repository
public interface TransportRepo extends JpaRepository<Transport, Long> {
    @Transactional
    void deleteByOwnerId(long ownerId);
    @Transactional
    List<Transport> findAllByOwnerId(long ownerId);

    boolean existsByIdentifier(String identifier);

    @Query(nativeQuery = true, value = "SELECT * FROM " + Transport.TABLE_NAME + " t WHERE t.id >= :start ORDER BY t.id ASC LIMIT :count")
    List<Transport> findByIdGreaterThan(@Param("start") long start, @Param("count") int count);

    @Query(nativeQuery = true, value = "SELECT * FROM " + Transport.TABLE_NAME + " t WHERE (t.id >= :start AND t.transport_type = :#{#type.name()}) ORDER BY t.id ASC LIMIT :count")
    List<Transport> findByIdGreaterThanAndType(@Param("start") long start, @Param("count") int count, @Param("type") TransportTypeEnum type);

    @Query(nativeQuery = true, value = "SELECT * FROM " + Transport.TABLE_NAME + " t WHERE earth_distance(ll_to_earth(t.latitude, t.longitude), ll_to_earth(:latitude, :longitude)) < (:radius * 1000)")
    List<Transport> findInRadius(@Param("latitude") double latitude, @Param("longitude") double longitude, @Param("radius") double radius);

    @Query(nativeQuery = true, value = "SELECT * FROM " + Transport.TABLE_NAME + " t WHERE earth_distance(ll_to_earth(t.latitude, t.longitude), ll_to_earth(:latitude, :longitude)) < (:radius * 1000) AND t.transport_type = :#{#type.name()}")
    List<Transport> findInRadiusByType(@Param("latitude") double latitude, @Param("longitude") double longitude, @Param("radius") double radius, @Param("type") TransportTypeEnum type);
}
