package senior.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import senior.project.entity.Term;
import senior.project.entity.User;

import java.time.LocalDate;
import java.util.*;

public interface TermRepository extends JpaRepository<Term, Long> {
    Term findByUser(User user);

    @Query("SELECT t FROM Term t WHERE t.user = :user AND CURRENT_DATE BETWEEN t.startDate AND t.endDate")
    Optional<Term> getCurrentTermByUser(@Param("user") User user);
}
