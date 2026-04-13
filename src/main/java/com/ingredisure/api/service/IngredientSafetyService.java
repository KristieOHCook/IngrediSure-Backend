package com.ingredisure.api.service;

import com.ingredisure.api.model.AvoidanceIngredient;
import com.ingredisure.api.model.MedicalCondition;
import com.ingredisure.api.repository.AvoidanceIngredientRepository;
import com.ingredisure.api.repository.MedicalConditionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class IngredientSafetyService {

    @Autowired
    private AvoidanceIngredientRepository avoidanceRepo;

    @Autowired
    private MedicalConditionRepository medicalConditionRepo;

    @Autowired
    private DietaryRuleEngine ruleEngine;

    public static class SafetyResult {
        public String verdict;
        public List<String> triggers;
        public String summary;

        public SafetyResult(String verdict, List<String> triggers, String summary) {
            this.verdict = verdict;
            this.triggers = triggers;
            this.summary = summary;
        }
    }

    @Transactional(readOnly = true)
    public SafetyResult checkIngredients(Long userId, String ingredientText) {
        List<String> conditions = medicalConditionRepo.findByUserId(userId)
                .stream().map(MedicalCondition::getConditionName).collect(Collectors.toList());

        List<String> avoidances = avoidanceRepo.findByUserId(userId)
                .stream().map(AvoidanceIngredient::getIngredientName).collect(Collectors.toList());

        Set<String> blocklist = ruleEngine.buildBlocklist(conditions, avoidances);
        String normalized = ingredientText.toLowerCase();

        List<String> matched = blocklist.stream()
                .filter(blocked -> normalized.contains(blocked))
                .collect(Collectors.toList());

        if (matched.isEmpty()) {
            return new SafetyResult("safe", Collections.emptyList(),
                    "No flagged ingredients detected.");
        }

        String verdict = matched.size() <= 2 ? "caution" : "unsafe";
        String summary = String.format("Found %d flagged ingredient(s): %s",
                matched.size(), String.join(", ", matched));

        return new SafetyResult(verdict, matched, summary);
    }
}

