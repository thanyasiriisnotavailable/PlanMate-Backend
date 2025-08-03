package senior.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
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
}
