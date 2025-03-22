package com.maplexpbar;

import lombok.Getter;

@Getter
public enum MapleXPBarMode {
    SINGLE("Single Bar"),
    HEALTH_AND_PRAYER("Health and Prayer"),
    MULTI_SKILL("Multi Skill");

    private final String menuName;

    MapleXPBarMode(String menuName)
    {
        this.menuName = menuName;
    }

    @Override
    public String toString()
    {
        return menuName;
    }
}
