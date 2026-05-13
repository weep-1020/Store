package com.weep.service.impl;

import com.weep.entity.Products;
import com.weep.mapper.ProductsMapper;
import com.weep.response.PageResult;
import com.weep.service.ProductsService;
import com.weep.util.IdGenerator;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 商品服务实现类
 * 
 * <p>负责商品的增删改查、库存管理、状态管理等业务逻辑的具体实现。
 * 采用 Spring 事务管理确保数据一致性，使用原子 SQL 操作防止并发超卖。
 * 
 * <p>核心功能：
 * <ul>
 *   <li>商品基本信息管理（增删改查）</li>
 *   <li>库存管理（更新、扣减）</li>
 *   <li>商品状态控制（上架/下架）</li>
 *   <li>并发安全的库存扣减（原子操作）</li>
 * </ul>
 */
@Service
public class ProductsServiceImpl implements ProductsService {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(ProductsServiceImpl.class);

    /**
     * 商品数据访问层
     */
    @Resource
    private ProductsMapper productsMapper;

    /**
     * 根据商品ID查询商品信息
     * 
     * @param spid 商品唯一标识ID
     * @return 商品实体对象，查询失败返回 null
     */
    @Override
    public Products findBySpid(String spid) {
        try {
            return productsMapper.findBySpid(spid);
        } catch (Exception e) {
            logger.error("查询商品失败 - ID: {}, 错误: {}", spid, e.getMessage());
            return null;
        }
    }

    /**
     * 根据商品名称查询商品信息
     * 
     * @param p_name 商品名称
     * @return 商品实体对象，查询失败返回 null
     */
    @Override
    public Products findByPName(String p_name) {
        try {
            return productsMapper.findByPName(p_name);
        } catch (Exception e) {
            logger.error("查询商品失败 - 名称: {}, 错误: {}", p_name, e.getMessage());
            return null;
        }
    }

