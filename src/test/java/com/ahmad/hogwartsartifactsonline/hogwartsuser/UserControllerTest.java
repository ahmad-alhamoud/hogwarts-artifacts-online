package com.ahmad.hogwartsartifactsonline.hogwartsuser;

import com.ahmad.hogwartsartifactsonline.hogwartsuser.dto.UserDto;
import com.ahmad.hogwartsartifactsonline.system.StatusCode;
import com.ahmad.hogwartsartifactsonline.system.exception.ObjectNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(value = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @MockitoBean
    UserService userService;

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @Value("${api.endpoint.base-url}")
    String baseUrl;

    List<UserDto> userDtoList = new ArrayList<>();
    List<HogwartsUser> hogwartsUserList = new ArrayList<>();

    @BeforeEach
    void setUp() {

        userDtoList.add(new UserDto(1, "harry.potter", true, "ROLE_STUDENT"));
        userDtoList.add(new UserDto(2, "hermione.granger", true, "ROLE_STUDENT ROLE_PREFECT"));
        userDtoList.add(new UserDto(3, "albus.dumbledore", true, "ROLE_HEADMASTER"));


        HogwartsUser user1 = new HogwartsUser();
        user1.setId(1);
        user1.setUsername("harry.potter");
        user1.setPassword("expelliarmus"); // Assuming password is required
        user1.setEnabled(true);
        user1.setRoles("ROLE_STUDENT");
        hogwartsUserList.add(user1);

        HogwartsUser user2 = new HogwartsUser();
        user2.setId(2);
        user2.setUsername("hermione.granger");
        user2.setPassword("leviosa"); // Assuming password is required
        user2.setEnabled(true);
        user2.setRoles("ROLE_STUDENT ROLE_PREFECT");
        hogwartsUserList.add(user2);

        HogwartsUser user3 = new HogwartsUser();
        user3.setId(3);
        user3.setUsername("albus.dumbledore");
        user3.setPassword("phoenix"); // Assuming password is required
        user3.setEnabled(true);
        user3.setRoles("ROLE_HEADMASTER");
        hogwartsUserList.add(user3);
    }

    @Test
    void testNotNull() {
        assertThat(mvc).isNotNull();
    }

    @Test
    void testFindAllUsersSuccess() throws Exception {

        given(userService.findAll()).willReturn(hogwartsUserList);

        mvc.perform(
                        get(baseUrl + "/users")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find All Users Success"))
                .andExpect(jsonPath("$.data[0].id").value(userDtoList.get(0).id()))
                .andExpect(jsonPath("$.data[0].username").value(userDtoList.get(0).username()))
                .andExpect(jsonPath("$.data[1].id").value(userDtoList.get(1).id()))
                .andExpect(jsonPath("$.data[1].username").value(userDtoList.get(1).username()));

    }

    @Test
    void testFindUserByIdSuccess() throws Exception {

        UserDto userDto = new UserDto(1, "harry.potter", true, "ROLE_STUDENT");
        HogwartsUser user = new HogwartsUser();
        user.setId(1);
        user.setUsername("harry.potter");
        user.setPassword("expelliarmus"); // Assuming password is required
        user.setEnabled(true);
        user.setRoles("ROLE_STUDENT");

        given(userService.findById(1)).willReturn(user);

        mvc.perform(
                        get(baseUrl + "/users/1")
                                .accept(MediaType.APPLICATION_JSON)
                )
                // .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find User By Id Success"))
                .andExpect(jsonPath("$.data.id").value(userDto.id()))
                .andExpect(jsonPath("$.data.username").value(userDto.username()))
                .andExpect(jsonPath("$.data.enabled").value(userDto.enabled()))
                .andExpect(jsonPath("$.data.roles").value(userDto.roles()));
    }

    @Test
    void testFindUserErrorWithNotExistentId() throws Exception {

        given(userService.findById(Mockito.any(Integer.class))).willThrow(new ObjectNotFoundException("user", 1));

        mvc.perform(
                        get(baseUrl + "/users/1")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find user With Id 1 :("))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testUpdateUserByIdSuccess() throws Exception {


        HogwartsUser update = new HogwartsUser();
        update.setId(2);
        update.setUsername("hermione.granger-update");
        update.setPassword("leviosa");
        update.setEnabled(true);
        update.setRoles("ROLE_STUDENT");
        UserDto userDto = new UserDto(2, "hermione.granger-update", true, "ROLE_STUDENT");

        String json = objectMapper.writeValueAsString(userDto);

        given(userService.update(eq(2), Mockito.any(HogwartsUser.class))).willReturn(update);

        mvc.perform(
                        put(baseUrl + "/users/2")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update User Success"))
                .andExpect(jsonPath("$.data.username").value(update.getUsername()))
                .andExpect(jsonPath("$.data.roles").value(update.getRoles()));

    }

    @Test
    void testUpdateUserErrorWithNotExistentId() throws Exception {

        UserDto userDto = new UserDto(4, "hermione.granger-update", true, "ROLE_STUDENT");

        String json = objectMapper.writeValueAsString(userDto);

        given(userService.update(eq(4), Mockito.any(HogwartsUser.class))).willThrow(new ObjectNotFoundException("user", 4));

        mvc.perform(
                        put(baseUrl + "/users/4")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find user With Id 4 :("))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testDeleteUserByIdSuccess() throws Exception {

        doNothing().when(userService).delete(2);

        mvc.perform(
                        delete(baseUrl + "/users/2")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Delete User Success"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testDeleteUserErrorWithNotExistentId() throws Exception {

        doThrow(new ObjectNotFoundException("user", 4)).when(userService).delete(Mockito.any(Integer.class));
        mvc.perform(
                        delete(baseUrl + "/users/4")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find user With Id 4 :("))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testAddUserSuccess() throws Exception {
        HogwartsUser user = new HogwartsUser();
        user.setId(4);
        user.setUsername("lily");
        user.setPassword("123456");
        user.setEnabled(true);
        user.setRoles("admin user"); // The delimiter is space.

        String json = this.objectMapper.writeValueAsString(user);

        user.setId(4);

        // Given. Arrange inputs and targets. Define the behavior of Mock object userService.
        given(this.userService.save(Mockito.any(HogwartsUser.class))).willReturn(user);

        // When and then
        mvc.perform(post(this.baseUrl + "/users").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Add Success"))
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.username").value("lily"))
                .andExpect(jsonPath("$.data.enabled").value(true))
                .andExpect(jsonPath("$.data.roles").value("admin user"));
    }
}