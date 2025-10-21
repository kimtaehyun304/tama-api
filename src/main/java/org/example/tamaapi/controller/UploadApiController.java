package org.example.tamaapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tamaapi.aspect.PreAuthentication;
import org.example.tamaapi.domain.item.*;
import org.example.tamaapi.dto.UploadFile;
import org.example.tamaapi.dto.requestDto.item.save.*;
import org.example.tamaapi.dto.responseDto.SimpleResponse;
import org.example.tamaapi.repository.item.*;
import org.example.tamaapi.service.ItemService;
import org.example.tamaapi.service.S3Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
@RequiredArgsConstructor
public class UploadApiController {

    private final ColorItemRepository colorItemRepository;
    private final ItemService itemService;
    private final S3Service s3Service;

    @PostMapping("/api/items/images/new")
    @PreAuthentication
    @Secured("ROLE_ADMIN")
    public ResponseEntity<SimpleResponse> saveItemImages(@Valid @ModelAttribute SaveColorItemImageWrapperRequest wrapperRequest) {
        //이미지 파일인지 검증
        wrapperRequest.getRequests().forEach(req -> s3Service.areFilesImage(req.getFiles()));

        List<Long> colorItemIds = wrapperRequest.getRequests().stream().map(SaveColorItemImageRequest::getColorItemId).toList();
        List<ColorItem> colorItems = colorItemRepository.findAllById(colorItemIds);

        //colorItemImages 엔티티 생성
        Map<Long, List<UploadFile>> uploadFileMap = wrapperRequest.getRequests().stream()
                .collect(Collectors.toMap(
                        SaveColorItemImageRequest::getColorItemId,
                        ci -> {
                            List<MultipartFile> files = ci.getFiles();
                            return s3Service.storeFiles(files);
                        }
                ));

        List<ColorItemImage> colorItemImages = colorItems.stream()
                .flatMap(ci -> {
                    List<UploadFile> uploadFiles = uploadFileMap.get(ci.getId());
                    return IntStream.range(0, uploadFiles.size())  // 인덱스를 생성
                            .mapToObj(i -> new ColorItemImage(ci, uploadFiles.get(i), i + 1)); // 1부터 시작하는 순서
                })
                .toList();

        itemService.saveColorItemImages(colorItemImages);
        return ResponseEntity.status(HttpStatus.OK).body(new SimpleResponse("저장 성공"));
    }


}
