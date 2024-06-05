package io.banditoz.dohmap.model.search;

import io.banditoz.dohmap.model.DataSource;

import java.util.List;
import java.util.Objects;

public class Search {
    private String search;
    private List<String> cities;
    private SearchOrder orderBy;
    private List<DataSource> sources;
    private int minimumInspCount;

    /**
     * @return null if the search is <i>empty</i> or <i>blank</i> else the String.
     */
    public String getSearch() {
        return search == null || search.isBlank() ? null : search;
    }

    public String getRealSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    /**
     * @return null if the member list is empty or null, else the values in the list.
     */
    public List<String> getCities() {
        return cities == null || cities.isEmpty() ? null : cities;
    }

    public void setCities(List<String> cities) {
        this.cities = cities;
    }

    public SearchOrder getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(SearchOrder orderBy) {
        this.orderBy = orderBy;
    }

    public List<DataSource> getSources() {
        return sources == null || sources.isEmpty() ? null : sources;
    }

    public void setSources(List<DataSource> sources) {
        this.sources = sources;
    }

    public int getMinimumInspCount() {
        return minimumInspCount;
    }

    public void setMinimumInspCount(int minimumInspCount) {
        this.minimumInspCount = minimumInspCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Search search = (Search) o;
        return minimumInspCount == search.minimumInspCount && Objects.equals(this.search, search.search) && Objects.equals(cities, search.cities) && orderBy == search.orderBy && Objects.equals(sources, search.sources);
    }

    @Override
    public int hashCode() {
        return Objects.hash(search, cities, orderBy, sources, minimumInspCount);
    }

    @Override
    public String toString() {
        return "Search{" +
                "search='" + search + '\'' +
                ", cities=" + cities +
                ", orderBy=" + orderBy +
                ", sources=" + sources +
                ", minimumInspCount=" + minimumInspCount +
                '}';
    }
}
