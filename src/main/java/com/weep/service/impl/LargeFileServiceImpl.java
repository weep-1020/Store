package com.weep.service.impl;

import com.weep.entity.LargeFile;
import com.weep.mapper.LargeFileMapper;
import com.weep.service.FileStorageService;
import com.weep.service.LargeFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 大文件服务实现类
 *
 * <p>实现大文件（如图片）的上传、查询和删除操作。
 * 负责管理文件的元数据存储和物理文件的操作。
 */
@Service
public class LargeFileServiceImpl implements LargeFileService {

    private static final Logger logger = LoggerFactory.getLogger(LargeFileServiceImpl.class);

    @Autowired
    private LargeFileMapper largeFileMapper;

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * 根据图片ID删除文件（同时删除元数据和物理文件）
     *
     * @param pic 图片ID
     * @return 删除的记录数
     */
    @Override
    @Transactional
    public int deleteByPic(String pic) {
        logger.info("删除图片 - ID: {}", pic);
        LargeFile record = largeFileMapper.findByPic(pic);
        if (record != null) {
            fileStorageService.delete(pic);
        }
        return largeFileMapper.deleteByPic(pic);
    }

    /**
     * 上传图片并保存元数据
     *
     * <p>校验规则：
     * <ul>
     *   <li>文件对象不能为空</li>
     *   <li>文件名不能为空</li>
     *   <li>文件大小必须大于0</li>
     * </ul>
     *
     * <p>如果图片ID为空，会自动生成UUID作为图片ID。
     *
     * @param largeFile 文件对象（包含filename、fileSize等字段）
     * @return 图片ID
     * @throws IllegalArgumentException 当参数校验失败时抛出
     * @throws RuntimeException 当数据库插入失败时抛出
     */
    @Override
    @Transactional
    public String uploadImage(LargeFile largeFile) {
        if (largeFile == null) {
            logger.error("上传失败：LargeFile对象为空");
            throw new IllegalArgumentException("文件对象不能为空");
        }
        if (largeFile.getFilename() == null || largeFile.getFilename().trim().isEmpty()) {
            logger.error("上传失败：文件名为空");
            throw new IllegalArgumentException("文件名不能为空");
        }
        if (largeFile.getFileSize() == null || largeFile.getFileSize() <= 0) {
            logger.error("上传失败：文件大小无效");
            throw new IllegalArgumentException("文件大小无效");
        }

        if (largeFile.getPic() == null || largeFile.getPic().trim().isEmpty()) {
            largeFile.setPic(UUID.randomUUID().toString());
        }

        try {
            int result = largeFileMapper.add(largeFile);
            if (result > 0) {
                logger.info("图片元数据保存成功 - ID: {}, 文件名: {}", largeFile.getPic(), largeFile.getFilename());
                return largeFile.getPic();
            } else {
                throw new RuntimeException("数据库插入失败，影响行数为0");
            }
        } catch (Exception e) {
            logger.error("图片上传异常", e);
            throw new RuntimeException("上传图片失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据图片ID查询文件元数据
     *
     * @param pic 图片ID
     * @return 文件对象，不存在则返回null
     * @throws RuntimeException 当查询异常时抛出
     */
    @Override
    public LargeFile findByPic(String pic) {
        if (pic == null || pic.trim().isEmpty()) {
            logger.warn("查询失败：图片ID为空");
            return null;
        }
        try {
            LargeFile result = largeFileMapper.findByPic(pic);
            if (result == null) {
                logger.warn("未找到图片记录 - ID: {}", pic);
            }
            return result;
        } catch (Exception e) {
            logger.error("查询图片异常 - ID: {}, 错误: {}", pic, e.getMessage(), e);
            throw new RuntimeException("查询图片失败: " + e.getMessage(), e);
        }
    }
}