package com.ingredisure.api.service;

import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class DietaryRuleEngine {

    private static final Map<String, List<String>> CONDITION_RULES = new HashMap<>();

    static {
        CONDITION_RULES.put("diabetes", Arrays.asList(
                "high fructose corn syrup", "corn syrup", "glucose", "sucrose",
                "sugar", "dextrose", "maltose", "cane sugar", "honey"
        ));
        CONDITION_RULES.put("hypertension", Arrays.asList(
                "sodium", "salt", "monosodium glutamate", "msg", "brine",
                "soy sauce", "sodium nitrate", "sodium benzoate"
        ));
        CONDITION_RULES.put("ckd", Arrays.asList(
                "potassium", "potassium chloride", "phosphate", "phosphorus",
                "sodium phosphate", "potassium citrate"
        ));
        CONDITION_RULES.put("celiac", Arrays.asList(
                "wheat", "barley", "rye", "malt", "gluten", "flour",
                "semolina", "spelt", "triticale"
        ));
        CONDITION_RULES.put("lactose intolerance", Arrays.asList(
                "milk", "cream", "butter", "cheese", "lactose", "whey",
                "casein", "dairy", "yogurt"
        ));
        CONDITION_RULES.put("nut allergy", Arrays.asList(
                "peanut", "almond", "cashew", "walnut", "pecan",
                "pistachio", "hazelnut", "tree nut"
        ));
        CONDITION_RULES.put("shellfish allergy", Arrays.asList(
                "shrimp", "crab", "lobster", "clam", "oyster",
                "scallop", "mussel", "shellfish", "crayfish"
        ));
        CONDITION_RULES.put("heart disease", Arrays.asList(
                "saturated fat", "trans fat", "hydrogenated",
                "partially hydrogenated", "lard", "shortening", "palm oil"
        ));
    }

    public Set<String> buildBlocklist(List<String> conditions, List<String> avoidances) {
        Set<String> blocklist = new HashSet<>();
        for (String condition : conditions) {
            String key = condition.toLowerCase().trim();
            CONDITION_RULES.forEach((conditionKey, keywords) -> {
                if (key.contains(conditionKey)) {
                    blocklist.addAll(keywords);
                }
            });
        }
        for (String avoidance : avoidances) {
            blocklist.add(avoidance.toLowerCase().trim());
        }
        return blocklist;
    }
}