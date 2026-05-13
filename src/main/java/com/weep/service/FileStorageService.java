package com.weep.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    @Value("${file.upload-dir:./uploads/images}")
    private String uploadDir;

    private Path rootPath;

    @PostConstruct
    public void init() {
        rootPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootPath);
            logger.info("文件存储目录已初始化: {}", rootPath);
        } catch (IOException e) {
            logger.error("无法创建文件存储目录: {}", rootPath, e);
            throw new RuntimeException("无法创建文件存储目录", e);
        }
    }

    public Path getRootPath() {
        return rootPath;
    }

    public byte[] load(String imageId) {
        Path filePath = resolvePath(imageId);
        if (!Files.exists(filePath)) {
            return null;
        }
        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            logger.error("读取文件失败: {}", filePath, e);
            return null;
        }
    }

    public String store(byte[] data, String imageId) {
        try {
            Path filePath = resolvePath(imageId);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, data);
            logger.info("文件已存储: {} ({} bytes)", filePath, data.length);
            return filePath.toString();
        } catch (IOException e) {
            logger.error("存储文件失败: {}", imageId, e);
            throw new RuntimeException("存储文件失败", e);
        }
    }

    public boolean delete(String imageId) {
        Path filePath = resolvePath(imageId);
        try {
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            logger.error("删除文件失败: {}", filePath, e);
            return false;
        }
    }

    private Path resolvePath(String imageId) {
        if (imageId == null || imageId.contains("..") || imageId.contains("/") || imageId.contains("\\")) {
            throw new IllegalArgumentException("非法的文件标识符");
        }
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return rootPath.resolve(dateDir).resolve(imageId);
    }
}
