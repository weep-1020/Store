/*
 * mall_db 初始数据脚本 (DML)
 * 包含所有表的基础初始数据
 * 执行顺序：先执行 mall_db.sql 建表，再执行本脚本插入数据
 * 密码均为明文 "123456" 的 BCrypt 编码
 */

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. 用户表 (users) — 3 个预置用户，密码均为 123456
-- ============================================================
INSERT INTO `users` (`id`, `username`, `password`, `email`, `phone`, `pic`, `status`, `role_id`, `created_at`, `updated_at`, `last_login_at`) VALUES
(1, 'admin',    '$2a$10$WDrM4Lzp/VufBPaWnl6HdeDqaIzGjOUjzmVSDtf1bpbVY3kXwLrEe', 'admin@mall.com',    '13800000001', 'https://api.dicebear.com/9.x/initials/svg?seed=Admin&backgroundColor=409EFF',      1, 1, '2026-04-22 10:29:42', NULL, NULL),
(2, 'merchant', '$2a$10$SzDRNJGn5er/tB/2obnBOO9WBSmPgHTnRNStthHNODb5s/UgjzNaW', 'shop@mall.com',     '13800000002', 'https://api.dicebear.com/9.x/initials/svg?seed=Shop&backgroundColor=E6A23C',      1, 2, '2026-04-22 10:29:42', NULL, NULL),
(3, 'user',     '$2a$10$wauBCB253BA5T53bJfsL4eNMjdrhOegRwg8m0iNtEMHFtL3UyNzZO', 'user@mall.com',     '13800000003', 'https://api.dicebear.com/9.x/initials/svg?seed=User&backgroundColor=67C23A',      1, 3, '2026-04-22 10:29:42', NULL, NULL),
(4, 'testuser', '$2a$10$WDrM4Lzp/VufBPaWnl6HdeDqaIzGjOUjzmVSDtf1bpbVY3kXwLrEe', 'test@mall.com',     '13800000004', 'https://api.dicebear.com/9.x/initials/svg?seed=Test&backgroundColor=909399',      1, 3, NOW(),                 NOW(),     NULL),
(5, 'disabled', '$2a$10$WDrM4Lzp/VufBPaWnl6HdeDqaIzGjOUjzmVSDtf1bpbVY3kXwLrEe', 'disabled@mall.com', '13800000005', 'https://api.dicebear.com/9.x/initials/svg?seed=Off&backgroundColor=F56C6C',       0, 3, NOW(),                 NOW(),     NULL);

-- ============================================================
-- 2. 角色表 (roles)
-- ============================================================
INSERT INTO `roles` (`id`, `role_name`, `role_code`, `description`) VALUES
(1, '超级管理员', 'SUPER_ADMIN', '所有权限，可管理用户、商品、订单'),
(2, '商家',       'MERCHANT',    '商品和订单管理'),
(3, '普通用户',   'USER',        '浏览和购买商品');

-- ============================================================
-- 3. 权限表 (permissions)
-- ============================================================
INSERT INTO `permissions` (`id`, `permission_name`, `permission_code`, `resource_type`) VALUES
(1, '查看商品',   'product:view',   'api'),
(2, '管理商品',   'product:manage', 'api'),
(3, '查看订单',   'order:view',     'api'),
(4, '管理订单',   'order:manage',   'api'),
(5, '用户管理',   'user:manage',    'api');

-- ============================================================
-- 4. 用户-角色关联 (user_roles)
-- ============================================================
INSERT INTO `user_roles` (`user_id`, `role_id`) VALUES
(1, 1),  -- admin → SUPER_ADMIN
(2, 2),  -- merchant → MERCHANT
(3, 3),  -- user → USER
(4, 3),  -- testuser → USER
(5, 3);  -- disabled → USER

