package org.example.tamaapi.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.domain.item.Item;
import org.example.tamaapi.dto.CharacterCreateRequest;
import org.example.tamaapi.dto.CharacterRequest;
import org.example.tamaapi.dto.requestDto.LoginRequest;
import org.example.tamaapi.repository.item.ColorItemImageRepository;
import org.example.tamaapi.repository.item.ItemRepository;
import org.example.tamaapi.service.PortOneService;
import org.example.tamaapi.util.ErrorMessageUtil;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
public class TestController {

    private final ColorItemImageRepository colorItemImageRepository;
    private final PortOneService portOneService;
    private final ItemRepository itemRepository;
    /*
    @PostMapping(value = "/api/v1/character")
    public void saveCharacter(@RequestBody CharacterCreateRequest request) {
        log.info("이름 : {}, 나이 : {}", request.getAge(), request.getName());
        //log.info("이름 : {}, 나이 : {}, 이미지 : {}", request.getAge(), request.getName(), imgFile);
    }

    @PostMapping(value = "/api/v2/character")
    public void saveCharacter2(@ModelAttribute CharacterRequest request, @RequestHeader("Content-Type") String contentType) {
        log.info("contentType: {}", contentType);
        log.info("성별: {}", request.getGender());
    }

    @PostMapping(value = "/api/v3/character")
    public void saveCharacter3(@RequestBody CharacterRequest request , @RequestHeader("Content-Type") String contentType) {
        log.info("contentType: {}", contentType);
        log.info("성별: {}", request.getGender());
    }

    @PostMapping(value = "/api/v4/character")
    public void saveCharacter4(@RequestPart CharacterRequest request , @RequestHeader("Content-Type") String contentType) {
        log.info("contentType: {}", contentType);
        log.info("성별: {}", request.getGender());
    }

    @PostMapping(value = "/api/v5/character")
    public void saveCharacter5(@RequestPart String gender , @RequestHeader("Content-Type") String contentType) {
        log.info("contentType: {}", contentType);
        log.info("성별: {}", gender);
    }

    @PostMapping(value = "/api/v6/character")
    public void saveCharacter6(@RequestBody String gender , @RequestHeader("Content-Type") String contentType) {
        log.info("contentType: {}", contentType);
        log.info("성별: {}", gender);
    }



    @GetMapping(value = "/test")
    public String find() {
        String name = "여 코듀로이 와이드 팬츠0";
        Item item = itemRepository.findWithColorItemByName(name)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessageUtil.NOT_FOUND_ITEM));
        return "OK";
    }

    @GetMapping(value = "/test/getBody")
    public String testRequestBody(@RequestBody LoginRequest loginRequest) {
        System.out.println("loginRequest.getEmail() = " + loginRequest.getEmail());
        System.out.println("loginRequest.getPassword() = " + loginRequest.getPassword());
        return "OK";
    }

    @PostMapping(value = "/test/getBody")
    public String testRequestBody2(@RequestBody LoginRequest loginRequest) {
        System.out.println("loginRequest.getEmail() = " + loginRequest.getEmail());
        System.out.println("loginRequest.getPassword() = " + loginRequest.getPassword());
        return "OK";
    }
    */
}
