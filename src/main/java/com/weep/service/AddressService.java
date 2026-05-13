package com.weep.service;

import com.weep.entity.Address;

import java.util.List;

/**
 * 收货地址服务接口
 *
 * <p>提供用户收货地址的完整管理功能，包括地址的增删改查、默认地址设置等业务逻辑。
 * 支持一个用户拥有多个收货地址，其中一个可设为默认地址。
 */
public interface AddressService {

    /**
     * 根据ID查询地址信息
     *
     * @param id 地址主键ID
     * @return 地址实体对象，不存在则返回null
     */
    Address findById(Long id);

    /**
     * 查询指定用户的所有收货地址列表
     *
     * @param user_id 用户ID
     * @return 该用户的地址列表，无数据则返回空列表
     */
    List<Address> findByUserId(Long user_id);

    /**
     * 查询指定用户的默认收货地址
     *
     * @param user_id 用户ID
     * @return 默认地址对象，未设置默认地址则返回null
     */
    Address findDefaultByUserId(Long user_id);

    /**
     * 添加新的收货地址
     *
     * <p>如果设置为默认地址，会自动取消该用户原有的默认地址。
     *
     * @param address 地址实体对象（需包含userId、receiverName、receiverPhone、receiverAddress等）
     * @return 添加成功的地址对象，失败则返回null
     */
    Address addAddress(Address address);

    /**
     * 更新收货地址信息
     *
     * <p>如果设置为默认地址，会自动取消该用户原有的默认地址。
     *
     * @param address 地址实体对象（必须包含id）
     * @return 更新成功返回true，失败返回false
     */
    boolean updateAddress(Address address);

    /**
     * 根据ID删除收货地址
     *
     * @param id 地址主键ID
     * @return 删除成功返回true，失败返回false
     */
    boolean deleteById(Long id);

    /**
     * 设置指定地址为用户的默认地址
     *
     * <p>会先取消该用户原有的默认地址，再设置新地址为默认。
     *
     * @param user_id 用户ID
     * @param address_id 要设为默认的地址ID
     * @return 设置成功返回true，失败返回false
     */
    boolean setDefaultAddress(Long user_id, Long address_id);
}
