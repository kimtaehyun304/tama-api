package org.example.tamaapi.service;

import lombok.RequiredArgsConstructor;
import org.example.tamaapi.config.S3Config;
import org.example.tamaapi.dto.UploadFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final S3Config s3Config;

    public List<UploadFile> storeFiles(List<MultipartFile> multipartFiles) {
        List<UploadFile> storeFileResult = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles) {
            if (!multipartFile.isEmpty()) {
                storeFileResult.add(storeFile(multipartFile));
            }
        }
        return storeFileResult;
    }

    public UploadFile storeFile(MultipartFile multipartFile) {
        if (multipartFile.isEmpty()) {
            return null;
        }

        String originalFilename = multipartFile.getOriginalFilename();
        String storedFileName = createStoredFileName(originalFilename);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(storedFileName)
                    .contentType(multipartFile.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(multipartFile.getBytes()));
        } catch (Exception e) {
            throw new IllegalArgumentException("파일 업로드 에러", e);
        }

        return new UploadFile(originalFilename, storedFileName);
    }

    private String createStoredFileName(String originalFileName) {
        String ext = extractExt(originalFileName);
        String name = extractName(originalFileName);
        String uuid = UUID.randomUUID().toString();
        return name + "-" + uuid + "." + ext;
    }

    private String extractExt(String originalFileName) {
        int pos = originalFileName.lastIndexOf(".");
        return originalFileName.substring(pos + 1);
    }

    private String extractName(String originalFileName) {
        int pos = originalFileName.lastIndexOf(".");
        return originalFileName.substring(0, pos);
    }

    public void areFilesImage(List<MultipartFile> files) {
        for (MultipartFile file : files) {
            String contentType = file.getContentType();
            if (!contentType.startsWith("image/")) {
                throw new IllegalArgumentException("이미지 파일이 아닙니다");
            }
        }
    }
}
