package io.banditoz.dohmap.database.migrator;

import java.time.OffsetDateTime;

public record Migration(String filename, String executedBy, OffsetDateTime executedWhen) {
}
