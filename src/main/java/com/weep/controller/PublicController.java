package com.weep.controller;

import com.weep.entity.LargeFile;
import com.weep.response.Result;
import com.weep.service.FileStorageService;
import com.weep.service.LargeFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 公共服务控制器
 *
 * <p>负责处理公共资源相关的 REST API 请求，主要包括图片上传、下载和管理功能。
 * 采用磁盘存储方案，图片以二进制形式存储在服务器文件系统，数据库仅存储元数据。
 *
 * <p>主要功能：
 * <ul>
 *   <li>图片上传（Base64格式，最大10MB）</li>
 *   <li>图片下载（二进制流，支持浏览器直接显示）</li>
 *   <li>图片Base64编码查询</li>
 *   <li>图片删除</li>
 *   <li>文件格式验证（PNG、JPG、GIF、WebP）</li>
 * </ul>
 *
 * <p>存储策略：
 * <ul>
 *   <li>文件路径：uploads/images/{year}/{month}/{day}/{uuid}</li>
 *   <li>数据库字段：pic(UUID)、filename、filePath、fileSize、contentType</li>
 * </ul>
 *
 * @author Weep
 * @version 1.0
 */
@RestController
@RequestMapping("/api/public")
public class PublicController {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(PublicController.class);

    /**
     * 大文件服务接口
     * 处理图片元数据的数据库操作
     */
    @Autowired
    private LargeFileService largeFileService;

    /**
     * 文件存储服务接口
     * 处理图片文件的磁盘读写操作
     */
    @Autowired
    private FileStorageService fileStorageService;

    /**
     * 上传图片
     *
     * <p>需要登录认证。接收Base64编码的图片数据，验证后存储到磁盘，
     * 并在数据库中记录元数据。支持PNG、JPG、GIF、WebP格式，最大10MB。
     *
     * <p>处理流程：
     * <ol>
     *   <li>解析Base64数据</li>
     *   <li>验证文件大小（≤10MB）</li>
     *   <li>验证文件格式（检查文件头魔数）</li>
     *   <li>生成唯一UUID作为文件ID</li>
     *   <li>存储到磁盘（uploads/images/...）</li>
     *   <li>保存元数据到数据库</li>
     * </ol>
     *
     * @param request 上传请求体，包含以下字段：
     *                <ul>
     *                  <li>filename: 原始文件名（必填）</li>
     *                  <li>base64Content: Base64编码的图片内容（必填，可带data:image前缀）</li>
     *                </ul>
     * @return 上传结果
     *         <ul>
     *           <li>200: 上传成功，data包含imageId、filename、size</li>
     *           <li>400: 参数错误（文件名为空、格式不支持、文件过大等）</li>
     *           <li>401: 未认证</li>
     *           <li>500: 上传失败（磁盘写入失败、数据库保存失败等）</li>
     *         </ul>
     */
    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MERCHANT', 'USER')")
    public Result<Map<String, Object>> uploadImage(@RequestBody Map<String, String> request) {
        // 提取请求参数
        String filename = request.get("filename");
        String base64Content = request.get("base64Content");

        // 参数校验：文件名不能为空
        if (filename == null || filename.trim().isEmpty()) {
            return Result.error(400, "文件名不能为空");
        }
        // 参数校验：图片内容不能为空
        if (base64Content == null || base64Content.trim().isEmpty()) {
            return Result.error(400, "图片内容不能为空");
        }

        // 处理Base64数据：移除data:image前缀（如果存在）
        String cleanBase64 = base64Content;
        if (base64Content.contains(",")) {
            cleanBase64 = base64Content.split(",", 2)[1];
        }

        // 解码Base64为字节数组
        byte[] content;
        try {
            content = Base64.getDecoder().decode(cleanBase64);
        } catch (IllegalArgumentException e) {
            return Result.error(400, "Base64 格式错误");
        }

        // 验证文件大小：最大10MB
        long maxSize = 10 * 1024 * 1024;
        if (content.length > maxSize) {
            return Result.error(400, "图片大小不能超过10MB");
        }

        // 验证文件格式：检查文件头魔数
        if (!isValidImage(content)) {
            return Result.error(400, "不支持的文件格式或文件已损坏");
        }

        // 生成唯一的图片ID（UUID）
        String imageId = UUID.randomUUID().toString();

        // 存储文件到磁盘
        String filePath;
        try {
            filePath = fileStorageService.store(content, imageId);
        } catch (RuntimeException e) {
            logger.error("文件写入磁盘失败", e);
            return Result.error(500, "上传失败: " + e.getMessage());
        }

        // 构建图片元数据对象
        LargeFile largeFile = new LargeFile();
        largeFile.setPic(imageId);
        largeFile.setFilename(filename);
        largeFile.setFilePath(filePath);
        largeFile.setFileSize((long) content.length);
        largeFile.setContentType(guessContentType(filename));

        // 保存元数据到数据库
        try {
            largeFileService.uploadImage(largeFile);
        } catch (IllegalArgumentException e) {
            // 数据库保存失败，删除已上传的文件（回滚）
            fileStorageService.delete(imageId);
            return Result.error(400, e.getMessage());
        } catch (RuntimeException e) {
            // 数据库保存失败，删除已上传的文件（回滚）
            fileStorageService.delete(imageId);
            logger.error("图片元数据保存失败", e);
            return Result.error(500, "上传失败: " + e.getMessage());
        }

        // 组装返回数据
        Map<String, Object> data = new HashMap<>();
        data.put("imageId", imageId);
        data.put("filename", filename);
        data.put("size", content.length);

        logger.info("图片上传成功 - ID: {}, 文件名: {}, 大小: {} bytes", imageId, filename, content.length);
        return Result.success("上传成功", data);
    }

