package com.example.KDBS.model;

import com.example.KDBS.enums.Role;
import com.example.KDBS.enums.StaffTask;
import com.example.KDBS.enums.Status;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "User")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", length = 30)
    private int userId;

    @Column(name = "username", length = 30)
    private String username;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "avatar", length = 500)
    private String avatar;

    @Column(name = "phone", length = 11, unique = true)
    private String phone;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "cccd", length = 12, unique = true)
    private String cccd;

    @Column(name = "big_decimal", precision = 15, scale = 2)
    private BigDecimal balance;

    @Column(name = "gender", length = 3)
    private String gender;

    @Column(name = "create_at")
    private LocalDateTime createdAt;

    @Column(name = "status", length = 255)
    private Status status;

    @Column(name = "role", length = 255)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "staff_task", length = 255)
    @Enumerated(EnumType.STRING)
    private StaffTask staffTask;

    @Column(name = "ban_reason", length = 255)
    private String banReason;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference
    private BusinessLicense businessLicense;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ForumPost> forumPosts;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ForumComment> forumComments;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Reaction> reactions;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<TourRated> tourRatings;

}
