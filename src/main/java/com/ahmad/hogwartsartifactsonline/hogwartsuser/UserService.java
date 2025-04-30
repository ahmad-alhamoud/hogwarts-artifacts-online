package com.ahmad.hogwartsartifactsonline.hogwartsuser;

import com.ahmad.hogwartsartifactsonline.client.rediscache.RedisCacheClient;
import com.ahmad.hogwartsartifactsonline.system.exception.ObjectNotFoundException;
import com.ahmad.hogwartsartifactsonline.system.exception.PasswordChangeIllegalArgumentException;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Transactional
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisCacheClient redisCacheClient;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RedisCacheClient redisCacheClient) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.redisCacheClient = redisCacheClient;
    }


    public List<HogwartsUser> findAll() {
        return userRepository.findAll();
    }

    public HogwartsUser findById(Integer userId) {
        HogwartsUser hogwartsUser = userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("user", userId));
        return hogwartsUser;
    }

    public HogwartsUser update(Integer userId, HogwartsUser update) {

        HogwartsUser oldHogwartsUser = userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("user", userId));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // If the user is not an admin, then the user can only update his username
        if (authentication.getAuthorities().stream().noneMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))) {
            oldHogwartsUser.setUsername(update.getUsername());
        } else { // is the user is an admin, then the user can update all fields

            oldHogwartsUser.setUsername(update.getUsername());
            oldHogwartsUser.setEnabled(update.isEnabled());
            oldHogwartsUser.setRoles(update.getRoles());

            // Revoke this user's current JWT by deleting it from Redis.
            redisCacheClient.delete("whiteList:" + userId);
        }


        return userRepository.save(oldHogwartsUser);
    }

    public void delete(Integer userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("user", userId));

        userRepository.deleteById(userId);
    }

    public HogwartsUser save(HogwartsUser newHogwartsUser) {
        // WE Need to encode plain text password before saving to the DB!
        newHogwartsUser.setPassword(passwordEncoder.encode(newHogwartsUser.getPassword()));
        return userRepository.save(newHogwartsUser);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .map(hogwartsUser -> new MyUserPrincipal(hogwartsUser)) // If found, wrap the returned user instance in a MyUserPrincipal instance.
                .orElseThrow(() -> new UsernameNotFoundException("username" + username + " is not found."));
    }

    public void changePassword(Integer userId, String oldPassword, String newPassword, String confirmNewPassword) {
        HogwartsUser hogwartsUser = userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("user", userId));

        if (!passwordEncoder.matches(oldPassword, hogwartsUser.getPassword())) {
            throw new BadCredentialsException("Old password is incorrect");
        }

        if (!newPassword.equals(confirmNewPassword)) {
            throw new PasswordChangeIllegalArgumentException("New password and confirm new password do not match.");
        }

        // The new password must contain at least one digit, one lowercase letter, one uppercase letter, and be at least 8 characters long.
        String passwordPolicy = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$";
        if (!newPassword.matches(passwordPolicy)) {
            throw new PasswordChangeIllegalArgumentException("New password does not conform to password policy.");
        }

        hogwartsUser.setPassword(passwordEncoder.encode(newPassword));

        // Revoke this user's current JWT by deleting it from Redis.
        redisCacheClient.delete("whiteList:" + userId);
        userRepository.save(hogwartsUser);
    }
}
