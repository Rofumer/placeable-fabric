package com.wennest.placeable;

/**
 * How leaves are laid out on a bamboo column hanging from a ceiling.
 *
 * <p>Persisted by name in {@code config/placeable.json}; renaming a constant
 * resets the user's choice to the default (see
 * {@link PlaceableConfig#validatePostLoad()}).
 */
public enum HangingBambooLeaves {
    /**
     * Vanilla layout flipped upside down: large leaves on the lowest segment
     * (the tip), small leaves on the one above it, bare stalk further up.
     */
    MIRRORED,

    /** Large leaves on every segment. */
    FULL,

    /** Bare stalk, no leaves at all. */
    NONE
}
