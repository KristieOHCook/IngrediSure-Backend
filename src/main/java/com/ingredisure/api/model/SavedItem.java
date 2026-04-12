package com.ingredisure.api.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "saved_items")
public class SavedItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "item_source", nullable = false)
    private String itemSource;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(name = "brand_or_restaurant")
    private String brandOrRestaurant;

    @Column(columnDefinition = "TEXT")
    private String ingredients;

    @Column(name = "safety_verdict", nullable = false)
    private String safetyVerdict;

    @Column(name = "matched_triggers")
    private String matchedTriggers;

    @Column(name = "external_item_id")
    private String externalItemId;

    @Column(name = "saved_at")
    private LocalDateTime savedAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getItemSource() { return itemSource; }
    public void setItemSource(String itemSource) { this.itemSource = itemSource; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getBrandOrRestaurant() { return brandOrRestaurant; }
    public void setBrandOrRestaurant(String brandOrRestaurant) { this.brandOrRestaurant = brandOrRestaurant; }
    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }
    public String getSafetyVerdict() { return safetyVerdict; }
    public void setSafetyVerdict(String safetyVerdict) { this.safetyVerdict = safetyVerdict; }
    public String getMatchedTriggers() { return matchedTriggers; }
    public void setMatchedTriggers(String matchedTriggers) { this.matchedTriggers = matchedTriggers; }
    public String getExternalItemId() { return externalItemId; }
    public void setExternalItemId(String externalItemId) { this.externalItemId = externalItemId; }
    public LocalDateTime getSavedAt() { return savedAt; }
    public void setSavedAt(LocalDateTime savedAt) { this.savedAt = savedAt; }
}