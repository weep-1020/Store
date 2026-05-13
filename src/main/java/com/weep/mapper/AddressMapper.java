package com.weep.mapper;

import com.weep.entity.Address;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 收货地址 Mapper 接口
 */
@Mapper
public interface AddressMapper {

    /**
     * 根据ID查询地址
     */
    Address findById(@Param("id") Long id);

    /**
     * 查询用户的所有地址（按默认地址排序）
     */
    List<Address> findByUserId(@Param("userId") Long userId);

    /**
     * 查询用户的默认地址
     */
    Address findDefaultByUserId(@Param("userId") Long userId);

    /**
     * 添加地址
     */
    int insert(Address address);

    /**
     * 更新地址
     */
    int update(Address address);

    /**
     * 删除地址
     */
    int deleteById(@Param("id") Long id);

    /**
     * 取消用户的所有默认地址
     */
    int cancelDefaultByUserId(@Param("userId") Long userId);

    /**
     * 设置默认地址
     */
    int setDefaultAddress(@Param("id") Long id);
}
