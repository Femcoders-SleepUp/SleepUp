package com.SleepUp.SU.user.utils;

import com.SleepUp.SU.user.User;
import com.SleepUp.SU.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserServiceHelper {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void checkEmail(String request) {
        Optional<User> isExistingEmail = userRepository.findByEmail(request);
        if (isExistingEmail.isPresent()) {
            throw new RuntimeException("EmailAlreadyExistException");
        }
    }

    public void checkUsername(String request) {
        Optional<User> isExistingUsername = userRepository.findByUsername(request);
        if (isExistingUsername.isPresent()) {
            throw new RuntimeException("UsernameAlreadyExistException");
        }
    }

    public Optional<User> getUserLogin(String username) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if(optionalUser.isEmpty()){
            throw new UsernameNotFoundException(username + " does not exist.");
        }
        return optionalUser;
    }

    public String getEncodePassword(String password) {
        return passwordEncoder.encode(password);
    }

}
