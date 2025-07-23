package senior.project.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import senior.project.dao.StudyGroupDao;
import senior.project.dto.GroupRequestDTO;
import senior.project.entity.StudyGroup;
import senior.project.service.StudyGroupService;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class StudyGroupServiceImpl implements StudyGroupService {

    private final StudyGroupDao studyGroupDao;

    private static final String JOIN_CODE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int JOIN_CODE_LENGTH = 6;
    private final SecureRandom random = new SecureRandom();

    @Override
    public ResponseEntity<?> createGroup(GroupRequestDTO groupInfo) {
        String name = groupInfo.getGroupName();
        String image = groupInfo.getImageUrl();

        if (name == null || name.trim().isEmpty() || name.length() > 50) {
            return ResponseEntity.badRequest().body("Group name is required and must be under 50 characters.");
        }

        String joinCode = generateJoinCode();

        StudyGroup group = StudyGroup.builder()
                .name(name)
                .imageUrl(image)
                .joinCode(joinCode)
                .build();

        try {
            studyGroupDao.save(group);
            return ResponseEntity.ok(joinCode);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Group creation failed. Please try again later.");
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
        } while (studyGroupDao.existsByJoinCode(code)); // Add this DAO method
        return code;
    }
}
