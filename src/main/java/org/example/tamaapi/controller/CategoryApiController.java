package org.example.tamaapi.controller;

import lombok.RequiredArgsConstructor;
import org.example.tamaapi.domain.Category;
import org.example.tamaapi.dto.responseDto.category.CategoryResponse;
import org.example.tamaapi.jwt.TokenProvider;
import org.example.tamaapi.repository.CategoryRepository;
import org.example.tamaapi.repository.ColorItemRepository;
import org.example.tamaapi.repository.ItemImageRepository;
import org.example.tamaapi.repository.MemberRepository;
import org.example.tamaapi.service.CacheService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
//카테고리 아이템은 itemApi
public class CategoryApiController {

    private final ColorItemRepository colorItemRepository;
    private final ItemImageRepository itemImageRepository;
    private final MemberRepository memberRepository;
    private final CacheService cacheService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final TokenProvider tokenProvider;
    private final CategoryRepository categoryRepository;

    @GetMapping("/api/category")
    public List<CategoryResponse> category() {
        List<Category> categories = categoryRepository.findAllWithChildrenAllByParentIsNull();
        return categories.stream().map(CategoryResponse::new).toList();
    }

    @GetMapping("/api/category/{categoryId}")
    public CategoryResponse category(@PathVariable Long categoryId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new IllegalArgumentException("존재하지않는 카테고리입니다."));
        CategoryResponse categoryResponse = new CategoryResponse(category);
        return categoryResponse;
    }

}
