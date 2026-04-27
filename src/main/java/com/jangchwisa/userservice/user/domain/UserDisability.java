package com.jangchwisa.userservice.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "user_disabilities",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_disability", columnNames = {"user_id", "disability_type"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserDisability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "disability_id")
    private Long disabilityId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "disability_type", nullable = false, length = 100)
    private String disabilityType;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private UserDisability(User user, String disabilityType, LocalDateTime createdAt) {
        this.user = user;
        this.disabilityType = disabilityType;
        this.createdAt = createdAt;
    }

    public static UserDisability create(User user, String disabilityType, LocalDateTime createdAt) {
        return new UserDisability(user, disabilityType, createdAt);
    }
}
