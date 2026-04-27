package com.jangchwisa.userservice.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "login_id", nullable = false, unique = true, length = 50)
    private String loginId;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Gender gender;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "desired_job", length = 100)
    private String desiredJob;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    private User(
            String loginId,
            String passwordHash,
            String name,
            LocalDate birthDate,
            Gender gender,
            String email,
            String desiredJob,
            AccountStatus status,
            LocalDateTime createdAt
    ) {
        this.loginId = loginId;
        this.passwordHash = passwordHash;
        this.name = name;
        this.birthDate = birthDate;
        this.gender = gender;
        this.email = email;
        this.desiredJob = desiredJob;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public static User create(
            String loginId,
            String passwordHash,
            String name,
            LocalDate birthDate,
            Gender gender,
            String email,
            String desiredJob,
            LocalDateTime createdAt
    ) {
        return new User(loginId, passwordHash, name, birthDate, gender, email, desiredJob, AccountStatus.ACTIVE, createdAt);
    }

    public void updateProfile(String name, Gender gender, String email, String desiredJob, LocalDateTime updatedAt) {
        this.name = name;
        this.gender = gender;
        this.email = email;
        this.desiredJob = desiredJob;
        this.updatedAt = updatedAt;
    }

    public void updateLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
        this.updatedAt = lastLoginAt;
    }

    public void changeStatus(AccountStatus status, LocalDateTime updatedAt) {
        this.status = status;
        this.updatedAt = updatedAt;
    }
}
