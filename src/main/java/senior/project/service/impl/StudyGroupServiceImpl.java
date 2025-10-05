package senior.project.service.impl;

import com.google.firebase.auth.FirebaseAuth;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import senior.project.dao.*;
import senior.project.dto.*;
import senior.project.entity.GroupMember;
import senior.project.entity.StudyGroup;
import senior.project.entity.User;
import senior.project.service.StudyGroupService;
import senior.project.util.SecurityUtil;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudyGroupServiceImpl implements StudyGroupService {

    private final StudyGroupDao studyGroupDao;
    private final GroupMemberDao groupMemberDao;
    private final SessionDao sessionDao;
    private final FocusSessionDao focusSessionDao;
    private final UserDao userDao;
    private final FirebaseAuth firebaseAuth;

    private static final String JOIN_CODE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int JOIN_CODE_LENGTH = 6;
    private final SecureRandom random = new SecureRandom();

    @Override
    public List<StudyGroupResponseDTO> getGroups() {
        String userUid = SecurityUtil.getAuthenticatedUid();
        User user = userDao.findByUid(userUid);

        List<GroupMember> groupMemberships = groupMemberDao.findByUser(user);

        return groupMemberships.stream().map(member -> {
            StudyGroup group = member.getGroup();
            StudyGroupResponseDTO dto = new StudyGroupResponseDTO();
            dto.setId(group.getId());
            dto.setName(group.getName());
            dto.setImageUrl(group.getImageUrl());
            dto.setJoinCode(group.getJoinCode());

            List<GroupMemberDTO> memberDTOs = group.getMembers().stream().map(m -> {
                GroupMemberDTO mdto = new GroupMemberDTO();
                mdto.setId(m.getId());
                mdto.setUser(m.getUser());
                return mdto;
            }).toList();
            dto.setMembers(memberDTOs);
            return dto;
        }).toList();
    }


    @Override
    public ResponseEntity<?> createGroup(GroupRequestDTO groupInfo) {
        String name = groupInfo.getGroupName();
        String image = groupInfo.getImageUrl();

        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Group name is required.");
        }
        if (name.length() > 50) {
            return ResponseEntity.badRequest().body("Group name must be less than 50 characters.");
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
    public ResponseEntity<?> joinGroup(String joinCode) {
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

    @Override
    public List<GroupMemberProgressDTO> getGroupProgress(Long groupId) {
        List<GroupMember> members = groupMemberDao.findByGroupId(groupId);
        if (members.isEmpty()) return List.of();

        int taskWeight = 10;
        int hourWeight = 5;

        List<GroupMemberProgressDTO> progressList = new ArrayList<>();

        for (GroupMember member : members) {
            User user = member.getUser();
            String uid = user.getUid();

            int totalPlannedSessions = sessionDao.countTotalPlannedSessionsForUser(uid);
            int completedSessions = sessionDao.countCompletedSessionsForUser(uid);

            double percentageCompleted = (totalPlannedSessions == 0)
                    ? 0.0
                    : ((double) completedSessions / totalPlannedSessions) * 100.0;

            long totalFocusSeconds = focusSessionDao.sumFocusSecondsByUser(uid);
            long focusHours = totalFocusSeconds / 3600;

            long points = (completedSessions * taskWeight) + (focusHours * hourWeight);

            // Fetch Firebase user info
            MemberProfileDTO profileDTO = new MemberProfileDTO();
            profileDTO.setMemberId(uid);

            try {
                var userRecord = firebaseAuth.getUser(uid);
                profileDTO.setDisplayName(userRecord.getDisplayName());
                profileDTO.setPhotoUrl(userRecord.getPhotoUrl());
            } catch (Exception e) {
                profileDTO.setDisplayName("Unknown");
                profileDTO.setPhotoUrl(null);
                // optionally log the exception here
            }

            progressList.add(GroupMemberProgressDTO.builder()
                    .member(profileDTO)   // set full MemberProfileDTO here
                    .completedSessions(completedSessions)
                    .totalFocusSeconds(totalFocusSeconds)
                    .percentageCompleted(Math.round(percentageCompleted * 100.0) / 100.0)
                    .points(points)
                    .build());
        }

        // sort by points descending
        progressList.sort((a, b) -> Long.compare(b.getPoints(), a.getPoints()));

        return progressList;
    }
}
