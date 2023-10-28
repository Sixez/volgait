package ru.sixez.volgait.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.sixez.volgait.entity.Account;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepo extends JpaRepository<Account, Long> {
    boolean existsByUsername(String username);
    Optional<Account> findByUsername(String username);
    @Query(nativeQuery = true, value = "SELECT * FROM " + Account.TABLE_NAME + " t WHERE t.id >= :start ORDER BY t.id ASC LIMIT :count")
    List<Account> findByIdGreaterThan(@Param("start") long start, @Param("count") int count);
    void deleteByUsername(String username);
}
