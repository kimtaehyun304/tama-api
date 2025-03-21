package org.example.tamaapi.controller;

import lombok.RequiredArgsConstructor;
import org.example.tamaapi.domain.item.Color;
import org.example.tamaapi.dto.responseDto.color.ColorResponse;
import org.example.tamaapi.dto.responseDto.color.ParentColorResponse;
import org.example.tamaapi.jwt.TokenProvider;
import org.example.tamaapi.repository.*;
import org.example.tamaapi.repository.item.CategoryRepository;
import org.example.tamaapi.repository.item.ColorItemRepository;
import org.example.tamaapi.repository.item.ColorRepository;
import org.example.tamaapi.repository.item.ColorItemImageRepository;
import org.example.tamaapi.service.CacheService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
//카테고리 아이템은 itemApi
public class ColorApiController {

    private final ColorItemRepository colorItemRepository;
    private final ColorItemImageRepository colorItemImageRepository;
    private final MemberRepository memberRepository;
    private final CacheService cacheService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final TokenProvider tokenProvider;
    private final CategoryRepository categoryRepository;
    private final ColorRepository colorRepository;

    @GetMapping("/api/colors/parent")
    public List<ParentColorResponse> parentColors() {
        List<Color> colors = colorRepository.findAllByParentIsNull();
        return colors.stream().map(ParentColorResponse::new).toList();
    }


    @GetMapping("/api/colors")
    public List<ColorResponse> colors() {
        List<Color> colors = colorRepository.findAllWithChildrenByParentIsNull();
        return colors.stream().map(ColorResponse::new).toList();
    }

}
