package vn.chiendt.haimuoi3.media.service;

import org.springframework.web.multipart.MultipartFile;
import vn.chiendt.haimuoi3.media.dto.response.MediaUploadResponse;
import vn.chiendt.haimuoi3.media.model.MediaTargetType;

public interface MediaService {

    MediaUploadResponse uploadImage(MultipartFile file, MediaTargetType targetType);
}