-- ============================================================
-- 5. 角色-权限关联 (role_permissions)
-- ============================================================
INSERT INTO `role_permissions` (`role_id`, `permission_id`) VALUES
-- SUPER_ADMIN: 全部 5 项权限
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5),
-- MERCHANT: 查看/管理商品、查看/管理订单
(2, 1), (2, 2), (2, 3), (2, 4),
-- USER: 查看商品、查看订单
(3, 1), (3, 3);

-- ============================================================
-- 6. 商品分类 (categories) — 2级树形结构, 7大品类 24个子类
-- ============================================================
INSERT INTO `categories` (`id`, `category_name`, `parent_id`, `description`, `sort_order`) VALUES
-- 电子产品
(1,  '电子产品',   0, '智能手机、电脑、平板等数码产品',    1),
(4,  '手机',       1, '智能手机及配件',                   1),
(5,  '电脑',       1, '笔记本和台式机',                   2),
(22, '平板',       1, '平板电脑及电子阅读器',             3),
(23, '智能穿戴',   1, '手表、手环等穿戴设备',             4),
(24, '耳机音箱',   1, '蓝牙耳机、音响设备',               5),
-- 服装鞋包
(2,  '服装鞋包',   0, '男女服装、鞋靴、箱包配饰',         2),
(6,  '男装',       2, '男士衬衫、外套、裤装等',            1),
(7,  '女装',       2, '女士连衣裙、上衣、半身裙等',        2),
(25, '运动鞋服',   2, '运动T恤、跑鞋、瑜伽服',            3),
(26, '箱包配饰',   2, '双肩包、手提包、钱包、皮带',       4),
-- 食品生鲜
(3,  '食品生鲜',   0, '休闲零食、生鲜水果、茶饮冲调',     3),
(27, '休闲零食',   3, '坚果、膨化食品、糖果巧克力',        1),
(28, '生鲜水果',   3, '新鲜水果、蔬菜、肉禽蛋奶',          2),
(29, '茶饮冲调',   3, '茶叶、咖啡、冲饮谷物',             3),
-- 家居生活
(30, '家居生活',   0, '厨具餐具、家纺布艺、收纳用品',     4),
(31, '厨具餐具',   30, '锅具、刀具、餐具套装',            1),
(32, '家纺布艺',   30, '床上用品、毛巾、窗帘',            2),
(33, '清洁收纳',   30, '清洁工具、收纳箱、置物架',         3),
-- 图书文具
(34, '图书文具',   0, '图书、杂志、文具办公用品',          5),
(35, '图书',       34, '小说、教材、专业技术书籍',         1),
(36, '文具办公',   34, '钢笔、笔记本、办公设备',           2),
-- 运动户外
(37, '运动户外',   0, '健身器材、户外装备、运动配件',      6),
(38, '健身器材',   37, '瑜伽垫、哑铃、跑步机',             1),
(39, '户外装备',   37, '帐篷、登山包、户外工具',           2),
-- 美妆个护
(40, '美妆个护',   0, '面部护肤、身体洗护、彩妆香水',      7),
(41, '面部护肤',   40, '面膜、精华、面霜、防晒',            1),
(42, '身体洗护',   40, '洗发水、沐浴露、身体乳',           2);

