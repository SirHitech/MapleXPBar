package com.maplexpbar;

import lombok.Getter;

@Getter
public enum MapleXPBarTooltipMode {
    CURRENT_XP("Current XP"),
    PERCENTAGE("Percentage"),
    BOTH("Both");

    private final String menuName;

    MapleXPBarTooltipMode(String menuName)
    {
        this.menuName = menuName;
    }

    @Override
    public String toString()
    {
        return menuName;
    }
}
