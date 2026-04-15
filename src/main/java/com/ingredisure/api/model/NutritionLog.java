package com.ingredisure.api.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "nutrition_logs")
public class NutritionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "log_date")
    private LocalDate logDate = LocalDate.now();

    @Column(name = "meal_name")
    private String mealName;

    @Column(name = "meal_type")
    private String mealType;

    @Column(name = "calories")
    private int calories;

    @Column(name = "protein")
    private double protein;

    @Column(name = "carbs")
    private double carbs;

    @Column(name = "fat")
    private double fat;

    @Column(name = "sodium")
    private double sodium;

    @Column(name = "fiber")
    private double fiber;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public LocalDate getLogDate() { return logDate; }
    public void setLogDate(LocalDate logDate) { this.logDate = logDate; }
    public String getMealName() { return mealName; }
    public void setMealName(String mealName) { this.mealName = mealName; }
    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }
    public int getCalories() { return calories; }
    public void setCalories(int calories) { this.calories = calories; }
    public double getProtein() { return protein; }
    public void setProtein(double protein) { this.protein = protein; }
    public double getCarbs() { return carbs; }
    public void setCarbs(double carbs) { this.carbs = carbs; }
    public double getFat() { return fat; }
    public void setFat(double fat) { this.fat = fat; }
    public double getSodium() { return sodium; }
    public void setSodium(double sodium) { this.sodium = sodium; }
    public double getFiber() { return fiber; }
    public void setFiber(double fiber) { this.fiber = fiber; }
}