-- ============================================================
-- 7. 商品 (products) — 26个商品覆盖所有分类
-- ============================================================
INSERT INTO `products` (`spid`, `p_name`, `pic`, `price`, `description`, `stock`, `category_id`, `status`, `created_at`, `updated_at`) VALUES
-- 电子产品 > 手机
('P001', 'iPhone 15 Pro',       'https://picsum.photos/seed/p001/400/400', 8999.00,  '苹果最新款智能手机，A17 Pro芯片，钛金属边框', 100, 4,  1, '2026-05-13 15:00:48', '2026-05-13 15:00:48'),
-- 电子产品 > 电脑
('P002', 'MacBook Pro 14',      'https://picsum.photos/seed/p002/400/400', 14999.00, 'M3 Pro芯片，18GB内存，Liquid Retina XDR屏',    50,  5,  1, '2026-05-13 15:00:48', '2026-05-13 15:00:48'),
-- 电子产品 > 平板
('P011', 'iPad Air M2',         'https://picsum.photos/seed/p011/400/400', 4799.00,  '11英寸全面屏，M2芯片，支持Apple Pencil',       80,  22, 1, NOW(),              NOW()),
-- 电子产品 > 智能穿戴
('P012', '小米手环9 Pro',       'https://picsum.photos/seed/p012/400/400', 399.00,   '1.74英寸AMOLED屏，14天续航，150+运动模式',     200, 23, 1, NOW(),              NOW()),
('P010', '华为GT4智能手表',     'https://picsum.photos/seed/p010/400/400', 1299.00,  '心率/血氧/GPS，14天续航，高尔夫模式',           60,  23, 0, NOW(),              NOW()),
-- 电子产品 > 耳机音箱
('P013', 'Sony WH-1000XM5',     'https://picsum.photos/seed/p013/400/400', 2299.00,  '行业标杆降噪，30小时续航，佩戴舒适',           120, 24, 1, NOW(),              NOW()),
('P006', '无线蓝牙耳机',        'https://picsum.photos/seed/p006/400/400', 199.00,   '主动降噪，蓝牙5.3，续航30小时',                500, 24, 1, NOW(),              NOW()),
-- 电子产品通用
('P005', '智能家居音箱',        'https://picsum.photos/seed/p005/400/400', 399.00,   '语音控制，360°环绕音效，智能家居中控',         300, 1,  1, '2026-05-13 15:00:48', '2026-05-13 15:00:48'),
('P007', '机械键盘',            'https://picsum.photos/seed/p007/400/400', 459.00,   'Cherry MX轴，RGB背光，热插拔',                 80,  1,  1, NOW(),              NOW()),
-- 服装鞋包 > 男装
('P003', '男士休闲衬衫',        'https://picsum.photos/seed/p003/400/400', 299.00,   '纯棉牛津纺，修身版型，多色可选',               200, 6,  1, '2026-05-13 15:00:48', '2026-05-13 15:00:48'),
-- 服装鞋包 > 女装
('P004', '女士连衣裙',          'https://picsum.photos/seed/p004/400/400', 599.00,   '法式复古风，V领收腰，适合通勤约会',            150, 7,  1, '2026-05-13 15:00:48', '2026-05-13 15:00:48'),
-- 服装鞋包 > 运动鞋服
('P014', '速干运动T恤',         'https://picsum.photos/seed/p014/400/400', 159.00,   '吸湿排汗，四面弹力，适合跑步健身',             300, 25, 1, NOW(),              NOW()),
('P008', '女士跑鞋',            'https://picsum.photos/seed/p008/400/400', 389.00,   '飞织网面，缓震回弹，轻量透气',                 120, 25, 1, NOW(),              NOW()),
-- 服装鞋包 > 箱包配饰
('P015', '真皮手提商务包',      'https://picsum.photos/seed/p015/400/400', 899.00,   '头层牛皮，大容量，14英寸电脑仓',               80,  26, 1, NOW(),              NOW()),
('P009', '双肩背包',            'https://picsum.photos/seed/p009/400/400', 259.00,   '防泼水面料，USB充电口，大容量旅行背包',        200, 26, 1, NOW(),              NOW()),
-- 食品生鲜
('P016', '坚果礼盒',            'https://picsum.photos/seed/p016/400/400', 168.00,   '每日坚果6种混合，30袋独立包装，年货送礼',       500, 27, 1, NOW(),              NOW()),
('P017', '新鲜红富士苹果5斤',   'https://picsum.photos/seed/p017/400/400', 49.90,    '产地直发，脆甜多汁，坏果包赔',                 1000,28, 1, NOW(),              NOW()),
('P018', '明前龙井绿茶',        'https://picsum.photos/seed/p018/400/400', 258.00,   '2026新茶，西湖产区，豆香浓郁',                 200, 29, 1, NOW(),              NOW()),
-- 家居生活
('P019', '麦饭石不粘炒锅',      'https://picsum.photos/seed/p019/400/400', 199.00,   '32cm大口径，少油烟，适用电磁炉燃气灶',          300, 31, 1, NOW(),              NOW()),
('P020', '全棉四件套',          'https://picsum.photos/seed/p020/400/400', 329.00,   '100%新疆长绒棉，简约纯色，1.5m/1.8m床通用',    250, 32, 1, NOW(),              NOW()),
-- 图书文具
('P021', 'Java编程思想(第5版)', 'https://picsum.photos/seed/p021/400/400', 128.00,   'Java入门经典，从基础到进阶，附赠源码',          400, 35, 1, NOW(),              NOW()),
('P022', '凌美钢笔套装',        'https://picsum.photos/seed/p022/400/400', 189.00,   '德国工艺，F尖适合日常书写，含墨水+笔套',        150, 36, 1, NOW(),              NOW()),
-- 运动户外
('P023', '加厚防滑瑜伽垫',      'https://picsum.photos/seed/p023/400/400', 89.00,    '10mm加厚，双面防滑，含收纳绑带',                500, 38, 1, NOW(),              NOW()),
('P024', '专业登山双肩包',      'https://picsum.photos/seed/p024/400/400', 349.00,   '50L大容量，防水透气背负系统，含防雨罩',         100, 39, 1, NOW(),              NOW()),
-- 美妆个护
('P025', '玻尿酸补水面膜20片',  'https://picsum.photos/seed/p025/400/400', 99.00,    '三重玻尿酸，深层补水，敏感肌适用',             800, 41, 1, NOW(),              NOW()),
('P026', '氨基酸洁面慕斯',      'https://picsum.photos/seed/p026/400/400', 79.00,    '温和不紧绷，泡沫绵密，含神经酰胺',              600, 42, 1, NOW(),              NOW());

