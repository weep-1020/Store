package com.weep.mapper;

import com.weep.entity.Carousel;
import org.apache.ibatis.annotations.Param;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CarouselMapper {
    List<Carousel> findActive();
    List<Carousel> findAll();
    Carousel findById(@Param("id") Long id);
    int insert(Carousel carousel);
    int update(Carousel carousel);
    int deleteById(@Param("id") Long id);
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
