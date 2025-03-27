package com.wiinventjava.Repository;

import com.wiinventjava.Entity.LotusPointHistory;
import com.wiinventjava.Entity.Users;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LotusPointHistoryRepo extends JpaRepository<LotusPointHistory, Long> {
    Page<LotusPointHistory> findByUserId(Long user_id, Pageable pageable);
}
