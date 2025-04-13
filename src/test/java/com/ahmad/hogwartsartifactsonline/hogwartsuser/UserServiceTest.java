package com.ahmad.hogwartsartifactsonline.hogwartsuser;

import com.ahmad.hogwartsartifactsonline.system.exception.ObjectNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles(value = "dev")
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserService userService;

    List<HogwartsUser> users = new ArrayList<>();

    @BeforeEach
    void setUp() {

        HogwartsUser user1 = new HogwartsUser();
        user1.setId(1);
        user1.setUsername("harry.potter");
        user1.setPassword("expelliarmus");
        user1.setEnabled(true);
        user1.setRoles("ROLE_STUDENT");
        users.add(user1);

        HogwartsUser user2 = new HogwartsUser();
        user2.setId(2);
        user2.setUsername("hermione.granger");
        user2.setPassword("leviosa");
        user2.setEnabled(true);
        user2.setRoles("ROLE_STUDENT ROLE_PREFECT");
        users.add(user2);

        HogwartsUser user3 = new HogwartsUser();
        user3.setId(3);
        user3.setUsername("albus.dumbledore");
        user3.setPassword("phoenix");
        user3.setEnabled(true);
        user3.setRoles("ROLE_HEADMASTER");
        users.add(user3);

    }

    @Test
    void testFindAllSuccess() {

        given(userRepository.findAll()).willReturn(users);

        List<HogwartsUser> foundUsers = userService.findAll();
        assertThat(foundUsers.size()).isEqualTo(users.size());

        Mockito.verify(userRepository, times(1)).findAll();
    }

    @Test
    void testFindByIdSuccess() {

        HogwartsUser user = new HogwartsUser();
        user.setId(1);
        user.setUsername("harry.potter");
        user.setPassword("expelliarmus");
        user.setEnabled(true);
        user.setRoles("ROLE_STUDENT");

        given(userRepository.findById(1)).willReturn(Optional.of(user));

        HogwartsUser hogwartsFoundUser = userService.findById(1);

        assertThat(hogwartsFoundUser.getUsername()).isEqualTo(user.getUsername());
        assertThat(hogwartsFoundUser.getPassword()).isEqualTo(user.getPassword());
        assertThat(hogwartsFoundUser.isEnabled()).isEqualTo(user.isEnabled());
        assertThat(hogwartsFoundUser.getRoles()).isEqualTo(user.getRoles());

        verify(userRepository, times(1)).findById(1);
    }

    @Test
    void testFindByIdNotFound() {

        given(userRepository.findById(Mockito.any(Integer.class))).willReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> {
            userService.findById(1);
        });

        assertThat(thrown)
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessage("Could not find user With Id 1 :(");

        verify(userRepository, times(1)).findById(1);

    }

    @Test
    void testUpdateByIdSuccess() {
        HogwartsUser oldUser = new HogwartsUser();
        oldUser.setId(1);
        oldUser.setUsername("hermione.granger");
        oldUser.setPassword("leviosa");
        oldUser.setEnabled(true);
        oldUser.setRoles("ROLE_STUDENT ROLE_PREFECT");

        HogwartsUser update = new HogwartsUser();
        update.setUsername("hermione.granger-update");
        update.setPassword("leviosa");
        update.setEnabled(true);
        update.setRoles("ROLE_STUDENT ROLE_PREFECT");

        given(userRepository.findById(1)).willReturn(Optional.of(oldUser));
        given(userRepository.save(oldUser)).willReturn(oldUser);

        HogwartsUser savedUser = userService.update(1, update);
        assertThat(savedUser.getUsername()).isEqualTo(update.getUsername());

        verify(userRepository, times(1)).findById(1);
        verify(userRepository, times(1)).save(oldUser);


    }

    @Test
    void testUpdateByIdNoFound() {

        given(userRepository.findById(Mockito.any(Integer.class))).willThrow(new ObjectNotFoundException("user", 4));

        Throwable thrown = catchThrowable(() -> {
            userService.update(4, Mockito.any(HogwartsUser.class));
        });

        assertThat(thrown)
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessage("Could not find user With Id 4 :(");

        verify(userRepository, times(1)).findById(4);
    }

    @Test
    void testDeleteByIdNotFound() {
        given(userRepository.findById(4)).willReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> {
            userService.delete(4);
        });
        assertThat(thrown)
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessage("Could not find user With Id 4 :(");
        verify(userRepository, times(1)).findById(4);

    }

    @Test
    void testDeleteByIdSuccess() {

        HogwartsUser user = new HogwartsUser();
        user.setId(1);
        user.setUsername("john");
        user.setPassword("123456");
        user.setEnabled(true);
        user.setRoles("admin user");

        given(userRepository.findById(1)).willReturn(Optional.of(user));
        doNothing().when(userRepository).deleteById(1);


        userService.delete(1);

        verify(userRepository, times(1)).findById(1);
        verify(userRepository, times(1)).deleteById(1);
    }

    @Test
    void testSaveSuccess() {

        HogwartsUser newUser = new HogwartsUser();
        newUser.setUsername("lily");
        newUser.setPassword("123456");
        newUser.setEnabled(true);
        newUser.setRoles("user");

        given(passwordEncoder.encode(newUser.getPassword())).willReturn("Encoded Password");
        given(userRepository.save(newUser)).willReturn(newUser);

        HogwartsUser returnedUser = userService.save(newUser);
        assertThat(returnedUser.getUsername()).isEqualTo(newUser.getUsername());
        assertThat(returnedUser.getPassword()).isEqualTo(newUser.getPassword());
        assertThat(returnedUser.isEnabled()).isEqualTo(newUser.isEnabled());
        assertThat(returnedUser.getRoles()).isEqualTo(newUser.getRoles());
        verify(userRepository, times(1)).save(newUser);


    }

}