package com.maplexpbar;

import lombok.Getter;

@Getter
public enum MapleXPBarAnchorMode {
    CHATBOX("Chatbox"),
    TOP_LEFT("Top Left"),
    MINIMAP("Minimap"),
    INVENTORY("Inventory");

    private final String menuName;

    MapleXPBarAnchorMode(String menuName)
    {
        this.menuName = menuName;
    }

    @Override
    public String toString()
    {
        return menuName;
    }
}