    /**
     * 获取图片（二进制流）
     *
     * <p>公开访问，无需认证。以二进制流形式返回图片，支持浏览器直接显示。
     * 设置了1小时的公共缓存，提高加载速度。
     *
     * @param imageId 图片ID（UUID）
     * @return 图片二进制流
     *         <ul>
     *           <li>200: 成功，返回图片内容（Content-Type根据格式设置）</li>
     *           <li>400: 图片ID为空</li>
     *           <li>404: 图片不存在</li>
     *         </ul>
     */
    @GetMapping(value = "/{imageId}")
    public ResponseEntity<byte[]> getImageAsBinary(@PathVariable String imageId) {
        // 参数校验
        if (imageId == null || imageId.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // 查询图片元数据
        LargeFile image = largeFileService.findByPic(imageId);
        if (image == null) {
            return ResponseEntity.notFound().build();
        }

        // 从磁盘加载图片内容
        byte[] content = fileStorageService.load(imageId);
        if (content == null || content.length == 0) {
            return ResponseEntity.notFound().build();
        }

        // 设置Content-Type
        String contentType = image.getContentType() != null ? image.getContentType() : "application/octet-stream";

        // 设置Content-Disposition为inline（浏览器直接显示）
        ContentDisposition contentDisposition = ContentDisposition.builder("inline")
                .filename(image.getFilename(), StandardCharsets.UTF_8)
                .build();

        // 构建响应：设置缓存控制（1小时公共缓存）
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic())
                .body(content);
    }

    /**
     * 获取图片（Base64编码）
     *
     * <p>公开访问，无需认证。返回Base64编码的图片数据，适用于某些特殊场景
     * （如邮件嵌入、API数据传输等）。
     *
     * @param imageId 图片ID（UUID）
     * @return Base64编码的图片数据
     *         <ul>
     *           <li>200: 查询成功，data包含imageId、filename、base64Content、size</li>
     *           <li>404: 图片不存在</li>
     *         </ul>
     */
    @GetMapping(value = "/{imageId}/base64")
    public Result<Map<String, Object>> getImageAsBase64(@PathVariable String imageId) {
        // 查询图片元数据
        LargeFile image = largeFileService.findByPic(imageId);
        if (image == null) {
            return Result.error(404, "图片不存在");
        }

        // 从磁盘加载图片内容
        byte[] content = fileStorageService.load(imageId);
        if (content == null || content.length == 0) {
            return Result.error(404, "图片文件不存在");
        }

        // 编码为Base64
        String base64 = Base64.getEncoder().encodeToString(content);

        // 组装返回数据
        Map<String, Object> data = new HashMap<>();
        data.put("imageId", image.getPic());
        data.put("filename", image.getFilename());
        data.put("base64Content", base64);
        data.put("size", content.length);

        return Result.success("查询成功", data);
    }

