package com.ahmad.hogwartsartifactsonline.hogwartsuser.dto;

import jakarta.validation.constraints.NotEmpty;

public record UserDto(
        Integer id,

        @NotEmpty(message = "username are required.")
        String username,

        boolean enabled,

        @NotEmpty(message = "roles are required.")
        String roles

) {
}
