package senior.project.dao;

import senior.project.entity.GroupMember;
import senior.project.entity.StudyGroup;
import senior.project.entity.User;

import java.util.List;

public interface GroupMemberDao {
    GroupMember save(GroupMember groupMember);
    Boolean existsByUserAndGroup(User user, StudyGroup group);
    List<GroupMember> findByUser(User user);
}
