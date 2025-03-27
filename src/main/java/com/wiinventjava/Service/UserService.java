package com.wiinventjava.Service;

import com.wiinventjava.DTO.Request.UserRequest;
import com.wiinventjava.DTO.Response.ProfileUserResponse;
import com.wiinventjava.Entity.LotusPointHistory;
import com.wiinventjava.Entity.Users;
import com.wiinventjava.Repository.LotusPointHistoryRepo;
import com.wiinventjava.Repository.UsersRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UsersRepo usersRepo;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private LotusPointHistoryRepo historyRepository;
    public UserService() {
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public Users createUser(UserRequest request) {
        // Check the username is existed
        if (usersRepo.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username đã tồn tại!");
        }
        // Create new user
        Users user = new Users();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setLotusPoint(0);
        return usersRepo.save(user);
    }

    public ProfileUserResponse getProfileUser (String username) {
        Optional<Users> userOp = usersRepo.findByUsername(username);
        if (userOp.isEmpty()) {
            throw new RuntimeException("Không tìm thấy người dùng!");
        }
        Users user = userOp.get();
        return new ProfileUserResponse(user.getUsername(),user.getAvatarUrl(), user.getLotusPoint());
    }

    @Transactional
    public void minusPoints(String username, int points) {
        Users user = usersRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user: " + username));

        if (user.getLotusPoint() < points) {
            throw new RuntimeException("Người dùng không đủ điểm!");
        }

        // Trừ điểm
        user.setLotusPoint(user.getLotusPoint() - points);
        usersRepo.save(user);

        // Ghi lịch sử trừ điểm
        LotusPointHistory history = new LotusPointHistory();
        history.setUser(user);
        history.setPoints(points);
        history.setType("MINUS");
        history.setCreatedAt(LocalDateTime.now());

        historyRepository.save(history);
    }
}
