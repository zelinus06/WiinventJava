package com.wiinventjava.Controller;

import com.wiinventjava.Service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/check-in")
    public ResponseEntity<?> checkIn(@RequestParam String username, @RequestParam LocalDate checkinDate, @RequestParam LocalTime checkinTime) {
        String message = attendanceService.checkIn(username, checkinDate, checkinTime);
        return ResponseEntity.ok(Map.of("message", message));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAttendanceStatus(
            @RequestParam String username,
            @RequestParam int month,
            @RequestParam int year) {

        Map<String, Object> response = attendanceService.getAttendanceStatus(username, month, year);
        return ResponseEntity.ok(response);
    }
}

