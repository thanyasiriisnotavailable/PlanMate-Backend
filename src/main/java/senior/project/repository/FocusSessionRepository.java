package senior.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import senior.project.entity.FocusSession;
import senior.project.enums.FocusStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface FocusSessionRepository extends JpaRepository<FocusSession, String> {

    int countByUserUidAndStatusAndFocusStartBetween(
            String userUid, FocusStatus status, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(SUM(f.elapsedSeconds), 0) FROM FocusSession f " +
            "WHERE f.user.uid = :userUid AND f.status = :status AND f.focusStart BETWEEN :start AND :end")
    long sumElapsedSeconds(@Param("userUid") String userUid,
                           @Param("status") FocusStatus status,
                           @Param("start") LocalDateTime start,
                           @Param("end") LocalDateTime end);

    @Query("SELECT f.course.name, SUM(f.elapsedSeconds) " +
            "FROM FocusSession f " +
            "WHERE f.user.uid = :userUid AND f.status = :status " +
            "AND f.focusStart BETWEEN :start AND :end " +
            "GROUP BY f.course.name")
    List<Object[]> groupByCourseName(@Param("userUid") String userUid,
                                        @Param("status") FocusStatus status,
                                        @Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(fs.elapsedSeconds), 0) " +
            "FROM FocusSession fs " +
            "WHERE fs.user.uid = :uid AND fs.status = 'COMPLETED'")
    long sumElapsedSecondsByUserUid(String uid);

    Optional<FocusSession> findByUserUidAndStatus(String userUid, FocusStatus focusStatus);

    @Query("""
    SELECT f.id, f.course.name, f.focusStart, f.focusEnd, f.elapsedSeconds
    FROM FocusSession f
    WHERE f.user.uid = :userUid 
      AND f.status = :status 
      AND f.focusStart BETWEEN :start AND :end
    ORDER BY f.focusStart ASC
""")
    List<Object[]> findCompletedSessionsWithTimes(
            @Param("userUid") String userUid,
            @Param("status") FocusStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}