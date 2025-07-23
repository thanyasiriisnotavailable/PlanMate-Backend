package senior.project.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import senior.project.dao.GroupMemberDao;
import senior.project.entity.GroupMember;
import senior.project.entity.StudyGroup;
import senior.project.entity.User;
import senior.project.repository.GroupMemberRepository;

@Repository
@RequiredArgsConstructor
public class GroupMemberDaoImpl implements GroupMemberDao {
    private final GroupMemberRepository groupMemberRepository;

    @Override
    public GroupMember save(GroupMember groupMember) {
        return groupMemberRepository.save(groupMember);
    }

    @Override
    public Boolean existsByUserAndGroup(User user, StudyGroup group) {
        return groupMemberRepository.existsByUserAndGroup(user, group);
    }
}
