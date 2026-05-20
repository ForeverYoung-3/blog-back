package com.blog.back.service.impl;

import com.blog.back.config.MinioConfig;
import com.blog.back.exception.BusinessException;
import com.blog.back.service.FileUploadService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    /** 允许上传的图片类型 */
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif",
            "image/webp", "image/svg+xml"
    );

    /** 单文件最大 10 MB */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024L;

    @Override
    public String uploadImage(MultipartFile file) {
        // 1. 校验文件
        if (file == null || file.isEmpty()) {
            throw BusinessException.badRequest("上传文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw BusinessException.badRequest("文件大小不能超过 10MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw BusinessException.badRequest("仅支持 JPG / PNG / GIF / WebP / SVG 格式");
        }

        // 2. 生成唯一文件名：images/年月/uuid.扩展名
        String originalFilename = file.getOriginalFilename();
        String extension = getExtension(originalFilename, contentType);
        String objectName = "images/" + java.time.LocalDate.now().toString().replace("-", "/")
                + "/" + UUID.randomUUID().toString().replace("-", "") + "." + extension;

        // 3. 上传到 MinIO
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(contentType)
                            .build()
            );
        } catch (Exception e) {
            log.error("MinIO 上传失败: {}", e.getMessage(), e);
            throw BusinessException.badRequest("文件上传失败，请稍后重试");
        }

        // 4. 拼接公开访问 URL（使用对外地址，而非容器内部地址）
        String url = minioConfig.getPublicEndpointOrDefault().stripTrailing()
                + "/" + minioConfig.getBucketName()
                + "/" + objectName;

        log.info("文件上传成功: {}", url);
        return url;
    }

    /** 从原始文件名或 ContentType 推断扩展名 */
    private String getExtension(String filename, String contentType) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        }
        return switch (contentType.toLowerCase()) {
            case "image/jpeg", "image/jpg" -> "jpg";
            case "image/png"               -> "png";
            case "image/gif"               -> "gif";
            case "image/webp"              -> "webp";
            case "image/svg+xml"           -> "svg";
            default                        -> "jpg";
        };
    }
}