    /**
     * 删除图片
     *
     * <p>需要登录认证。删除指定的图片，同时删除数据库记录和磁盘文件。
     *
     * @param imageId 图片ID（UUID）
     * @return 删除结果
     *         <ul>
     *           <li>200: 删除成功</li>
     *           <li>400: 图片ID为空</li>
     *           <li>404: 图片不存在</li>
     *           <li>500: 删除失败</li>
     *         </ul>
     */
    @DeleteMapping("/{imageId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MERCHANT', 'USER')")
    public Result<Void> deleteImage(@PathVariable String imageId) {
        // 参数校验
        if (imageId == null || imageId.trim().isEmpty()) {
            return Result.error(400, "图片ID不能为空");
        }

        try {
            // 删除数据库记录
            int deleted = largeFileService.deleteByPic(imageId);
            if (deleted > 0) {
                // 删除磁盘文件
                fileStorageService.delete(imageId);
                logger.info("图片删除成功 - ID: {}", imageId);
                return Result.success("删除成功", null);
            } else {
                return Result.error(404, "图片不存在");
            }
        } catch (Exception e) {
            logger.error("图片删除失败 - ID: {}", imageId, e);
            return Result.error(500, "删除失败: " + e.getMessage());
        }
    }

    /**
     * 权限测试接口
     *
     * <p>仅 USER 角色可访问，用于测试权限配置是否正确。
     *
     * @return 测试结果
     */
    @GetMapping("/test1")
    @PreAuthorize("hasRole('USER')")
    public Result<String> testAuth() {
        return Result.success("权限认证通过", "user role");
    }

    /**
     * 根据文件名猜测Content-Type
     *
     * <p>根据文件扩展名判断MIME类型，用于设置HTTP响应的Content-Type头。
     *
     * @param filename 文件名
     * @return MIME类型字符串
     */
    private String guessContentType(String filename) {
        if (filename == null) {
            return "application/octet-stream";
        }

        // 提取文件扩展名
        int dot = filename.lastIndexOf('.');
        if (dot == -1) {
            return "application/octet-stream";
        }

        String ext = filename.substring(dot + 1).toLowerCase();

        // 根据扩展名返回对应的MIME类型
        switch (ext) {
            case "png":  return "image/png";
            case "jpg":
            case "jpeg": return "image/jpeg";
            case "gif":  return "image/gif";
            case "webp": return "image/webp";
            default:     return "application/octet-stream";
        }
    }

    /**
     * 验证图片格式
     *
     * <p>通过检查文件头的魔数（Magic Number）来验证文件格式，
     * 防止恶意文件上传。支持PNG、JPG、GIF、WebP格式。
     *
     * @param data 文件字节数组
     * @return true-是有效的图片格式，false-不是图片或格式不支持
     */
    private boolean isValidImage(byte[] data) {
        // 数据有效性检查：至少12字节才能判断格式
        if (data == null || data.length < 12) {
            return false;
        }

        // PNG格式：文件头为 89 50 4E 47
        if (data[0] == (byte) 0x89 && data[1] == (byte) 0x50
                && data[2] == (byte) 0x4E && data[3] == (byte) 0x47) {
            return true;
        }

        // JPG格式：文件头为 FF D8
        if (data[0] == (byte) 0xFF && data[1] == (byte) 0xD8) {
            return true;
        }

        // GIF格式：文件头为 47 49 46 38 或 47 49 46 39
        if (data[0] == (byte) 0x47 && data[1] == (byte) 0x49 && data[2] == (byte) 0x46
                && (data[3] == (byte) 0x38 || data[3] == (byte) 0x39)) {
            return true;
        }

        // WebP格式：文件头为 RIFF....WEBP
        if (data[0] == (byte) 0x52 && data[1] == (byte) 0x49 && data[2] == (byte) 0x46 && data[3] == (byte) 0x46
                && data[8] == (byte) 0x57 && data[9] == (byte) 0x45 && data[10] == (byte) 0x42 && data[11] == (byte) 0x50) {
            return true;
        }

        return false;
    }
}
