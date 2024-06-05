package io.banditoz.dohmap.model;

public enum DataSource {
    /** Data is from Salt Lake County's
     * <a href="https://public.cdpehs.com/UTEnvPbl/(S(soffpeg13ugefn2bsa1ppse0))/VW_EST_PUBLIC/ShowVW_EST_PUBLICTablePage.aspx">implementation</a>
     * of <a href="https://www.cdpehs.com/">CDP.</a> */
    SALT_LAKE_COUNTY_CDP("Salt Lake County"),
    /**
     * Data is from Utah County's implementation of <a href="https://paragoncda.com/">Paragon's</a>
     * <a href="https://www.inspectionsonline.us/InspectionsOnline.html">Inspections Online</a> platform.
     */
    UTAH_COUNTY_PARAGON("Utah County");

    private final String displayName;

    DataSource(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
