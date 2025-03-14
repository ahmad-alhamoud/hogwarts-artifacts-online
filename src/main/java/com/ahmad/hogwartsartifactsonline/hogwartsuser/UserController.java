package com.ahmad.hogwartsartifactsonline.hogwartsuser;

import com.ahmad.hogwartsartifactsonline.hogwartsuser.converter.UserDtoToUserConverter;
import com.ahmad.hogwartsartifactsonline.hogwartsuser.converter.UserToUserDtoConverter;
import com.ahmad.hogwartsartifactsonline.hogwartsuser.dto.UserDto;
import com.ahmad.hogwartsartifactsonline.system.Result;
import com.ahmad.hogwartsartifactsonline.system.StatusCode;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.endpoint.base-url}/users")
public class UserController {

    private final UserService userService;
    private final UserToUserDtoConverter userToUserDtoConverter;
    private final UserDtoToUserConverter userDtoToUserConverter;

    public UserController(UserService userService, UserToUserDtoConverter userToUserDtoConverter, UserDtoToUserConverter userDtoToUserConverter) {
        this.userService = userService;
        this.userToUserDtoConverter = userToUserDtoConverter;
        this.userDtoToUserConverter = userDtoToUserConverter;
    }

    @GetMapping
    public Result findAllUsers() {
        List<HogwartsUser> hogwartsUsers = userService.findAll();
        List<UserDto> userDtos = hogwartsUsers
                .stream().map(userToUserDtoConverter::convert).toList();
        return new Result(true, StatusCode.SUCCESS, "Find All Users Success", userDtos);
    }

    @GetMapping("{userId}")
    public Result findUserById(@PathVariable Integer userId) {
        HogwartsUser foundUser = userService.findById(userId);
        UserDto userDto = userToUserDtoConverter.convert(foundUser);
        return new Result(true, StatusCode.SUCCESS, "Find User By Id Success", userDto);
    }

    @PutMapping("/{userId}")
    public Result updateUserById(
            @PathVariable Integer userId, @RequestBody @Valid UserDto update
    ) {
        HogwartsUser newUser = userDtoToUserConverter.convert(update);
        HogwartsUser savedUser = userService.update(userId, newUser);
        UserDto savedUserDto = userToUserDtoConverter.convert(savedUser);
        return new Result(true, StatusCode.SUCCESS, "Update User Success", savedUserDto);
    }

    @DeleteMapping("/{userId}")
    public Result deleteUserById(@PathVariable Integer userId) {
        userService.delete(userId);
        return new Result(true, StatusCode.SUCCESS, "Delete User Success");
    }

    @PostMapping
    public Result addUser(@RequestBody  @Valid HogwartsUser newHogwartsUser) {
        HogwartsUser savedUser = userService.save(newHogwartsUser);
        UserDto savedUserDto = userToUserDtoConverter.convert(savedUser);
        return new Result(true, StatusCode.SUCCESS, "Add Success", savedUserDto);
    }

}
