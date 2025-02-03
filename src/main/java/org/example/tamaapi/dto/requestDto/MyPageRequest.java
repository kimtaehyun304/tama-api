package org.example.tamaapi.dto.requestDto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import org.springframework.data.domain.Sort;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class MyPageRequest {

    //현재 페이지
    @Positive
    @NotNull
    int page;

    //한 페이지에 들어갈 아이템 수. (몇개로 묶을건지)
    @Positive
    @NotNull
    int size;

    Sort sort;

    public MyPageRequest(int page, int size, List<String> sort) {
        this.page = page;
        this.size = size;
        this.sort = parseSort(sort);
    }

    private Sort parseSort(List<String> sort) {

        if (sort == null || sort.isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "createdAt", "id");
        }

        List<Sort.Order> orders = sort.stream()
                .map(param -> {
                            String[] parts = param.split("-");
                            String property = parts[0].trim();
                            Sort.Direction direction = (parts.length > 1) ? Sort.Direction.fromString(parts[1].trim()) : Sort.Direction.ASC;
                            return new Sort.Order(direction, property, );
                        }
                ).collect(Collectors.toList());
        orders.add(new Sort.Order(Sort.Direction.DESC, "id"));

        return Sort.by(orders);
    }


}
