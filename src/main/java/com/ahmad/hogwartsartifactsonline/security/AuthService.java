package com.ahmad.hogwartsartifactsonline.security;

import com.ahmad.hogwartsartifactsonline.hogwartsuser.MyUserPrincipal;
import com.ahmad.hogwartsartifactsonline.hogwartsuser.converter.UserToUserDtoConverter;
import com.ahmad.hogwartsartifactsonline.hogwartsuser.dto.UserDto;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final JwtProvider jwtProvider;

    private final UserToUserDtoConverter userToUserDtoConverter;

    public AuthService(JwtProvider jwtProvider, UserToUserDtoConverter userToUserDtoConverter) {
        this.jwtProvider = jwtProvider;
        this.userToUserDtoConverter = userToUserDtoConverter;
    }

    public Map<String, Object> createLoginInfo(Authentication authentication) {
        MyUserPrincipal principal = (MyUserPrincipal) authentication.getPrincipal();
        UserDto userDto = userToUserDtoConverter.convert(principal.getHogwartsUser());

        String token = jwtProvider.createToken(authentication);

        Map<String, Object> loginResultMap = new HashMap<>();
        loginResultMap.put("userInfo", userDto);
        loginResultMap.put("token", token);

        return loginResultMap;
    }
}
