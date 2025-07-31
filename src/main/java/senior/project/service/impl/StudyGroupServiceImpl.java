package senior.project.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import senior.project.dao.GroupMemberDao;
import senior.project.dao.StudyGroupDao;
import senior.project.dao.UserDao;
import senior.project.dto.GroupMemberDTO;
import senior.project.dto.GroupRequestDTO;
import senior.project.dto.JoinGroupRequestDTO;
import senior.project.dto.StudyGroupResponseDTO;
import senior.project.entity.GroupMember;
import senior.project.entity.StudyGroup;
import senior.project.entity.User;
import senior.project.service.StudyGroupService;
import senior.project.util.SecurityUtil;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudyGroupServiceImpl implements StudyGroupService {

    private final StudyGroupDao studyGroupDao;
    private final GroupMemberDao groupMemberDao;
    private final UserDao userDao;

    private static final String JOIN_CODE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int JOIN_CODE_LENGTH = 6;
    private final SecureRandom random = new SecureRandom();

    @Override
    public StudyGroupResponseDTO getGroups() {
        String userUid = SecurityUtil.getAuthenticatedUid();
        User user = userDao.findByUid(userUid);

        // Get all group memberships for the user
        List<GroupMember> groupMemberships = groupMemberDao.findByUser(user);

        if (groupMemberships.isEmpty()) {
            return null; // No groups found
        }

        // For simplicity, returning the first group only (as per controller definition)
        GroupMember firstMembership = groupMemberships.get(0);
        StudyGroup group = firstMembership.getGroup();

        StudyGroupResponseDTO responseDTO = new StudyGroupResponseDTO();
        responseDTO.setId(group.getId());
        responseDTO.setName(group.getName());
        responseDTO.setImageUrl(group.getImageUrl());
        responseDTO.setJoinCode(group.getJoinCode());

        List<GroupMemberDTO> memberDTOs = group.getMembers().stream().map(member -> {
            GroupMemberDTO dto = new GroupMemberDTO();
            dto.setId(member.getId());
            dto.setUser(member.getUser());
            return dto;
        }).toList();

        responseDTO.setMembers(memberDTOs);
        return responseDTO;
    }

    @Override
    public ResponseEntity<?> createGroup(GroupRequestDTO groupInfo) {
        String name = groupInfo.getGroupName();
        String image = groupInfo.getImageUrl();

        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Group name is required.");
        }
        if (name.length() > 50) {
            return ResponseEntity.badRequest().body("Group name must be under 50 characters.");
        }

        String joinCode = generateUniqueJoinCode();

        StudyGroup group = StudyGroup.builder()
                .name(name)
                .imageUrl(image)
                .joinCode(joinCode)
                .build();

        try {
            studyGroupDao.save(group);

            // Get current authenticated user
            String userUid = SecurityUtil.getAuthenticatedUid();
            User user = userDao.findByUid(userUid);

            // Save creator as the first group member
            GroupMember groupMember = GroupMember.builder()
                    .user(user)
                    .group(group)
                    .build();
            groupMemberDao.save(groupMember);

            return ResponseEntity.ok(joinCode);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Group creation failed. Please try again later.");
        }
    }

    @Override
    public ResponseEntity<?> joinGroup(JoinGroupRequestDTO dto) {
        String joinCode = dto.getJoinCode();

        if (joinCode == null || !joinCode.matches("[A-Z0-9]{6}")) {
            return ResponseEntity.badRequest().body("Invalid join code");
        }

        try {
            Optional<StudyGroup> optionalGroup = studyGroupDao.findByJoinCode(joinCode);
            if (optionalGroup.isEmpty()) {
                return ResponseEntity.badRequest().body("Invalid join code");
            }

            StudyGroup group = optionalGroup.get();

            String userUid = SecurityUtil.getAuthenticatedUid();
            User user = userDao.findByUid(userUid);

            if (user == null) {
                return ResponseEntity.badRequest().body("Invalid user");
            }

            // Check if user is already a member
            if (groupMemberDao.existsByUserAndGroup(user, group)) {
                return ResponseEntity.badRequest().body("You are already a member of this group.");
            }

            // Add user as member
            GroupMember groupMember = GroupMember.builder()
                    .user(user)
                    .group(group)
                    .build();
            groupMemberDao.save(groupMember);

            return ResponseEntity.ok("Joined group: " + group.getName());

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Network issue. Please try again.");
        }
    }

    private String generateJoinCode() {
        StringBuilder code = new StringBuilder(JOIN_CODE_LENGTH);
        for (int i = 0; i < JOIN_CODE_LENGTH; i++) {
            int index = random.nextInt(JOIN_CODE_CHARACTERS.length());
            code.append(JOIN_CODE_CHARACTERS.charAt(index));
        }
        return code.toString();
    }

    private String generateUniqueJoinCode() {
        String code;
        do {
            code = generateJoinCode();
        } while (studyGroupDao.existsByJoinCode(code));
        return code;
    }
}
