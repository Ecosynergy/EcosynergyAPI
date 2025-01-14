package app.ecosynergy.api.repositories;

import app.ecosynergy.api.models.FireReading;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FireReadingRepository extends JpaRepository<FireReading, Long> {
    @Query("SELECT r FROM FireReading r LEFT JOIN FETCH r.team WHERE r.id =:id")
    Optional<FireReading> findByIdWithTeam(Long id);

    @Query("SELECT r FROM FireReading r JOIN FETCH r.team t WHERE t.handle = :teamHandle")
    Page<FireReading> findByTeamHandle(String teamHandle, Pageable pageable);

    @Query("SELECT r FROM FireReading r JOIN FETCH r.team")
    Page<FireReading> findAllWithTeam(Pageable pageable);

    @Query("SELECT fr FROM FireReading fr WHERE fr.team.id = :teamId and fr.isFire = true ORDER BY fr.timestamp DESC LIMIT 1")
    Optional<FireReading> findLatestByTeamId(@Param("teamId") Long teamId);
}
