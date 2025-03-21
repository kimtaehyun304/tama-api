package org.example.tamaapi.util;

import org.example.tamaapi.dto.UploadFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class FileStore {

    @Value("${file.dir}")
    private String fileDir;

    public String getFullPath(String fileName){
        return fileDir + fileName;
    }

    public List<UploadFile> storeFiles(List<MultipartFile> multipartFiles) {
        List<UploadFile> storeFileResult  = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles) {
            if(!multipartFile.isEmpty())
                storeFileResult.add(storeFile(multipartFile));
        }
        return storeFileResult;
    }


    // 똑같은 파일 이름 존재하는지 검증 필요
    public UploadFile storeFile(MultipartFile multipartFile)  {
        if(multipartFile.isEmpty())
            return null;

        String originalFilename = multipartFile.getOriginalFilename();
        String storedFileName = createStoredFileName(originalFilename);

        //로컬 업로드
        try {
            multipartFile.transferTo(new File(getFullPath(storedFileName)));
        } catch (Exception e){
            throw new IllegalArgumentException("파일 저장 에러");
        }

        return new UploadFile(originalFilename, storedFileName);
    }

    private String createStoredFileName(String originalFileName){
        String ext = extractExt(originalFileName);
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + ext;
    }

    private String extractExt(String originalFileName){
        int pos = originalFileName.lastIndexOf(".");
        return originalFileName.substring(pos + 1);
    }


}
