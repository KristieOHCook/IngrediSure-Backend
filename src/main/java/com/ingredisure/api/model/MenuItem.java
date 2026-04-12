package com.ingredisure.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "menu_items")
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Item name is required")
    private String itemName;

    @NotBlank(message = "Restaurant name is required")
    private String restaurantName;

    @NotBlank(message = "Ingredients are required")
    private String ingredients;

    @Min(0) @Max(10)
    private int sodiumLevel;

    @Min(0) @Max(10)
    private int potassiumLevel;

    @Min(0) @Max(10)
    private int sugarLevel;

    private String dietCategory;
    private String modificationTip;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getRestaurantName() { return restaurantName; }
    public void setRestaurantName(String restaurantName) { this.restaurantName = restaurantName; }
    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }
    public int getSodiumLevel() { return sodiumLevel; }
    public void setSodiumLevel(int sodiumLevel) { this.sodiumLevel = sodiumLevel; }
    public int getPotassiumLevel() { return potassiumLevel; }
    public void setPotassiumLevel(int potassiumLevel) { this.potassiumLevel = potassiumLevel; }
    public int getSugarLevel() { return sugarLevel; }
    public void setSugarLevel(int sugarLevel) { this.sugarLevel = sugarLevel; }
    public String getDietCategory() { return dietCategory; }
    public void setDietCategory(String dietCategory) { this.dietCategory = dietCategory; }
    public String getModificationTip() { return modificationTip; }
    public void setModificationTip(String modificationTip) { this.modificationTip = modificationTip; }
}