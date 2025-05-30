package senior.project.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import senior.project.dao.TopicDao;
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
}
