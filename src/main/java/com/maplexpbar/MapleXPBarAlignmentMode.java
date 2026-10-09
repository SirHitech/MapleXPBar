package com.maplexpbar;

import lombok.Getter;

@Getter
public enum MapleXPBarAlignmentMode {
    CENTER("Centered"),
    LEFT("Left"),
    RIGHT("Right");

    private final String menuName;

    MapleXPBarAlignmentMode(String menuName)
    {
        this.menuName = menuName;
    }

    @Override
    public String toString()
    {
        return menuName;
    }
}
