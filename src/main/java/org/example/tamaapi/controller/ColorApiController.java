package org.example.tamaapi.controller;

import lombok.RequiredArgsConstructor;
import org.example.tamaapi.domain.Category;
import org.example.tamaapi.domain.Color;
import org.example.tamaapi.dto.responseDto.category.CategoryResponse;
import org.example.tamaapi.dto.responseDto.item.ColorResponse;
import org.example.tamaapi.jwt.TokenProvider;
import org.example.tamaapi.repository.*;
import org.example.tamaapi.service.CacheService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
//카테고리 아이템은 itemApi
public class ColorApiController {

    private final ColorItemRepository colorItemRepository;
    private final ItemImageRepository itemImageRepository;
    private final MemberRepository memberRepository;
    private final CacheService cacheService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final TokenProvider tokenProvider;
    private final CategoryRepository categoryRepository;
    private final ColorRepository colorRepository;

    @GetMapping("/api/colors")
    public List<ColorResponse> category() {
        List<Color> colors = colorRepository.findAllByParentIsNull();
        return colors.stream().map(ColorResponse::new).toList();
    }

}
