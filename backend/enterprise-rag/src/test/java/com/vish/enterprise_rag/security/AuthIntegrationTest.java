package com.vish.enterprise_rag.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vish.enterprise_rag.entities.Document;
import com.vish.enterprise_rag.entities.DocumentPermission;
import com.vish.enterprise_rag.entities.Organization;
import com.vish.enterprise_rag.entities.User;
import com.vish.enterprise_rag.enums.UserDesignation;
import com.vish.enterprise_rag.repositories.read.DocumentPermissionReadRepository;
import com.vish.enterprise_rag.repositories.read.DocumentReadRepository;
import com.vish.enterprise_rag.repositories.read.OrganizationReadRepository;
import com.vish.enterprise_rag.repositories.read.UserReadRepository;
import com.vish.enterprise_rag.repositories.write.DocumentPermissionWriteRepository;
import com.vish.enterprise_rag.repositories.write.DocumentWriteRepository;
import com.vish.enterprise_rag.repositories.write.OrganizationWriteRepository;
import com.vish.enterprise_rag.repositories.write.UserWriteRepository;
import com.vish.enterprise_rag.requests.LoginReq;
import com.vish.enterprise_rag.requests.SignupReq;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.vish.enterprise_rag.service.AuditService;

@SpringBootTest
public class AuthIntegrationTest {

    @MockitoBean
    private AuditService auditService;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private OrganizationWriteRepository organizationWriteRepository;

    @Autowired
    private OrganizationReadRepository organizationReadRepository;

    @Autowired
    private UserWriteRepository userWriteRepository;

    @Autowired
    private UserReadRepository userReadRepository;

    @Autowired
    private DocumentReadRepository documentReadRepository;

    @Autowired
    private DocumentWriteRepository documentWriteRepository;

    @Autowired
    private DocumentPermissionReadRepository documentPermissionReadRepository;

    @Autowired
    private DocumentPermissionWriteRepository documentPermissionWriteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Organization testOrg;
    private String signupCreatedEmail;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        testOrg = organizationReadRepository.findByContactEmailAndIsActiveTrue("auth-test@enterprise.com")
                .orElseGet(() -> {
                    Organization org = new Organization();
                    org.setName("Auth Test Org");
                    org.setContactEmail("auth-test@enterprise.com");
                    org.setContactPhone("1234567890");
                    return organizationWriteRepository.save(org);
                });
    }

    @AfterEach
    void tearDown() {
        try {
            documentPermissionWriteRepository.deleteAll();
            documentWriteRepository.deleteAll();

            if (testOrg != null && testOrg.getId() != null) {
                List<User> users = userReadRepository.findByOrganizationIdAndIsActiveTrue(testOrg.getId());
                userWriteRepository.deleteAll(users);
            }

            if (signupCreatedEmail != null) {
                userReadRepository.findByEmailAndIsActiveTrue(signupCreatedEmail).ifPresent(u -> {
                    Long orgId = u.getOrganization().getId();
                    userWriteRepository.delete(u);
                    organizationReadRepository.findByIdAndIsActiveTrue(orgId).ifPresent(organizationWriteRepository::delete);
                });
            }
        } catch (Exception ignored) {
        }
    }

    private String createTestUserAndGetToken(String emailPrefix, UserDesignation designation) throws Exception {
        String uniqueEmail = emailPrefix + "-" + System.currentTimeMillis() + "@enterprise.com";
        String rawPassword = "SecurePassword123!";

        User user = new User();
        user.setName("Test " + designation.name());
        user.setEmail(uniqueEmail);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setDesignation(designation);
        user.setOrganization(testOrg);
        userWriteRepository.save(user);

        LoginReq validLogin = new LoginReq(uniqueEmail, rawPassword);
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        return objectMapper.readTree(responseBody).get("data").get("token").asText();
    }

    @Test
    void testSignupFlow() throws Exception {
        String uniqueOrg = "Acme-" + System.currentTimeMillis();
        signupCreatedEmail = "admin-" + System.currentTimeMillis() + "@acme.com";

        SignupReq signupReq = new SignupReq(uniqueOrg, "Acme Admin", signupCreatedEmail, "SecurePassword123!", "9876543210");

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.user.email").value(signupCreatedEmail))
                .andExpect(jsonPath("$.data.user.designation").value("ADMIN"));
    }

    @Test
    void testUserCreationAndLoginFlow() throws Exception {
        String uniqueEmail = "user-" + System.currentTimeMillis() + "@enterprise.com";
        String rawPassword = "SecurePassword123!";

        // 1. Create a user
        User user = new User();
        user.setName("Test User");
        user.setEmail(uniqueEmail);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setDesignation(UserDesignation.EMPLOYEE);
        user.setOrganization(testOrg);
        user = userWriteRepository.save(user);

        assertNotNull(user.getId());
        assertTrue(passwordEncoder.matches(rawPassword, user.getPassword()));

        // 2. Attempt login with correct credentials
        LoginReq validLogin = new LoginReq(uniqueEmail, rawPassword);
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.user.email").value(uniqueEmail))
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseBody).get("data").get("token").asText();
        assertNotNull(token);

        // 3. Attempt login with invalid password
        LoginReq invalidLogin = new LoginReq(uniqueEmail, "WrongPassword!");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));

        // 4. Access protected endpoint without token -> Forbidden (403)
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isForbidden());

        // 5. Access protected endpoint with valid Bearer token -> OK (200)
        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true));
    }

    @Test
    void testDocumentUploadAndTenantScoping() throws Exception {
        String token = createTestUserAndGetToken("doc-uploader", UserDesignation.ADMIN);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-document-" + System.currentTimeMillis() + ".txt",
                "text/plain",
                ("Sample content for enterprise RAG document ingestion - " + System.currentTimeMillis()).getBytes()
        );

        // 1. Upload document with valid Bearer token
        MvcResult uploadResult = mockMvc.perform(multipart("/api/v1/documents/upload")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data.documentName").value(file.getOriginalFilename()))
                .andReturn();

        String uploadJson = uploadResult.getResponse().getContentAsString();
        long docId = objectMapper.readTree(uploadJson).get("data").get("id").asLong();

        // 2. Duplicate upload test (SHA-256 duplicate detection)
        mockMvc.perform(multipart("/api/v1/documents/upload")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.flag").value(false));

        // 3. Fetch documents for current tenant
        mockMvc.perform(get("/api/v1/documents")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data").isArray());

        // 4. Fetch specific document
        mockMvc.perform(get("/api/v1/documents/" + docId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data.id").value(docId));

        // 5. Delete document
        mockMvc.perform(delete("/api/v1/documents/" + docId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true));
    }
}
