package com.ahmad.hogwartsartifactsonline.hogwartsuser;

import com.ahmad.hogwartsartifactsonline.hogwartsuser.dto.UserDto;
import com.ahmad.hogwartsartifactsonline.system.StatusCode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Integration tests for User API endpoints")
@Tag("integration")
class HogwartsUserControllerIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @Value("${api.endpoint.base-url}")
    String baseUrl;

    String token;

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
    @DisplayName("Check findUserById (GET)")
    void testFindUserByIdSuccess() throws Exception {
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
    @DisplayName("Check updateUser with valid input (PUT)")
    void testUpdateUserSuccess() throws Exception {
        UserDto update = new UserDto(null, "newName", false, "user");
        String json = objectMapper.writeValueAsString(update);

        mvc.perform(
                        put(baseUrl + "/users/1")
                                .accept(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update User Success"))
                .andExpect(jsonPath("$.data.username").value("newName"))
                .andExpect(jsonPath("$.data.enabled").value(false))
                .andExpect(jsonPath("$.data.roles").value("user"));
    }

    @Test
    @DisplayName("Check updateUser with non-existent id (PUT)")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testUpdateUserErrorWithNonExistentId() throws Exception {
        UserDto update = new UserDto(null,"john123",true,"user");
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
}