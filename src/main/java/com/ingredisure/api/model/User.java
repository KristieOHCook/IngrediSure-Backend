package com.ingredisure.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username is required")
    @Column(unique = true, nullable = false)
    private String username;

    @Email(message = "Must be a valid email")
    private String email;

    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    private String password;

    private String role;
    @Column(name = "phone")
    private String phone;

    @Column(name = "email_opt_in", columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean emailOptIn = true;

    @Column(name = "sms_opt_in", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean smsOptIn = false;


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Boolean isEmailOptIn() { return emailOptIn != null ? emailOptIn : true; }
    public void setEmailOptIn(Boolean emailOptIn) { this.emailOptIn = emailOptIn != null ? emailOptIn : true; }
    public Boolean isSmsOptIn() { return smsOptIn != null ? smsOptIn : false; }
    public void setSmsOptIn(Boolean smsOptIn) { this.smsOptIn = smsOptIn != null ? smsOptIn : false; }
}