package com.ingredisure.api.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "family_members")
public class FamilyMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "member_name", nullable = false)
    private String memberName;

    @Column(name = "relationship")
    private String relationship;

    @Column(name = "age")
    private Integer age;

    @Column(name = "conditions", columnDefinition = "TEXT")
    private String conditions;

    @Column(name = "avoidances", columnDefinition = "TEXT")
    private String avoidances;

    @Column(name = "avatar_color")
    private String avatarColor;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }
    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public String getConditions() { return conditions; }
    public void setConditions(String conditions) { this.conditions = conditions; }
    public String getAvoidances() { return avoidances; }
    public void setAvoidances(String avoidances) { this.avoidances = avoidances; }
    public String getAvatarColor() { return avatarColor; }
    public void setAvatarColor(String avatarColor) { this.avatarColor = avatarColor; }
}
