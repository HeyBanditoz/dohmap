package io.banditoz.dohmap.scraper.page.base;

import java.util.ArrayList;
import java.util.List;

public record PageConfiguration(int startPage, int endPage) {
    public String tName() {
        return startPage + "," + endPage;
    }

    public static List<PageConfiguration> dividePages(int totalPages, int numberOfGroups) {
        if (numberOfGroups > totalPages) {
            throw new IllegalArgumentException("numberOfGroups=" + numberOfGroups + " exceeds totalPages=" + totalPages);
        }
        List<PageConfiguration> pages = new ArrayList<>(numberOfGroups);

        int pagesPerGroup = totalPages / numberOfGroups;
        int remainingPages = totalPages % numberOfGroups;

        int startPage = 1;
        for (int i = 0; i < numberOfGroups; i++) {
            int endPage = startPage + pagesPerGroup - 1;
            if (remainingPages > 0) {
                endPage++;
                remainingPages--;
            }

            if (endPage > totalPages) {
                endPage = totalPages; // Ensure the last group doesn't exceed total pages
            }

            pages.add(new PageConfiguration(startPage, endPage));

            startPage = endPage + 1;
        }

        return pages;
    }
}
