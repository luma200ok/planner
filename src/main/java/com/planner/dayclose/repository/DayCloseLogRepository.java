package com.planner.dayclose.repository;

import com.planner.dayclose.domain.DayCloseLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface DayCloseLogRepository extends JpaRepository<DayCloseLog, Long> {
//    Optional<DayCloseLog> findByClosedDate(LocalDate closedDate);

    @Modifying
    @Query(
            value = "INSERT IGNORE INTO day_close_logs(closed_date, executed_at) VALUES (:closedDate,:executedAt)",
            nativeQuery = true
    )
    int insertIgnore(@Param("closedDate") LocalDate date,
                     @Param("executedAt") LocalDateTime executedAt);
}
