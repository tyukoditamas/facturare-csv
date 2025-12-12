package org.app.model;

public enum BusinessMode {
    FAN("FAN"),
    ASSET("ASSET");

    private final String displayName;

    BusinessMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
