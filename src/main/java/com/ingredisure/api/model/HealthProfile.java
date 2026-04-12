package com.ingredisure.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "health_profiles")
public class HealthProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Double currentWeight;
    private Double goalWeight;
    private Double heightInches;
    private Integer dailyCarbGoal;
    private String dietaryRestrictions;
    private String dietaryGoal;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Double getCurrentWeight() { return currentWeight; }
    public void setCurrentWeight(Double currentWeight) { this.currentWeight = currentWeight; }
    public Double getGoalWeight() { return goalWeight; }
    public void setGoalWeight(Double goalWeight) { this.goalWeight = goalWeight; }
    public Double getHeightInches() { return heightInches; }
    public void setHeightInches(Double heightInches) { this.heightInches = heightInches; }
    public Integer getDailyCarbGoal() { return dailyCarbGoal; }
    public void setDailyCarbGoal(Integer dailyCarbGoal) { this.dailyCarbGoal = dailyCarbGoal; }
    public String getDietaryRestrictions() { return dietaryRestrictions; }
    public void setDietaryRestrictions(String dietaryRestrictions) { this.dietaryRestrictions = dietaryRestrictions; }
    public String getDietaryGoal() { return dietaryGoal; }
    public void setDietaryGoal(String dietaryGoal) { this.dietaryGoal = dietaryGoal; }
}