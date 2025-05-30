package senior.project.service;

import senior.project.entity.Topic;

import java.util.List;

public interface TopicService {
    void save(Topic topic);
    void saveAll(List<Topic> topics);
}
