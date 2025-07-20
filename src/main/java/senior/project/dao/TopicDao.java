package senior.project.dao;

import senior.project.entity.Course;
import senior.project.entity.Topic;

import java.util.List;

public interface TopicDao {
    Topic save(Topic topic);
    void saveAll(List<Topic> topics);
    List<Topic> findByCourse(Course course);
    Topic findById(String topicId);
    void deleteByCourse(Course existingCourse);
    boolean existsById(String id);
    void deleteById(String existingId);
}