-- ============================================================
-- 8. 收货地址 (addresses)
-- ============================================================
INSERT INTO `addresses` (`user_id`, `receiver_name`, `receiver_phone`, `province`, `city`, `district`, `detail_address`, `is_default`, `created_at`) VALUES
(1, '张管理', '13800000001', '北京市',  '北京市', '朝阳区', '建国路100号',               1, NOW()),
(1, '张管理', '13900000001', '上海市',  '上海市', '浦东新区', '陆家嘴金融城1号',         0, NOW()),
(2, '李商家', '13800000002', '广东省',  '广州市', '天河区', '天河路200号',               1, NOW()),
(3, '王用户', '13800000003', '浙江省',  '杭州市', '西湖区', '文三路300号',               1, NOW()),
(3, '王用户', '13600000003', '江苏省',  '南京市', '鼓楼区', '中山北路50号',              0, NOW());

-- ============================================================
-- 9. 购物车 (cart_item)
-- ============================================================
INSERT INTO `cart_item` (`user_id`, `sku_id`, `product_name`, `product_image`, `product_price`, `product_specs`, `quantity`, `created_at`) VALUES
(3, 'P001', 'iPhone 15 Pro',   'https://picsum.photos/seed/p001/200/200', 8999.00,  NULL, 1, NOW()),
(3, 'P003', '男士休闲衬衫',    'https://picsum.photos/seed/p003/200/200', 299.00,   '{"color":"白色","size":"L"}',  2, NOW()),
(3, 'P005', '智能家居音箱',    'https://picsum.photos/seed/p005/200/200', 399.00,   NULL, 1, NOW()),
(4, 'P006', '无线蓝牙耳机',    'https://picsum.photos/seed/p006/200/200', 199.00,   '{"color":"黑色"}', 1, NOW());

