package com.weep.service;

import com.weep.entity.LargeFile;

/**
 * 大文件服务接口
 *
 * <p>提供大文件（主要是图片）的上传、查询、删除等业务逻辑。
 * 支持文件的存储和管理，用于商品图片、轮播图等场景。
 */
public interface LargeFileService {

    /**
     * 上传图片
     *
     * @param largeFile 大文件实体对象
     * @return 返回图片存储路径
     */
    String uploadImage(LargeFile largeFile);

    /**
     * 根据图片路径查询文件信息
     *
     * @param pic 图片路径
     * @return 大文件实体对象，不存在则返回null
     */
    LargeFile findByPic(String pic);

    /**
     * 根据图片路径删除文件
     *
     * @param pic 图片路径
     * @return 删除成功返回1，失败返回0
     */
    int deleteByPic(String pic);
}