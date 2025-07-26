package senior.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import senior.project.entity.GroupMember;
import senior.project.entity.StudyGroup;
import senior.project.entity.User;

import java.util.List;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    Boolean existsByUserAndGroup(User user, StudyGroup group);
    List<GroupMember> findByUser(User user);
}
