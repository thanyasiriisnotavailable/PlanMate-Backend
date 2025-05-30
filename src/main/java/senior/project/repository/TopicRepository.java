package senior.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import senior.project.entity.Topic;

public interface TopicRepository extends JpaRepository<Topic, Long> {
}
