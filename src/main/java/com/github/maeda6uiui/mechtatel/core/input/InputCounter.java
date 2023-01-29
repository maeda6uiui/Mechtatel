package com.github.maeda6uiui.mechtatel.core.input;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Counter for user input
 *
 * @author maeda6uiui
 */
public class InputCounter {
    private Map<String, Boolean> pressingFlags;
    private Map<String, Integer> pressingCounts;
    private Map<String, Integer> releasingCounts;

    public InputCounter(List<String> keys) {
        pressingFlags = new HashMap<>();
        pressingCounts = new HashMap<>();
        releasingCounts = new HashMap<>();

        keys.forEach(key -> {
            pressingFlags.put(key, false);
            pressingCounts.put(key, 0);
            releasingCounts.put(key, 0);
        });
    }

    public int getPressingCount(String key) {
        return pressingCounts.get(key);
    }

    public int getReleasingCount(String key) {
        return releasingCounts.get(key);
    }

    public void setPressingFlag(String key, boolean pressingFlag) {
        pressingFlags.put(key, pressingFlag);
    }

    public void update() {
        for (var entry : pressingFlags.entrySet()) {
            String key = entry.getKey();
            boolean pressingFlag = entry.getValue();

            int pressingCount = pressingCounts.get(key);
            int releasingCount = releasingCounts.get(key);

            if (pressingFlag) {
                pressingCounts.put(key, pressingCount + 1);
                releasingCounts.put(key, 0);
            } else {
                pressingCounts.put(key, 0);
                releasingCounts.put(key, releasingCount + 1);
            }
        }
    }
}
