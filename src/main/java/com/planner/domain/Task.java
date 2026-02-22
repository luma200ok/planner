package com.planner.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@SQLDelete(sql = "UPDATE task SET deleted = true WHERE id = ?") // 삭제 요청 시 UPDATE 문이 나가게 함
@SQLRestriction("deleted = false") // 모든 조회(findAll 등) 시 삭제되지 않은 것만 가져옴
@Getter
@NoArgsConstructor
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private LocalDate scheduledDate;

    @Enumerated(EnumType.STRING)
    private TaskStatus status = TaskStatus.PLANNED;
    private LocalDateTime completedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    private Template template; // 템플릿 직접 참조

    public Task(String title, LocalDate date, Template template) {
        this.title = title;
        this.scheduledDate = date;
        this.template = template;
    }

    public void complete(LocalDateTime now) {
        this.status = TaskStatus.DONE;
        this.completedAt = now;
    }

    public void skip(LocalDateTime now) {
        this.status = TaskStatus.SKIPPED;
        this.completedAt = now;
    }

    public void moveScheduledDate(LocalDate newDate) {
        this.scheduledDate = newDate;
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void undoTask() {
        this.status = TaskStatus.PLANNED;
        this.completedAt = null; // 완료 시간 초기화
    }

    private boolean deleted = false; // 🚩 삭제 여부 플래그 final x

    // 연관 관계 끊기
    public void disconnectTemplate() {
        // ✨ 수정된 부분: 연결을 끊기 전에 Template의 리스트에서도 나를 제거
        if (this.template != null) {
            this.template.getTasks().remove(this);
        }
        this.template = null;
    }
}
