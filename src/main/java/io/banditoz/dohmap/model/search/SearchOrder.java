package io.banditoz.dohmap.model.search;

/**
 * Enum representing search order for web queries. First enum will be the default!<br>
 * If names are changed, they must also be changed in {@link io.banditoz.dohmap.database.mapper.SearchMapper}
 */
public enum SearchOrder {
    LAST_INSPECTION("Last Inspection"),
    FIRST_SEEN("First Seen"),
    LAST_SEEN("Last Seen"),
    MOST_INSPECTIONS("Most Inspections"),
    MOST_VIOLATIONS("Most Violations"),
    LEAST_VIOLATIONS("Least Violations"),
    MOST_VIOLATIONS_AVG("Highest Number of Violations per Inspection");

    private final String displayName;

    SearchOrder(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
