package com.weep.mapper;

import com.weep.entity.LargeFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LargeFileMapper {
    public int add(LargeFile largefile);
    public LargeFile findByPic(String pic);
    int deleteByPic(@Param("pic") String pic);}
