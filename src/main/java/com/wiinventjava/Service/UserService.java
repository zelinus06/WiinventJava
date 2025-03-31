package com.wiinventjava.Service;

import com.wiinventjava.DTO.Request.UserRequest;
import com.wiinventjava.DTO.Response.ProfileUserResponse;
import com.wiinventjava.Entity.LotusPointHistory;
import com.wiinventjava.Entity.Users;
import com.wiinventjava.Repository.LotusPointHistoryRepo;
import com.wiinventjava.Repository.UsersRepo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.wiinventjava.Enums.AttendanceMessage.SYSTEM_BUSY;

@Service
public class UserService {
    @Autowired
    private UsersRepo usersRepo;
    @Autowired
    private LotusPointHistoryRepo historyRepository;
    private final RedissonClient redissonClient;
    private final PasswordEncoder passwordEncoder;
    private static final String LOCK_KEY = "attendance-lock:";

    public UserService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
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

    @Cacheable(value = "userProfile", key = "#username")
    public ProfileUserResponse getProfileUser (String username) {
        Optional<Users> userOp = usersRepo.findByUsername(username);
        if (userOp.isEmpty()) {
            throw new RuntimeException("Không tìm thấy người dùng!");
        }
        Users user = userOp.get();
        return new ProfileUserResponse(user.getUsername(),user.getAvatarUrl(), user.getLotusPoint());
    }

    @Transactional()
    public String minusPoints(String username, int points) throws InterruptedException {
        RLock lock = redissonClient.getLock(LOCK_KEY + username);
//        try {
//            if (!lock.tryLock(5, 4, TimeUnit.SECONDS)) {
//                return SYSTEM_BUSY.getMessage();
//            }
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
            Thread.sleep(3000);
            historyRepository.save(history);
//        } finally {
//            lock.unlock();
//        }
        return "success";
    }
}
