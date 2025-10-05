package senior.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import senior.project.entity.User;
import senior.project.entity.plan.Schedule;
import senior.project.entity.plan.Session;
import senior.project.enums.FocusStatus;

import java.time.LocalDate;
import java.util.List;

public interface SessionRepository extends JpaRepository<Session, String> {
    List<Session> findBySchedule_UserAndDate(User user, LocalDate date);
    List<Session> findBySchedule_UserAndDateAfter(User user, LocalDate date);
    int countBySchedule_User_Uid(String uid);
    int countBySchedule_User_UidAndFocusSessions_Status(String uid, FocusStatus status);

    @Query("""
        SELECT s FROM Session s
        WHERE s.schedule.user = :user
          AND s.date = CURRENT_DATE
          AND (
            s.isCompleted = false
            AND FUNCTION('TIME', s.start) >= CURRENT_TIME
          )
    """)
    List<Session> findTodayActiveSessions(@Param("user") User user);

    @Query("""
        SELECT s FROM Session s
        WHERE s.schedule.user = :user
          AND s.isCompleted = false
          AND (
            s.date < CURRENT_DATE
            OR (s.date = CURRENT_DATE AND FUNCTION('TIME', s.start) < CURRENT_TIME)
          )
    """)
    List<Session> findOverdueSessions(@Param("user") User user);

    @Query("SELECT s FROM Session s WHERE s.schedule.user = :user AND s.isCompleted = true")
    List<Session> getCompletedSessions(@Param("user") User user);
}
