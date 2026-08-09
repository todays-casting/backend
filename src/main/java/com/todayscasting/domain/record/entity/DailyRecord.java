package com.todayscasting.domain.record.entity;

import com.todayscasting.common.entity.BaseEntity;
import org.hibernate.annotations.JdbcTypeCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.List;

@Entity // DB 테이블 하나랑 매핑되는 객체라는걸 알려주는 어노테이션
@Table( // 어느 테이블이랑 매핑할지 이름 표시,
        name = "daily_records",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "record_date"})
)
@Getter // getter 메서드를 자동으로 만들어줌
@NoArgsConstructor(access = AccessLevel.PROTECTED)

// BaseEntity 상속
public class DailyRecord extends BaseEntity {

    @Id // PK 표시
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate; // 시간없이 날짜만 다루는 자바 타입, SQL의 DATE 타입과 대응

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // mood 수정
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "mood", columnDefinition = "json")
    private List<String> mood;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "mood_tags", columnDefinition = "json")
    private List<String> moodTags;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "activity_tags", columnDefinition = "json")
    private List<String> activityTags;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    public enum Status {
        DRAFT, COMPLETED
    }

    public static DailyRecord create(Long userId, LocalDate recordDate, String content,
                                     List<String> mood, List<String> moodTags, List<String> activityTags, Status status) {
        DailyRecord record = new DailyRecord();
        record.userId = userId;
        record.recordDate = recordDate;
        record.content = content;
        record.mood = mood;
        record.moodTags = moodTags;
        record.activityTags = activityTags;
        record.status = status != null ? status : Status.COMPLETED;
        return record;
    }

    // userId, recordDate는 한번 정해지면 바뀌지 않으니까 없음
    public void update(String content, List<String> mood, List<String> moodTags, List<String> activityTags, Status status) {
        this.content = content;
        this.mood = mood;
        this.moodTags = moodTags;
        this.activityTags = activityTags;
        this.status = status != null ? status : Status.COMPLETED;
    }
}