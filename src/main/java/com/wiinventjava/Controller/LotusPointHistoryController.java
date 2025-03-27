package com.wiinventjava.Controller;

import com.wiinventjava.Entity.LotusPointHistory;
import com.wiinventjava.Entity.Users;
import com.wiinventjava.Service.LotusPointHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lotus-point")
@RequiredArgsConstructor
public class LotusPointHistoryController {
    private final LotusPointHistoryService historyService;

    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getPointHistory(
            @RequestParam String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<LotusPointHistory> pointHistoryPage = historyService.getPointHistory(username, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("data", pointHistoryPage.getContent());
        response.put("currentPage", pointHistoryPage.getNumber());
        response.put("totalItems", pointHistoryPage.getTotalElements());
        response.put("totalPages", pointHistoryPage.getTotalPages());
        response.put("pageSize", pointHistoryPage.getSize());

        return ResponseEntity.ok(response);
    }
}
