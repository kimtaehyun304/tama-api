package org.example.tamaapi.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.dto.CharacterCreateRequest;
import org.example.tamaapi.dto.CharacterRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
public class TestController {

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


}