    /**
     * 查询所有商品列表
     * 
     * @return 商品列表，查询失败返回空列表
     */
    @Override
    public List<Products> findAll() {
        try {
            return productsMapper.findAll();
        } catch (Exception e) {
            logger.error("查询所有商品失败 - 错误: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 根据分类ID查询商品列表
     * 
     * @param category_id 分类ID
     * @return 该分类下的商品列表，查询失败返回空列表
     */
    @Override
    public List<Products> findByCategoryId(String category_id) {
        try {
            return productsMapper.findByCategoryId(category_id);
        } catch (Exception e) {
            logger.error("查询分类商品失败 - 分类ID: {}, 错误: {}", category_id, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 根据状态查询商品列表
     * 
     * @param status 商品状态（0: 下架, 1: 上架）
     * @return 对应状态的商品列表，查询失败返回空列表
     */
    @Override
    public List<Products> findByStatus(String status) {
        try {
            return productsMapper.findByStatus(status);
        } catch (Exception e) {
            logger.error("查询商品失败 - 状态: {}, 错误: {}", status, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 添加新商品
     * 
     * <p>校验规则：
     * <ul>
     *   <li>商品名称不能为空且不能重复</li>
     *   <li>价格不能为负数</li>
     *   <li>库存不能为负数</li>
     * </ul>
     * 
     * @param product 商品实体对象
     * @return 添加成功返回 true，失败返回 false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addProduct(Products product) {
        try {
            if (product.getP_name() == null || product.getP_name().isEmpty()) {
                logger.warn("添加商品失败：商品名称为空");
                return false;
            }

            if (productsMapper.findByPName(product.getP_name()) != null) {
                logger.warn("添加商品失败：商品名称已存在 - {}", product.getP_name());
                return false;
            }

            if (product.getPrice() != null && product.getPrice() < 0) {
                logger.warn("添加商品失败：商品价格不能为负数 - {}", product.getPrice());
                return false;
            }

            if (product.getStock() != null && product.getStock() < 0) {
                logger.warn("添加商品失败：商品库存不能为负数 - {}", product.getStock());
                return false;
            }

            if (product.getSpid() == null || product.getSpid().isEmpty()) {
                product.setSpid(IdGenerator.generateUUID());
            }

            Date now = new Date();
            if (product.getCreated_at() == null) {
                product.setCreated_at(now);
            }
            if (product.getUpdated_at() == null) {
                product.setUpdated_at(now);
            }
            if (product.getStatus() == null) {
                product.setStatus("1");
            }

            int result = productsMapper.insert(product);
            if (result > 0) {
                logger.info("商品添加成功 - 商品ID: {}, 名称: {}", product.getSpid(), product.getP_name());
                return true;
            } else {
                logger.warn("商品添加失败 - 商品名称: {}", product.getP_name());
                return false;
            }
        } catch (Exception e) {
            logger.error("商品添加异常 - 商品名称: {}, 错误: {}", product.getP_name(), e.getMessage());
            return false;
        }
    }

    /**
     * 更新商品信息
     * 
     * <p>校验规则：
     * <ul>
     *   <li>商品ID不能为空</li>
     *   <li>商品必须存在</li>
     *   <li>价格不能为负数</li>
     *   <li>库存不能为负数</li>
     *   <li>状态只能是 0 或 1</li>
     * </ul>
     * 
     * @param product 商品实体对象（需包含 spid）
     * @return 更新成功返回 true，失败返回 false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateProduct(Products product) {
        try {
            if (product.getSpid() == null || product.getSpid().isEmpty()) {
                logger.warn("更新商品失败：商品ID为空");
                return false;
            }

            Products existingProduct = productsMapper.findBySpid(product.getSpid());
            if (existingProduct == null) {
                logger.warn("更新商品失败：商品不存在 - ID: {}", product.getSpid());
                return false;
            }

            if (product.getPrice() != null && product.getPrice() < 0) {
                logger.warn("更新商品失败：商品价格不能为负数 - {}", product.getPrice());
                return false;
            }

            if (product.getStock() != null && product.getStock() < 0) {
                logger.warn("更新商品失败：商品库存不能为负数 - {}", product.getStock());
                return false;
            }

            if (product.getStatus() != null && !product.getStatus().equals("0") && !product.getStatus().equals("1")) {
                logger.warn("更新商品失败：商品状态无效 - {}", product.getStatus());
                return false;
            }

            product.setUpdated_at(new Date());

            int result = productsMapper.update(product);
            if (result > 0) {
                logger.info("商品信息更新成功 - ID: {}", product.getSpid());
                return true;
            } else {
                logger.warn("商品信息更新失败 - ID: {}", product.getSpid());
                return false;
            }
        } catch (Exception e) {
            logger.error("商品信息更新异常 - ID: {}, 错误: {}", product.getSpid(), e.getMessage());
            return false;
        }
    }

    /**
     * 根据商品ID删除商品
     * 
     * @param spid 商品唯一标识ID
     * @return 删除成功返回 true，失败返回 false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteBySpid(String spid) {
        try {
            if (spid == null || spid.isEmpty()) {
                logger.warn("删除商品失败：商品ID为空");
                return false;
            }

            Products existingProduct = productsMapper.findBySpid(spid);
            if (existingProduct == null) {
                logger.warn("删除商品失败：商品不存在 - ID: {}", spid);
                return false;
            }

            int result = productsMapper.deleteBySpid(spid);
            if (result > 0) {
                logger.info("商品删除成功 - ID: {}", spid);
                return true;
            } else {
                logger.warn("商品删除失败 - ID: {}", spid);
                return false;
            }
        } catch (Exception e) {
            logger.error("商品删除异常 - ID: {}, 错误: {}", spid, e.getMessage());
            return false;
        }
    }

    /**
     * 更新商品状态
     * 
     * @param spid 商品唯一标识ID
     * @param status 商品状态（0: 下架, 1: 上架）
     * @return 更新成功返回 true，失败返回 false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(String spid, String status) {
        try {
            if (spid == null || spid.isEmpty() || status == null) {
                logger.warn("更新商品状态失败：参数无效 - ID: {}, Status: {}", spid, status);
                return false;
            }

            if (!status.equals("0") && !status.equals("1")) {
                logger.warn("更新商品状态失败：状态值无效，只能是 0 或 1 - Status: {}", status);
                return false;
            }

            Products existingProduct = productsMapper.findBySpid(spid);
            if (existingProduct == null) {
                logger.warn("更新商品状态失败：商品不存在 - ID: {}", spid);
                return false;
            }

            int result = productsMapper.updateStatus(spid, status);
            if (result > 0) {
                logger.info("商品状态更新成功 - ID: {}, 新状态: {}", spid, status);
                return true;
            } else {
                logger.warn("商品状态更新失败 - ID: {}", spid);
                return false;
            }
        } catch (Exception e) {
            logger.error("商品状态更新异常 - ID: {}, 错误: {}", spid, e.getMessage());
            return false;
        }
    }

    /**
     * 更新商品库存（直接设置库存值）
     * 
     * @param spid 商品唯一标识ID
     * @param stock 新的库存数量（必须 >= 0）
     * @return 更新成功返回 true，失败返回 false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStock(String spid, int stock) {
        try {
            if (spid == null || spid.isEmpty()) {
                logger.warn("更新商品库存失败：商品ID为空");
                return false;
            }

            if (stock < 0) {
                logger.warn("更新商品库存失败：库存不能为负数 - ID: {}, Stock: {}", spid, stock);
                return false;
            }

            Products existingProduct = productsMapper.findBySpid(spid);
            if (existingProduct == null) {
                logger.warn("更新商品库存失败：商品不存在 - ID: {}", spid);
                return false;
            }

            int result = productsMapper.updateStock(spid, stock);
            if (result > 0) {
                logger.info("商品库存更新成功 - ID: {}, 新库存: {}", spid, stock);
                return true;
            } else {
                logger.warn("商品库存更新失败 - ID: {}", spid);
                return false;
            }
        } catch (Exception e) {
            logger.error("商品库存更新异常 - ID: {}, 错误: {}", spid, e.getMessage());
            return false;
        }
    }

    /**
     * 扣减商品库存（并发安全）
     * 
     * <p>使用原子更新操作，直接在 SQL 层完成库存检查和扣减，
     * 避免并发场景下的超卖问题。
     * 
     * @param spid 商品唯一标识ID
     * @param quantity 扣减数量（必须 > 0）
     * @return 扣减成功返回 true，失败返回 false（库存不足或商品不存在）
     */
    private static final Set<String> ALLOWED_SORT_COLUMNS = Set.of("created_at", "price", "stock", "p_name", "updated_at");
    private static final Set<String> ALLOWED_SORT_ORDERS = Set.of("ASC", "DESC");

    @Override
    public PageResult<Products> search(String keyword, String category_id, String status,
                                        int page, int size, String sortBy, String sortOrder) {
        try {
            if (sortBy != null && !sortBy.isEmpty() && !ALLOWED_SORT_COLUMNS.contains(sortBy)) {
                logger.warn("非法排序字段: {}, 回退为默认排序", sortBy);
                sortBy = "created_at";
            }
            if (sortOrder != null && !sortOrder.isEmpty() && !ALLOWED_SORT_ORDERS.contains(sortOrder.toUpperCase())) {
                sortOrder = "DESC";
            } else if (sortOrder != null) {
                sortOrder = sortOrder.toUpperCase();
            }
            if (sortBy == null || sortBy.isEmpty()) sortBy = "created_at";
            if (sortOrder == null || sortOrder.isEmpty()) sortOrder = "DESC";

            int offset = (page - 1) * size;
            List<Products> records = productsMapper.search(keyword, category_id, status, sortBy, sortOrder, offset, size);
            long total = productsMapper.countSearch(keyword, category_id, status);
            return new PageResult<>(records, total, page, size);
        } catch (Exception e) {
            logger.error("商品搜索失败 - 关键词: {}, 错误: {}", keyword, e.getMessage());
            return new PageResult<>(new ArrayList<>(), 0, page, size);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean decreaseStock(String spid, int quantity) {
        try {
            if (spid == null || spid.isEmpty() || quantity <= 0) {
                logger.warn("扣减商品库存失败：参数无效 - ID: {}, Quantity: {}", spid, quantity);
                return false;
            }

            // 使用原子更新，直接在 SQL 层完成检查和扣减，避免并发问题
            int affectedRows = productsMapper.decreaseStockAtomic(spid, quantity);
            if (affectedRows > 0) {
                logger.info("商品库存扣减成功 - ID: {}, 扣减数量: {}", spid, quantity);
                return true;
            } else {
                logger.warn("商品库存扣减失败：库存不足或商品不存在 - ID: {}, 扣减数量: {}", spid, quantity);
                return false;
            }
        } catch (Exception e) {
            logger.error("商品库存扣减异常 - ID: {}, 错误: {}", spid, e.getMessage());
            return false;
        }
    }
}
