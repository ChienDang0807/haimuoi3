package vn.chiendt.haimuoi3.media.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaUploadResponse {
    private String fileName;
    private String originalFileName;
    private String contentType;
    private long size;
    private String url;
}
