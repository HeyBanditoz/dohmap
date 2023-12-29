package io.banditoz.dohmap.scraper.page.base;

import org.junit.jupiter.api.Test;

import static io.banditoz.dohmap.scraper.page.base.PageConfiguration.dividePages;
import static org.assertj.core.api.Assertions.*;

class PageConfigurationTest {
    @Test
    void dividePages_over() {
        assertThatThrownBy(() -> dividePages(136, 137)).isInstanceOf(IllegalArgumentException.class);
        assertThatCode(() -> dividePages(136, 12)).doesNotThrowAnyException();
    }
}