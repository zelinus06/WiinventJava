package com.wiinventjava.Service;

import com.wiinventjava.DTO.UserRequest;
import com.wiinventjava.Entity.Users;
import com.wiinventjava.Repository.UsersRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UsersRepo usersRepo;
    private final PasswordEncoder passwordEncoder;

    public UserService() {
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public Users createUser(UserRequest request) {
        String username = request.getUsername();
        // Check the username is existed
        if (usersRepo.existsByUsername(username)) {
            throw new RuntimeException("Username đã tồn tại!");
        }
        // Create new user
        Users user = new Users();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setLotusPoint(0);
        return usersRepo.save(user);
    }
}
