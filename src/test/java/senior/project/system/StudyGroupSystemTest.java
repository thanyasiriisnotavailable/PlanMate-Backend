package senior.project.system;

import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.containsString;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.google.firebase.auth.FirebaseAuth;
import senior.project.dao.StudyGroupDao;
import senior.project.util.SecurityUtil;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class StudyGroupSystemTest {

    private static final String TEST_UID = "user123";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private FirebaseAuth firebaseAuth;

    @MockitoBean
    private StudyGroupDao studyGroupDao;

    // ========== STC-11-TC-01 ==========
    @Test
    void STC_11_TC_01_createGroup_valid_shouldReturnJoinCode() throws Exception {
        String payload = """
        {"groupName":"Math Final","imageUrl":"math.png"}
        """;

        try (MockedStatic<SecurityUtil> mockedUtil = Mockito.mockStatic(SecurityUtil.class)) {
            mockedUtil.when(SecurityUtil::getAuthenticatedUid).thenReturn(TEST_UID);

            mvc.perform(post("/groups")
                            .with(user(TEST_UID))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isOk())
                    .andExpect(content().string(matchesPattern("^[A-Z0-9]{6}$")));
        }
    }

    // ========== STC-11-TC-02 ==========
    @Test
    void STC_11_TC_02_createGroup_emptyName_shouldReturn400() throws Exception {
        String payload = """
                {"groupName":"","imageUrl":"math.png"}
                """;

        try (MockedStatic<SecurityUtil> sec = Mockito.mockStatic(SecurityUtil.class)) {
            sec.when(SecurityUtil::getAuthenticatedUid).thenReturn(TEST_UID);

            mvc.perform(post("/groups")
                            .with(user(TEST_UID))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Group name is required.")));
        }
    }

    // ========== STC-11-TC-03 ==========
    @Test
    void STC_11_TC_03_createGroup_longName_shouldReturn400() throws Exception {
        String longName = "A".repeat(51);
        String payload = String.format("{\"groupName\":\"%s\",\"imageUrl\":\"math.png\"}", longName);

        try (MockedStatic<SecurityUtil> sec = Mockito.mockStatic(SecurityUtil.class)) {
            sec.when(SecurityUtil::getAuthenticatedUid).thenReturn(TEST_UID);

            mvc.perform(post("/groups")
                            .with(user(TEST_UID))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Group name must be less than 50 characters.")));
        }
    }

    // ========== STC-11-TC-04 ==========
    @Test
    void STC_11_TC_04_createGroup_nullImage_shouldReturnOk() throws Exception {
        String payload = """
                {"groupName":"Final Review","imageUrl":null}
                """;

        try (MockedStatic<SecurityUtil> sec = Mockito.mockStatic(SecurityUtil.class)) {
            sec.when(SecurityUtil::getAuthenticatedUid).thenReturn(TEST_UID);

            mvc.perform(post("/groups")
                            .with(user(TEST_UID))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isOk())
                    .andExpect(content().string(matchesPattern("^[A-Z0-9]{6}$")));
        }
    }

    // ========== STC-11-TC-05 ==========
    @Test
    void STC_11_TC_05_createGroup_dbError_shouldReturn500() throws Exception {
        String payload = """
                {"groupName":"Math Final","imageUrl":"math.png"}
                """;

        try (MockedStatic<SecurityUtil> sec = Mockito.mockStatic(SecurityUtil.class)) {
            sec.when(SecurityUtil::getAuthenticatedUid).thenReturn(TEST_UID);

            doThrow(new RuntimeException("DB error"))
                    .when(studyGroupDao).save(Mockito.any());

            mvc.perform(post("/groups")
                            .with(user(TEST_UID))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().string(containsString("Group creation failed")));
        }
    }
}