package com.college.sms.gui;

/**
 * Interface implemented by GUI panels that display dynamic data
 * that must be refreshed when database/service collections change.
 */
public interface RefreshablePanel {
    /**
     * Reloads data from services into table models and combo boxes.
     */
    void refreshData();
}
