package senior.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import senior.project.entity.GroupMember;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
}
