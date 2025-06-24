package senior.project.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import senior.project.dao.TopicDao;
import senior.project.entity.Course;
import senior.project.entity.Topic;
import senior.project.repository.TopicRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TopicDaoImpl implements TopicDao {

    private final TopicRepository topicRepository;

    @Override
    public Topic save(Topic topic) {
        topicRepository.save(topic);
        return topic;
    }

    @Override
    public void saveAll(List<Topic> topics) {
        topicRepository.saveAll(topics);
    }

    @Override
    public List<Topic> findByCourse(Course course) {
        return topicRepository.findByCourse(course);
    }

    @Override
    public Topic findById(String topicId) {
        return topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));
    }

    @Override
    public void deleteByCourse(Course existingCourse) {
        topicRepository.deleteAll(topicRepository.findByCourse(existingCourse));
    }

    @Override
    public boolean existsById(String id) {
        return topicRepository.existsById(id);
    }
}