-- ============================================================
-- 10. 订单 (orders) — 覆盖各种状态
-- ============================================================
INSERT INTO `orders` (`order_no`, `user_id`, `total_amount`, `pay_amount`, `status`, `payment_method`, `payment_time`, `delivery_time`, `receiver_name`, `receiver_phone`, `receiver_address`, `remark`, `created_at`) VALUES
('ORD20260501001', 3, 8999.00,  8999.00,  2, 'wechat', '2026-05-01 10:30:00', '2026-05-02 09:00:00', '王用户', '13800000003', '浙江省杭州市西湖区文三路300号', '请发顺丰',          '2026-05-01 10:00:00'),
('ORD20260515001', 3, 598.00,   598.00,   1, 'alipay', '2026-05-15 14:00:00', NULL,                  '王用户', '13800000003', '浙江省杭州市西湖区文三路300号', NULL,                '2026-05-15 13:30:00'),
('ORD20260520001', 4, 199.00,   0.00,     0, NULL,      NULL,                   NULL,                  '赵测试', '13800000004', '广东省深圳市南山区科技园路1号',   NULL,                '2026-05-20 16:00:00'),
('ORD20260522001', 3, 299.00,   0.00,     4, NULL,      NULL,                   NULL,                  '王用户', '13800000003', '浙江省杭州市西湖区文三路300号', '不想要了',          '2026-05-22 08:00:00');

-- ============================================================
-- 11. 订单明细 (order_items)
-- ============================================================
INSERT INTO `order_items` (`order_no`, `sku_id`, `product_name`, `product_image`, `product_price`, `product_specs`, `quantity`, `subtotal`, `created_at`) VALUES
('ORD20260501001', 'P001', 'iPhone 15 Pro',   'https://picsum.photos/seed/p001/200/200', 8999.00, NULL,                             1, 8999.00, '2026-05-01 10:00:00'),
('ORD20260515001', 'P003', '男士休闲衬衫',    'https://picsum.photos/seed/p003/200/200', 299.00,  '{"color":"白色","size":"L"}',    2, 598.00,  '2026-05-15 13:30:00'),
('ORD20260520001', 'P006', '无线蓝牙耳机',    'https://picsum.photos/seed/p006/200/200', 199.00,  '{"color":"黑色"}',               1, 199.00,  '2026-05-20 16:00:00'),
('ORD20260522001', 'P003', '男士休闲衬衫',    'https://picsum.photos/seed/p003/200/200', 299.00,  '{"color":"蓝色","size":"XL"}',   1, 299.00,  '2026-05-22 08:00:00');

-- ============================================================
-- 12. 商品评价 (reviews)
-- ============================================================
INSERT INTO `reviews` (`order_item_id`, `user_id`, `product_id`, `rating`, `content`, `images`, `additional_content`, `additional_time`, `created_at`) VALUES
(1, 3, 'P001', 5, '非常好用，物流也很快！',                NULL, '用了半个月，电池续航确实强', '2026-05-16 10:00:00', '2026-05-03 12:00:00'),
(2, 3, 'P003', 4, '材质不错，穿起来很舒服，尺码刚好。',  NULL, NULL,                        NULL,                  '2026-05-18 15:00:00');

-- ============================================================
-- 13. 图片表 (large_files) — 默认头像占位（实际文件在 FileStorageService 初始化时生成）
-- ============================================================
-- 图片文件现在存储于本地磁盘，large_files 表只保留元数据
-- INSERT INTO `large_files` (`pic`, `filename`, `file_path`, `file_size`, `content_type`) VALUES
-- ('a39a949e-a686-4742-80d3-ed2f44c46260', 'default-avatar.png', '', 67, 'image/png');

-- ============================================================
-- 14. 轮播图 (carousels)
-- ============================================================
INSERT INTO `carousels` (`image_url`, `title`, `link_url`, `sort_order`, `status`) VALUES
('https://picsum.photos/seed/c1/800/400', '春日焕新季', '/categories?category_id=2', 1, 1),
('https://picsum.photos/seed/c2/800/400', '数码好物推荐', '/categories?category_id=3', 2, 1),
('https://picsum.photos/seed/c3/800/400', '限时特惠', '/categories', 3, 1),
('https://picsum.photos/seed/c4/800/400', '新品首发', '/categories', 4, 1);

SET FOREIGN_KEY_CHECKS = 1;
