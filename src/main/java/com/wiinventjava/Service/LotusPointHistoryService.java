package com.wiinventjava.Service;


import com.wiinventjava.Entity.LotusPointHistory;
import com.wiinventjava.Entity.Users;
import com.wiinventjava.Repository.LotusPointHistoryRepo;
import com.wiinventjava.Repository.UsersRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LotusPointHistoryService {
    private final LotusPointHistoryRepo historyRepository;
    private final UsersRepo userRepository;

    public Page<LotusPointHistory> getPointHistory(String username, Pageable pageable) {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return historyRepository.findByUserId(user.getId(), pageable);
    }
}

