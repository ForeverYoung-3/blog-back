package com.blog.back.controller;

import com.blog.back.dto.ApiResponse;
import com.blog.back.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final FileUploadService fileUploadService;

    /**
     * 上传图片
     * POST /api/files/upload/image
     * Content-Type: multipart/form-data
     * 参数: file（图片文件）
     * 返回: { "code": 200, "data": { "url": "http://..." } }
     */
    @PostMapping("/upload/image")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadImage(
            @RequestParam("file") MultipartFile file) {
        String url = fileUploadService.uploadImage(file);
        return ResponseEntity.ok(ApiResponse.success(Map.of("url", url)));
    }
}
