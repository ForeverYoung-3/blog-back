package com.blog.back.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {

    /**
     * 上传图片到 MinIO
     *
     * @param file 上传的文件
     * @return 可公开访问的图片 URL
     */
    String uploadImage(MultipartFile file);
}
