package pl.propertyrentalmanager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import pl.propertyrentalmanager.common.pagination.PageResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageResponseTest {

    @Test
    @DisplayName("Should correctly map Spring Data Page to PageResponse")
    void shouldMapSpringDataPageToPageResponse() {
        List<String> items = List.of("Item A", "Item B");
        Page<String> springPage = new PageImpl<>(items, PageRequest.of(0, 10), 25);

        PageResponse<String> pageResponse = PageResponse.from(springPage);

        assertThat(pageResponse.content()).containsExactly("Item A", "Item B");
        assertThat(pageResponse.page()).isZero();
        assertThat(pageResponse.size()).isEqualTo(10);
        assertThat(pageResponse.totalElements()).isEqualTo(25);
        assertThat(pageResponse.totalPages()).isEqualTo(3);
        assertThat(pageResponse.first()).isTrue();
        assertThat(pageResponse.last()).isFalse();
        assertThat(pageResponse.empty()).isFalse();
    }

    @Test
    @DisplayName("Should correctly map PageResponse with mapper function")
    void shouldMapPageResponseWithMapper() {
        List<Integer> numbers = List.of(1, 2, 3);
        Page<Integer> springPage = new PageImpl<>(numbers, PageRequest.of(0, 5), 3);

        PageResponse<String> pageResponse = PageResponse.from(springPage, num -> "Num: " + num);

        assertThat(pageResponse.content()).containsExactly("Num: 1", "Num: 2", "Num: 3");
        assertThat(pageResponse.totalElements()).isEqualTo(3);
        assertThat(pageResponse.last()).isTrue();
    }
}
