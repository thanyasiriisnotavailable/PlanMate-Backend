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

    @Query("SELECT t FROM Term t WHERE t.startDate <= :today AND t.endDate >= :today")
    Optional<Term> findCurrentTerm(@Param("today") LocalDate today);
}
