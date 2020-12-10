package de.dosmike.sponge.oreapi.v2;

public enum OreReviewState {

    UNREVIEWED,
    REVIEWED,
    BACKLOG,
    PARTIALLY_REVIEWED,
    ;
    public static OreReviewState fromString(String string) {
        for (OreReviewState state : values()) {
            if (state.name().equalsIgnoreCase(string))
                return state;
        }
        throw new IllegalArgumentException("No such OreCategory "+string);
    }

}
