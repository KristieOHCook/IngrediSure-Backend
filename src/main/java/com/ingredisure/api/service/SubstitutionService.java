package com.ingredisure.api.service;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class SubstitutionService {

    private static final Map<String, String> SUBSTITUTION_MAP = new LinkedHashMap<>();

    static {
        SUBSTITUTION_MAP.put("sodium", "Ask for no added salt or sauce on the side.");
        SUBSTITUTION_MAP.put("salt", "Request low-sodium preparation.");
        SUBSTITUTION_MAP.put("msg", "Ask if MSG can be omitted.");
        SUBSTITUTION_MAP.put("sugar", "Ask for unsweetened version.");
        SUBSTITUTION_MAP.put("high fructose corn syrup", "Look for no added sugar version.");
        SUBSTITUTION_MAP.put("wheat", "Ask if gluten-free option is available.");
        SUBSTITUTION_MAP.put("gluten", "Request gluten-free preparation.");
        SUBSTITUTION_MAP.put("milk", "Substitute with oat milk or almond milk.");
        SUBSTITUTION_MAP.put("cream", "Ask for dairy-free alternative.");
        SUBSTITUTION_MAP.put("cheese", "Ask for dairy-free cheese or omit.");
        SUBSTITUTION_MAP.put("butter", "Substitute with olive oil.");
        SUBSTITUTION_MAP.put("peanut", "Ask if sunflower seed butter can substitute.");
        SUBSTITUTION_MAP.put("trans fat", "Request items prepared without hydrogenated oils.");
        SUBSTITUTION_MAP.put("hydrogenated", "Choose items prepared with olive oil instead.");
    }

    public List<Map<String, String>> getSuggestions(List<String> triggers) {
        List<Map<String, String>> suggestions = new ArrayList<>();
        for (String trigger : triggers) {
            String lower = trigger.toLowerCase().trim();
            SUBSTITUTION_MAP.forEach((key, advice) -> {
                if (lower.contains(key) || key.contains(lower)) {
                    Map<String, String> suggestion = new LinkedHashMap<>();
                    suggestion.put("trigger", trigger);
                    suggestion.put("suggestion", advice);
                    suggestions.add(suggestion);
                }
            });
        }
        if (suggestions.isEmpty() && !triggers.isEmpty()) {
            Map<String, String> generic = new LinkedHashMap<>();
            generic.put("trigger", String.join(", ", triggers));
            generic.put("suggestion", "Ask your server about modifications for your dietary needs.");
            suggestions.add(generic);
        }
        return suggestions;
    }
}