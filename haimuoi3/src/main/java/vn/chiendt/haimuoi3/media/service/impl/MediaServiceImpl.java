package vn.chiendt.haimuoi3.media.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import vn.chiendt.haimuoi3.media.dto.response.MediaUploadResponse;
import vn.chiendt.haimuoi3.media.model.MediaTargetType;
import vn.chiendt.haimuoi3.media.service.MediaService;
import vn.chiendt.haimuoi3.media.validator.MediaBusinessValidator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private final MediaBusinessValidator mediaBusinessValidator;

    @Value("${app.media.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public MediaUploadResponse uploadImage(MultipartFile file, MediaTargetType targetType) {
        mediaBusinessValidator.validateImage(file);
        String extension = extractExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + extension;

        try {
            Path targetDir = Paths.get(uploadDir, targetType.name().toLowerCase());
            Files.createDirectories(targetDir);
            Path targetPath = targetDir.resolve(fileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uploads/")
                    .path(targetType.name().toLowerCase())
                    .path("/")
                    .path(fileName)
                    .toUriString();
            return buildResponse(file, fileName, url);
        } catch (IOException e) {
            throw new IllegalArgumentException("cannot upload image file", e);
        }
    }

    private MediaUploadResponse buildResponse(MultipartFile file, String fileName, String url) {
        return MediaUploadResponse.builder()
                .fileName(fileName)
                .originalFileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .size(file.getSize())
                .url(url)
                .build();
    }

    private String extractExtension(String originalFileName) {
        if (originalFileName == null || !originalFileName.contains(".")) {
            return "";
        }
        return originalFileName.substring(originalFileName.lastIndexOf('.'));
    }
}
