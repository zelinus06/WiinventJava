package com.wiinventjava.Service;

import com.wiinventjava.Entity.LotusPointHistory;
import com.wiinventjava.Entity.Users;
import com.wiinventjava.Repository.LotusPointHistoryRepo;
import com.wiinventjava.Repository.UsersRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.wiinventjava.Enums.AttendanceMessage.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {
    private final UsersRepo userRepository;
    private final LotusPointHistoryRepo lotusPointHistoryRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedissonClient redissonClient;

    private static final String ATTENDANCE_KEY = "attendance:";
    private static final String LOCK_KEY = "attendance-lock:";
    private static final List<Integer> LOTUS_POINTS = List.of(1, 2, 3, 5, 8, 13, 21);
    private static final int MAX_ATTENDANCE_DAYS = 7;

    @Transactional
    public String checkIn(String username,LocalDate checkInDate, LocalTime checkInTime) {
        // Kiểm tra thời gian điểm danh
        if (!isValidCheckInTime(checkInTime)) {
            return WRONG_TIME.getMessage();
        }

//        String today = LocalDate.now().toString();
        // Giả lập thời gian điểm danh
        String today = checkInDate.toString();
//        String currentMonth = LocalDate.now().getYear() + "-" + LocalDate.now().getMonthValue();
        String redisKey = ATTENDANCE_KEY + username + ":" + checkInDate.getYear()+ "-" + checkInDate.getMonthValue();
        RLock lock = redissonClient.getLock(LOCK_KEY + username);

        try {
            if (!lock.tryLock(3, 3, TimeUnit.SECONDS)) {
                return SYSTEM_BUSY.getMessage();
            }

            // Lấy danh sách ngày điểm danh trong Redis
            Set<Object> attendanceDays = redisTemplate.opsForSet().members(redisKey);

            if (!attendanceDays.isEmpty() && attendanceDays.contains(today)) {
                return ALREADY_CHECKED_IN.getMessage(); // Đã điểm danh hôm nay
            }

            int attendanceCount = attendanceDays.size();
            if (attendanceCount >= MAX_ATTENDANCE_DAYS) {
                return REACHED_MAX_DAYS.getMessage(); // Đã đạt giới hạn 7 ngày trong tháng
            }

            // Lấy thông tin user
            Users user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Cộng điểm theo số lần điểm danh
            int lotusPoint = LOTUS_POINTS.get(attendanceCount);
            user.setLotusPoint(user.getLotusPoint() + lotusPoint);
            userRepository.save(user);

            // Lưu lịch sử điểm danh
            LotusPointHistory history = new LotusPointHistory();
            history.setUser(user);
            history.setPoints(lotusPoint);
            history.setType("ADD");
//            history.setCreatedAt(LocalDateTime.now());

            // Lưu thời gian giả lập điểm danh
            LocalDateTime createdAt = checkInDate.atTime(checkInTime);
            history.setCreatedAt(createdAt);

            lotusPointHistoryRepository.save(history);

            // Thêm ngày điểm danh vào Redis
            redisTemplate.opsForSet().add(redisKey, today);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
        return CHECK_IN_SUCCESS.getMessage();
    }


    public Map<String, Object> getAttendanceStatus(String username, int month, int year) {
        String redisKey = "attendance:" + username + ":" + year + "-" + month;
        Set<Object> attendanceDaysSet = redisTemplate.opsForSet().members(redisKey);

        // Chuyển dữ liệu từ Object sang Set<String> (vì Redis lưu dạng JSON String)
        Set<String> attendanceDays = attendanceDaysSet.stream()
                .map(Object::toString)
                .map(s -> s.replaceAll("\"", "")) // Xóa dấu ngoặc kép nếu có
                .collect(Collectors.toSet());

        // Lấy số ngày trong tháng
        YearMonth yearMonth = YearMonth.of(year, month);
        List<String> allDaysInMonth = IntStream.rangeClosed(1, yearMonth.lengthOfMonth())
                .mapToObj(day -> LocalDate.of(year, month, day).toString())
                .toList();

        // Chia danh sách ngày đã điểm danh và chưa điểm danh
        List<String> attendedDays = allDaysInMonth.stream()
                .filter(attendanceDays::contains)
                .collect(Collectors.toList());

        List<String> missedDays = allDaysInMonth.stream()
                .filter(day -> !attendanceDays.contains(day))
                .collect(Collectors.toList());

        // Trả về kết quả
        Map<String, Object> response = new HashMap<>();
        response.put("attendedDays", attendedDays);
        response.put("missedDays", missedDays);
        return response;
    }

    private boolean isValidCheckInTime(LocalTime checkInTime) {
        return (checkInTime.isAfter(LocalTime.of(8, 59)) && checkInTime.isBefore(LocalTime.of(11, 1))) ||
                (checkInTime.isAfter(LocalTime.of(18, 59)) && checkInTime.isBefore(LocalTime.of(21, 1)));
    }
}

