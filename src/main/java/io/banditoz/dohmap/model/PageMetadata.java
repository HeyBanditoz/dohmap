package io.banditoz.dohmap.model;

public record PageMetadata(int page, int maxPages, int startAt, long countTimeElapsedMs, long searchTimeElapsedMs) {
}
