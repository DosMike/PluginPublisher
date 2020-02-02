package de.dosmike.sponge.oreapi.v2;

public enum OreCategory {

    ADMIN_TOOLS,
    CHAT,
    DEV_TOOLS,
    ECONOMY,
    GAMEPLAY,
    GAMES,
    PROTECTION,
    ROLE_PLAYING,
    WORLD_MANAGEMENT,
    MISC,
    ;

    public static OreCategory fromString(String string) {
        for (OreCategory category : values()) {
            if (category.name().equalsIgnoreCase(string))
                return category;
        }
        throw new IllegalArgumentException("No such OreCategory "+string);
    }

}
