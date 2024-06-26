package io.banditoz.dohmap.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WorkQueueTests {
    @Test
    void testWorkQueue_happyPath() {
        WorkQueue<String> queue = new WorkQueue<>("the", "quick", "brown", "fox");
        assertThat(queue.getNextItem()).isEqualTo("the");
        assertThat(queue.getNextItem()).isEqualTo("quick");
        assertThat(queue.getNextItem()).isEqualTo("brown");
        assertThat(queue.getNextItem()).isEqualTo("fox");
        assertThat(queue.getNextItem()).isNull();
        assertThat(queue.getNextItem()).isNull();
    }

    @Test
    void testWorkQueue_offset() {
        WorkQueue<String> queue = new WorkQueue<>("the", "quick", "brown", "fox");
        assertThat(queue.offsetToFirstOccurrence("brown")).isEqualTo(2);
        assertThat(queue.getNextItem()).isEqualTo("brown");
        assertThat(queue.getNextItem()).isEqualTo("fox");
        assertThat(queue.getNextItem()).isNull();
        assertThat(queue.getNextItem()).isNull();
    }

    @Test
    void testWorkQueue_offset_notFound() {
        WorkQueue<String> queue = new WorkQueue<>("the", "quick", "brown", "fox");
        assertThat(queue.offsetToFirstOccurrence("jumped")).isEqualTo(-1);
        assertThat(queue.getNextItem()).isEqualTo("the");
        assertThat(queue.getNextItem()).isEqualTo("quick");
        assertThat(queue.getNextItem()).isEqualTo("brown");
        assertThat(queue.getNextItem()).isEqualTo("fox");
        assertThat(queue.getNextItem()).isNull();
        assertThat(queue.getNextItem()).isNull();
    }

    @Test
    void testWorkQueue_oneElement() {
        WorkQueue<String> queue = new WorkQueue<>("the");
        assertThat(queue.getNextItem()).isEqualTo("the");
        assertThat(queue.getNextItem()).isNull();
        assertThat(queue.getNextItem()).isNull();
    }

    @Test
    void testWorkQueue_skipFirst() {
        WorkQueue<String> queue = new WorkQueue<>("aa", "ab", "ac", "ad");
        assertThat(queue.offsetToFirstOccurrence("aa")).isEqualTo(0);
        assertThat(queue.getNextItem()).isEqualTo("aa");
        assertThat(queue.getNextItem()).isEqualTo("ab");
        assertThat(queue.getNextItem()).isEqualTo("ac");
        assertThat(queue.getNextItem()).isEqualTo("ad");
        assertThat(queue.getNextItem()).isNull();
        assertThat(queue.getNextItem()).isNull();
    }

    @Test
    void testWorkQueue_intFilling() {
        WorkQueue<Integer> queue = WorkQueue.fillOneToN(3);
        assertThat(queue.getNextItem()).isEqualTo(1);
        assertThat(queue.getNextItem()).isEqualTo(2);
        assertThat(queue.getNextItem()).isEqualTo(3);
        assertThat(queue.getNextItem()).isNull();
        assertThat(queue.getNextItem()).isNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    void testWorkQueue_noElement() {
        assertThatThrownBy(WorkQueue::new).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testWorkQueue_nullElements() {
        assertThatThrownBy(() -> new WorkQueue<>("the", "quick", null, "fox")).isInstanceOf(NullPointerException.class);
    }
}