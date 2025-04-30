package com.ahmad.hogwartsartifactsonline.hogwartsuser;

import com.ahmad.hogwartsartifactsonline.hogwartsuser.dto.UserDto;
import com.ahmad.hogwartsartifactsonline.system.StatusCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis.testcontainers.RedisContainer;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@DisplayName("Integration tests for User API endpoints")
@Tag("integration")
@ActiveProfiles(value = "dev")
class HogwartsUserControllerIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @Value("${api.endpoint.base-url}")
    String baseUrl;

    String token;

    @Container
    @ServiceConnection
    static RedisContainer redisContainer = new RedisContainer(DockerImageName.parse("redis"));

    @BeforeEach
    void setUp() throws Exception {
        // User john has all permissions.
        ResultActions resultActions = mvc.perform(post(this.baseUrl + "/users/login").with(httpBasic("john", "123456"))); // httpBasic() is from spring-security-test.
        MvcResult mvcResult = resultActions.andDo(print()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        JSONObject json = new JSONObject(contentAsString);
        token = "Bearer " + json.getJSONObject("data").getString("token");

    }

    @Test
    @DisplayName("Check findAllUsers (GET)")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testFindAllUsersSuccess() throws Exception {
        mvc.perform(
                        get(baseUrl + "/users")
                                .accept(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, token)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find All Users Success"))
                .andExpect(jsonPath("$.data", Matchers.hasSize(3)));
    }

    @Test
    @DisplayName("Check findUserById (GET): User with ROLE_admin Accessing any User's Info")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testFindUserByIdWithAdminAccessingAnyUserInfo() throws Exception {
        mvc.perform(
                        get(baseUrl + "/users/2")
                                .accept(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, token)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find User By Id Success"))
                .andExpect(jsonPath("$.data.username").value("eric"));
    }

    @Test
    @DisplayName("Check findUserById (GET): User with ROLE_user Accessing own Info")
    void testFindUserByIdWithUserAccessingOwnInfo() throws Exception {

        ResultActions resultActions = mvc.perform(post(baseUrl + "/users/login").with(httpBasic("eric", "654321")));
        MvcResult mvcResult = resultActions.andDo(print()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        JSONObject json = new JSONObject(contentAsString);
        String ericToken = "Bearer " + json.getJSONObject("data").getString("token");

        mvc.perform(
                        get(baseUrl + "/users/2")
                                .accept(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, ericToken)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find User By Id Success"))
                .andExpect(jsonPath("$.data.username").value("eric"));
    }

    @Test
    @DisplayName("Check findUserById (GET): User with ROLE_user Accessing Another Users Info")
    void testFindUserByIdWithUserAccessingAnotherUserSInfo() throws Exception {

        ResultActions resultActions = mvc.perform(post(baseUrl + "/users/login").with(httpBasic("eric", "654321")));
        MvcResult mvcResult = resultActions.andDo(print()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        JSONObject json = new JSONObject(contentAsString);
        String ericToken = "Bearer " + json.getJSONObject("data").getString("token");

        mvc.perform(
                        get(baseUrl + "/users/1")
                                .accept(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, ericToken)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No Permission."))
                .andExpect(jsonPath("$.data").value("Access Denied"));
    }


    @Test
    @DisplayName("Check findUserById with non-existent id (GET)")
    void testFindUserByIdNotFound() throws Exception {
        mvc.perform(
                        get(baseUrl + "/users/400")
                                .accept(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, token)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find user With Id 400 :("))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("Check addUser with valid input (POST)")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testAddUserErrorWithValidInput() throws Exception {

        HogwartsUser hogwartsUser = new HogwartsUser();
        hogwartsUser.setUsername("lily");
        hogwartsUser.setPassword("123789");
        hogwartsUser.setEnabled(true);
        hogwartsUser.setRoles("admin user");

        String json = objectMapper.writeValueAsString(hogwartsUser);
        mvc.perform(
                        post(baseUrl + "/users")
                                .accept(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Add Success"))
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.username").value("lily"))
                .andExpect(jsonPath("$.data.enabled").value(true))
                .andExpect(jsonPath("$.data.roles").value("admin user"));

        mvc.perform(
                        get(baseUrl + "/users")
                                .accept(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, token)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find All Users Success"))
                .andExpect(jsonPath("$.data", Matchers.hasSize(4)));
    }

    @Test
    @DisplayName("Check addUser with invalid input (POST)")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testAddUserWithInvalidInput() throws Exception {
        HogwartsUser hogwartsUser = new HogwartsUser();
        hogwartsUser.setUsername("");
        hogwartsUser.setPassword("");
        hogwartsUser.setEnabled(true);
        hogwartsUser.setRoles("");

        String json = objectMapper.writeValueAsString(hogwartsUser);
        mvc.perform(
                        post(baseUrl + "/users")
                                .accept(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value("Provided arguments are invalid, see data for details."))
                .andExpect(jsonPath("$.data.username").value("username is required"))
                .andExpect(jsonPath("$.data.password").value("password is required."))
                .andExpect(jsonPath("$.data.roles").value("roles are required."));

        mvc.perform(
                        get(baseUrl + "/users")
                                .accept(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, token)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find All Users Success"))
                .andExpect(jsonPath("$.data", Matchers.hasSize(3)));
    }

    @Test
    @DisplayName("Check updateUser with valid input (PUT): User with ROLE_admin Updating Any User's Info")
    void testUpdateUserWithAdminUpdatingAnyUsersInfo() throws Exception {
        UserDto update = new UserDto(null, "tom123", false, "user");
        String json = objectMapper.writeValueAsString(update);

        mvc.perform(
                        put(baseUrl + "/users/3")
                                .accept(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update User Success"))
                .andExpect(jsonPath("$.data.username").value("tom123"))
                .andExpect(jsonPath("$.data.enabled").value(false))
                .andExpect(jsonPath("$.data.roles").value("user"));
    }

    @Test
    @DisplayName("Check updateUser with valid input (PUT): User with ROLE_user Updating Own Info")
    void testUpdateUserWithUserUpdatingOwnInfo() throws Exception {

        ResultActions resultActions = mvc.perform(post(baseUrl + "/users/login").with(httpBasic("eric", "654321")));
        MvcResult mvcResult = resultActions.andDo(print()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        JSONObject json = new JSONObject(contentAsString);
        String ericToken = "Bearer " + json.getJSONObject("data").getString("token");

        UserDto update = new UserDto(null, "eric123", true, "user");
        String updatedJson = objectMapper.writeValueAsString(update);

        mvc.perform(
                        put(baseUrl + "/users/2")
                                .accept(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, ericToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updatedJson)
                )
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update User Success"))
                .andExpect(jsonPath("$.data.username").value("eric123"))
                .andExpect(jsonPath("$.data.enabled").value(true))
                .andExpect(jsonPath("$.data.roles").value("user"));
    }

    @Test
    @DisplayName("Check updateUser with valid input (PUT): User with ROLE_user Updating Another Users Info")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testUpdateUserWithUserUpdatingAnotherUsersInfo() throws Exception {
        ResultActions resultActions = mvc.perform(post(baseUrl + "/users/login").with(httpBasic("eric", "654321")));
        MvcResult mvcResult = resultActions.andDo(print()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        JSONObject json = new JSONObject(contentAsString);
        String ericToken = "Bearer " + json.getJSONObject("data").getString("token");

        UserDto update = new UserDto(null, "tom123", false, "user");
        String updatedJson = objectMapper.writeValueAsString(update);

        mvc.perform(
                        put(baseUrl + "/users/3")
                                .accept(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, ericToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updatedJson)
                )
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No Permission."))
                .andExpect(jsonPath("$.data").value("Access Denied"));
    }


    @Test
    @DisplayName("Check updateUser with non-existent id (PUT)")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testUpdateUserErrorWithNonExistentId() throws Exception {
        UserDto update = new UserDto(null, "john123", true, "user");
        String json = objectMapper.writeValueAsString(update);

        mvc.perform(
                        put(baseUrl + "/users/8")
                                .accept(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, token)
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find user With Id 8 :("))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("Check deleteUser with valid input (DELETE)")
    void testDeleteUserSuccess() throws Exception {
        mvc.perform(
                        delete(baseUrl + "/users/2")
                                .accept(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, token)
                                .contentType(MediaType.APPLICATION_JSON)

                )
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Delete User Success"))
                .andExpect(jsonPath("$.data").isEmpty());

        mvc.perform(get(baseUrl + "/users/2").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, token).contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find user With Id 2 :("))
                .andExpect(jsonPath("$.data").isEmpty());

    }

    @Test
    @DisplayName("Check deleteUser with non-existent id (DELETE)")
    void testDeleteUserErrorWithNonExistentId() throws Exception {
        mvc.perform(delete(baseUrl + "/users/5").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find user With Id 5 :("))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("Check deleteUser with insufficient permission (DELETE)")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testDeleteUserNoAccessRoleUser() throws Exception {

        ResultActions resultActions = mvc.perform(post(baseUrl + "/users/login").with(httpBasic("eric", "654321")));
        MvcResult mvcResult = resultActions.andDo(print()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        JSONObject json = new JSONObject(contentAsString);
        String ericToken = "Bearer " + json.getJSONObject("data").getString("token");

        mvc.perform(delete(baseUrl + "/users/2").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, ericToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No Permission."))
                .andExpect(jsonPath("$.data").value("Access Denied"));

        mvc.perform(get(baseUrl + "/users").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find All Users Success"))
                .andExpect(jsonPath("$.data", Matchers.hasSize(3)))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].username").value("john"));

    }


    @Test
    @DisplayName("Check changeUserPassword with valid input (PATCH)")
    void testChangeUserPasswordSuccess() throws Exception {

        ResultActions resultActions = mvc.perform(post(baseUrl + "/users/login").with(httpBasic("eric", "654321")));
        MvcResult mvcResult = resultActions.andDo(print()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        JSONObject json = new JSONObject(contentAsString);
        String ericToken = "Bearer " + json.getJSONObject("data").getString("token");

        // Given
        Map<String, String> passwordMap = new HashMap<>();
        passwordMap.put("oldPassword", "654321");
        passwordMap.put("newPassword", "Abc12345");
        passwordMap.put("confirmNewPassword", "Abc12345");

        String passwordMapJson = this.objectMapper.writeValueAsString(passwordMap);


        mvc.perform(patch(baseUrl + "/users/2/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(passwordMapJson).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, ericToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Password Changed Success"));


    }

    @Test
    @DisplayName("Check changeUserPassword with wrong old password (PATCH)")
    void testChangeUserPasswordWithWrongOldPassword() throws Exception {

        ResultActions resultActions = mvc.perform(post(baseUrl + "/users/login").with(httpBasic("eric", "654321")));
        MvcResult mvcResult = resultActions.andDo(print()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        JSONObject json = new JSONObject(contentAsString);
        String ericToken = "Bearer " + json.getJSONObject("data").getString("token");

        // Given
        Map<String, String> passwordMap = new HashMap<>();
        passwordMap.put("oldPassword", "123456");
        passwordMap.put("newPassword", "Abc12345");
        passwordMap.put("confirmNewPassword", "Abc12345");

        String passwordMapJson = this.objectMapper.writeValueAsString(passwordMap);


        mvc.perform(patch(baseUrl + "/users/2/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(passwordMapJson).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, ericToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("username or password is incorrect."))
                .andExpect(jsonPath("$.data").value("Old password is incorrect"));


    }

    @Test
    @DisplayName("Check changeUserPassword with new password not matching confirm new password (PATCH)")
    void testChangeUserPasswordWithNewPasswordNotMatchingConfirmPassword() throws Exception {

        ResultActions resultActions = mvc.perform(post(baseUrl + "/users/login").with(httpBasic("eric", "654321")));
        MvcResult mvcResult = resultActions.andDo(print()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        JSONObject json = new JSONObject(contentAsString);
        String ericToken = "Bearer " + json.getJSONObject("data").getString("token");

        // Given
        Map<String, String> passwordMap = new HashMap<>();
        passwordMap.put("oldPassword", "654321");
        passwordMap.put("newPassword", "Abc123456");
        passwordMap.put("confirmNewPassword", "Abc12345");

        String passwordMapJson = this.objectMapper.writeValueAsString(passwordMap);


        mvc.perform(patch(baseUrl + "/users/2/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(passwordMapJson).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, ericToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value("New password and confirm new password do not match."));


    }

    @Test
    @DisplayName("Check changeUserPassword with new password not conforming to password policy (PATCH)")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testChangeUserPasswordWithNewPasswordNotConformingToPasswordPolicy() throws Exception {

        ResultActions resultActions = mvc.perform(post(baseUrl + "/users/login").with(httpBasic("eric", "654321")));
        MvcResult mvcResult = resultActions.andDo(print()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        JSONObject json = new JSONObject(contentAsString);
        String ericToken = "Bearer " + json.getJSONObject("data").getString("token");

        // Given
        Map<String, String> passwordMap = new HashMap<>();
        passwordMap.put("oldPassword", "654321");
        passwordMap.put("newPassword", "short");
        passwordMap.put("confirmNewPassword", "short");

        String passwordMapJson = this.objectMapper.writeValueAsString(passwordMap);


        mvc.perform(patch(baseUrl + "/users/2/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(passwordMapJson).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, ericToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value("New password does not conform to password policy."));


    }


}