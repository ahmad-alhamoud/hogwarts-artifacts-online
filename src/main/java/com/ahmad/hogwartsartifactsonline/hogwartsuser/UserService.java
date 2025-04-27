package com.ahmad.hogwartsartifactsonline.hogwartsuser;

import com.ahmad.hogwartsartifactsonline.system.exception.ObjectNotFoundException;
import jakarta.transaction.Transactional;
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

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
}
