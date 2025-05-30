package senior.project.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import senior.project.dao.impl.TopicDaoImpl;
import senior.project.entity.Topic;
import senior.project.service.TopicService;

import java.util.List;


@Service
@RequiredArgsConstructor
public class TopicServiceImpl implements TopicService {

    private final TopicDaoImpl topicDao;

    @Override
    public void save(Topic topic) {
        topicDao.save(topic);
    }

    @Override
    public void saveAll(List<Topic> topics) {
        topicDao.saveAll(topics);
    }
}
