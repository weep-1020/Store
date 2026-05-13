package com.weep.service.impl;

import com.weep.entity.Address;
import com.weep.mapper.AddressMapper;
import com.weep.service.AddressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 收货地址服务实现类
 *
 * <p>实现收货地址的完整管理功能，包括增删改查和默认地址设置。
 * 使用 @Transactional 注解保证事务一致性。
 */
@Service
public class AddressServiceImpl implements AddressService {

    private static final Logger logger = LoggerFactory.getLogger(AddressServiceImpl.class);

    @Autowired
    private AddressMapper addressMapper;

    /**
     * 根据ID查询地址信息
     *
     * @param id 地址主键ID
     * @return 地址实体对象，不存在则返回null
     */
    @Override
    public Address findById(Long id) {
        return addressMapper.findById(id);
    }

    /**
     * 查询指定用户的所有收货地址列表
     *
     * @param user_id 用户ID
     * @return 该用户的地址列表，无数据则返回空列表
     */
    @Override
    public List<Address> findByUserId(Long user_id) {
        return addressMapper.findByUserId(user_id);
    }

    /**
     * 查询指定用户的默认收货地址
     *
     * @param user_id 用户ID
     * @return 默认地址对象，未设置默认地址则返回null
     */
    @Override
    public Address findDefaultByUserId(Long user_id) {
        return addressMapper.findDefaultByUserId(user_id);
    }

    /**
     * 添加新的收货地址
     *
     * <p>如果设置为默认地址，会自动取消该用户原有的默认地址。
     *
     * @param address 地址实体对象（需包含userId、receiverName、receiverPhone、receiverAddress等）
     * @return 添加成功的地址对象，失败则返回null
     */
    @Override
    @Transactional
    public Address addAddress(Address address) {
        // 如果是默认地址，先取消其他默认地址
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            addressMapper.cancelDefaultByUserId(address.getUserId());
        } else {
            address.setIsDefault(0);
        }

        address.setCreatedAt(LocalDateTime.now());
        address.setUpdatedAt(LocalDateTime.now());

        int result = addressMapper.insert(address);
        if (result > 0) {
            logger.info("地址添加成功 - ID: {}, 用户: {}", address.getId(), address.getUserId());
            return address;
        } else {
            logger.error("地址添加失败 - 用户: {}", address.getUserId());
            return null;
        }
    }

    /**
     * 更新收货地址信息
     *
     * <p>如果设置为默认地址，会自动取消该用户原有的默认地址。
     *
     * @param address 地址实体对象（必须包含id）
     * @return 更新成功返回true，失败返回false
     */
    @Override
    @Transactional
    public boolean updateAddress(Address address) {
        // 如果设置为默认地址，先取消其他默认地址
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            addressMapper.cancelDefaultByUserId(address.getUserId());
        }

        address.setUpdatedAt(LocalDateTime.now());
        int result = addressMapper.update(address);
        
        if (result > 0) {
            logger.info("地址更新成功 - ID: {}", address.getId());
            return true;
        } else {
            logger.error("地址更新失败 - ID: {}", address.getId());
            return false;
        }
    }

    /**
     * 根据ID删除收货地址
     *
     * @param id 地址主键ID
     * @return 删除成功返回true，失败返回false
     */
    @Override
    @Transactional
    public boolean deleteById(Long id) {
        int result = addressMapper.deleteById(id);
        if (result > 0) {
            logger.info("地址删除成功 - ID: {}", id);
            return true;
        } else {
            logger.error("地址删除失败 - ID: {}", id);
            return false;
        }
    }

    /**
     * 设置指定地址为用户的默认地址
     *
     * <p>会先取消该用户原有的默认地址，再设置新地址为默认。
     *
     * @param user_id 用户ID
     * @param address_id 要设为默认的地址ID
     * @return 设置成功返回true，失败返回false
     */
    @Override
    @Transactional
    public boolean setDefaultAddress(Long user_id, Long address_id) {
        // 验证地址是否属于该用户
        Address address = addressMapper.findById(address_id);
        if (address == null || !address.getUserId().equals(user_id)) {
            logger.warn("设置默认地址失败 - 地址不存在或不属于该用户");
            return false;
        }

        // 取消所有默认地址
        addressMapper.cancelDefaultByUserId(user_id);
        
        // 设置新的默认地址
        int result = addressMapper.setDefaultAddress(address_id);
        
        if (result > 0) {
            logger.info("设置默认地址成功 - 用户: {}, 地址: {}", user_id, address_id);
            return true;
        } else {
            logger.error("设置默认地址失败 - 用户: {}, 地址: {}", user_id, address_id);
            return false;
        }
    }
}