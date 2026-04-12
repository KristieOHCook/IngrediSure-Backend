package com.ingredisure.api.service;

import com.ingredisure.api.model.MenuItem;
import com.ingredisure.api.repository.MenuItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class MenuItemService {

    private static final int HIGH_SODIUM_THRESHOLD = 7;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Transactional(readOnly = true)
    public List<MenuItem> getAllMenuItems() {
        return menuItemRepository.findAll();
    }

    @Transactional
    public MenuItem saveMenuItem(MenuItem item) {
        if (item.getSodiumLevel() > HIGH_SODIUM_THRESHOLD) {
            String existing = item.getDietCategory() != null ? item.getDietCategory() : "";
            if (!existing.contains("High Sodium")) {
                item.setDietCategory(existing.isBlank() ? "High Sodium" : existing + ", High Sodium");
            }
            if (item.getModificationTip() == null || item.getModificationTip().isBlank()) {
                item.setModificationTip("Ask for sauces on the side and avoid added salt.");
            }
        }
        return menuItemRepository.save(item);
    }
}