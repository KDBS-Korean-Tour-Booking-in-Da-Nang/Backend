package com.example.KDBS.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_id_card")
public class UserIdCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCardId;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "id_number")
    private String idNumber;

    @Column(name = "id_prob")
    private String idProb;

    @Column(name = "name")
    private String name;

    @Column(name = "name_prob")
    private String nameProb;

    @Column(name = "dob")
    private String dob;

    @Column(name = "dob_prob")
    private String dobProb;

    @Column(name = "sex")
    private String sex;

    @Column(name = "sex_prob")
    private String sexProb;

    @Column(name = "nationality")
    private String nationality;

    @Column(name = "nationality_prob")
    private String nationalityProb;

    @Column(name = "home")
    private String home;

    @Column(name = "home_prob")
    private String homeProb;

    @Column(name = "address")
    private String address;

    @Column(name = "address_prob")
    private String addressProb;

    @Column(name = "province")
    private String province;

    @Column(name = "district")
    private String district;

    @Column(name = "ward")
    private String ward;

    @Column(name = "street")
    private String street;

    @Column(name = "doe")
    private String doe;

    @Column(name = "doe_prob")
    private String doeProb;

    @Column(name = "type")
    private String type;

    @Column(name = "front_image_path")
    private String frontImagePath;

    @Column(name = "back_image_path")
    private String backImagePath;
}
