DROP TABLE IF EXISTS `house`;
CREATE TABLE `house`
(
    `id`                 int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'house唯一标识',
    `title`              varchar(32)      NOT NULL,
    `price`              int(11) unsigned NOT NULL COMMENT '价格',
    `area`               int(11) unsigned NOT NULL COMMENT '面积',
    `room`               int(11) unsigned NOT NULL COMMENT '卧室数量',
    `floor`              int(11) unsigned NOT NULL COMMENT '楼层',
    `total_floor`        int(11) unsigned NOT NULL COMMENT '总楼层',
    `watch_times`        int(11) unsigned          DEFAULT '0' COMMENT '被看次数',
    `build_year`         int(4)           NOT NULL COMMENT '建立年限',
    `status`             int(4) unsigned  NOT NULL DEFAULT '0' COMMENT '房屋状态 0-未审核 1-审核通过 2-已出租 3-逻辑删除',
    `create_time`        datetime         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `last_update_time`   datetime         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最近数据更新时间',
    `city_en_name`       varchar(32)      NOT NULL COMMENT '城市标记缩写 如 北京bj',
    `region_en_name`     varchar(255)     NOT NULL COMMENT '地区英文简写 如昌平区 cpq',
    `cover`              varchar(32)               DEFAULT NULL COMMENT '封面',
    `direction`          int(11)          NOT NULL COMMENT '房屋朝向',
    `distance_to_subway` int(11)          NOT NULL DEFAULT '-1' COMMENT '距地铁距离 默认-1 附近无地铁',
    `parlour`            int(11)          NOT NULL DEFAULT '0' COMMENT '客厅数量',
    `district`           varchar(32)      NOT NULL COMMENT '所在小区',
    `admin_id`           int(11)          NOT NULL COMMENT '所属管理员id',
    `bathroom`           int(11)          NOT NULL DEFAULT '0',
    `street`             varchar(32)      NOT NULL COMMENT '街道',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 26
  DEFAULT CHARSET = utf8mb4 COMMENT ='房屋信息表';


DROP TABLE IF EXISTS `house_detail`;
CREATE TABLE `house_detail`
(
    `id`                  int(11) unsigned NOT NULL AUTO_INCREMENT,
    `description`         varchar(255) DEFAULT NULL COMMENT '详细描述',
    `layout_desc`         varchar(255) DEFAULT NULL COMMENT '户型介绍',
    `traffic`             varchar(255) DEFAULT NULL COMMENT '交通出行',
    `round_service`       varchar(255) DEFAULT NULL COMMENT '周边配套',
    `rent_way`            int(2)           NOT NULL COMMENT '租赁方式',
    `address`             varchar(32)      NOT NULL COMMENT '详细地址 ',
    `subway_line_id`      int(11)      DEFAULT NULL COMMENT '附近地铁线id',
    `subway_line_name`    varchar(32)  DEFAULT NULL COMMENT '附近地铁线名称',
    `subway_station_id`   int(11)      DEFAULT NULL COMMENT '地铁站id',
    `subway_station_name` varchar(32)  DEFAULT NULL COMMENT '地铁站名',
    `house_id`            int(11)          NOT NULL COMMENT '对应house的id',
    PRIMARY KEY (`id`),
    UNIQUE KEY `index_on_house_id` (`house_id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 33
  DEFAULT CHARSET = utf8mb4;


DROP TABLE IF EXISTS `house_picture`;
CREATE TABLE `house_picture`
(
    `id`         int(10) unsigned NOT NULL AUTO_INCREMENT,
    `house_id`   int(11)          NOT NULL COMMENT '所属房屋id',
    `cdn_prefix` varchar(255)     NOT NULL COMMENT '图片路径',
    `width`      int(11)     DEFAULT NULL COMMENT '宽',
    `height`     int(11)     DEFAULT NULL COMMENT '高',
    `location`   varchar(32) DEFAULT NULL COMMENT '所属房屋位置',
    `path`       varchar(255)     NOT NULL COMMENT '文件名',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 86
  DEFAULT CHARSET = utf8mb4 COMMENT ='房屋图片信息';


DROP TABLE IF EXISTS `house_subscribe`;
CREATE TABLE `house_subscribe`
(
    `id`               int(11) unsigned NOT NULL AUTO_INCREMENT,
    `house_id`         int(11)          NOT NULL COMMENT '房源id',
    `user_id`          int(11)          NOT NULL COMMENT '用户id',
    `desc`             varchar(255)              DEFAULT NULL COMMENT '用户描述',
    `status`           int(2)           NOT NULL DEFAULT '0' COMMENT '预约状态 1-加入待看清单 2-已预约看房时间 3-看房完成',
    `create_time`      datetime         NOT NULL COMMENT '数据创建时间',
    `last_update_time` datetime         NOT NULL COMMENT '记录更新时间',
    `order_time`       datetime                  DEFAULT NULL COMMENT '预约时间',
    `telephone`        varchar(11)               DEFAULT NULL COMMENT '联系电话',
    `admin_id`         int(11)          NOT NULL COMMENT '房源发布者id',
    PRIMARY KEY (`id`),
    UNIQUE KEY `index_on_user_and_house` (`house_id`, `user_id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 10
  DEFAULT CHARSET = utf8mb4 COMMENT ='预约看房信息表';


DROP TABLE IF EXISTS `house_tag`;
CREATE TABLE `house_tag`
(
    `house_id` int(11)          NOT NULL COMMENT '房屋id',
    `id`       int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '标签id',
    `name`     varchar(32)      NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `index_on_house_id_and_name` (`house_id`, `name`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 22
  DEFAULT CHARSET = utf8mb4 COMMENT ='房屋标签映射关系表';


DROP TABLE IF EXISTS `role`;
CREATE TABLE `role`
(
    `id`      int(11) unsigned NOT NULL AUTO_INCREMENT,
    `user_id` int(11) unsigned NOT NULL COMMENT '用户id',
    `name`    varchar(32)      NOT NULL COMMENT '用户角色名',
    PRIMARY KEY (`id`),
    UNIQUE KEY `user_id_and_name` (`user_id`, `name`) USING BTREE,
    KEY `user_id` (`user_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 9
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户角色表';


DROP TABLE IF EXISTS `subway`;
CREATE TABLE `subway`
(
    `id`           int(11) unsigned NOT NULL AUTO_INCREMENT,
    `name`         varchar(32)      NOT NULL COMMENT '线路名',
    `city_en_name` varchar(32)      NOT NULL COMMENT '所属城市英文名缩写',
    PRIMARY KEY (`id`),
    KEY `index_on_city` (`city_en_name`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 8
  DEFAULT CHARSET = utf8mb4;


DROP TABLE IF EXISTS `subway_station`;
CREATE TABLE `subway_station`
(
    `id`        int(11) unsigned NOT NULL AUTO_INCREMENT,
    `subway_id` int(11)          NOT NULL COMMENT '所属地铁线id',
    `name`      varchar(32)      NOT NULL COMMENT '站点名称',
    PRIMARY KEY (`id`),
    UNIQUE KEY `unique_station` (`subway_id`, `name`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 65
  DEFAULT CHARSET = utf8mb4;


DROP TABLE IF EXISTS `support_address`;
CREATE TABLE `support_address`
(
    `id`            int(11) unsigned NOT NULL AUTO_INCREMENT,
    `belong_to`     varchar(32)      NOT NULL DEFAULT '0' COMMENT '上一级行政单位名',
    `en_name`       varchar(32)      NOT NULL COMMENT '行政单位英文名缩写',
    `cn_name`       varchar(32)      NOT NULL COMMENT '行政单位中文名',
    `level`         varchar(16)      NOT NULL COMMENT '行政级别 市-city 地区-region',
    `baidu_map_lng` double           NOT NULL COMMENT '百度地图经度',
    `baidu_map_lat` double           NOT NULL COMMENT '百度地图纬度',
    PRIMARY KEY (`id`),
    UNIQUE KEY `unique_en_name_and_belong_to` (`en_name`, `level`, `belong_to`) USING BTREE COMMENT '每个城市的英文名都是独一无二的'
) ENGINE = InnoDB
  AUTO_INCREMENT = 20
  DEFAULT CHARSET = utf8mb4;



DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`
(
    `id`               int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '用户唯一id',
    `name`             varchar(32)               DEFAULT NULL COMMENT '用户名',
    `email`            varchar(32)               DEFAULT NULL COMMENT '电子邮箱',
    `phone_number`     varchar(15)      NOT NULL COMMENT '电话号码',
    `password`         varchar(64)               DEFAULT NULL COMMENT '密码',
    `status`           int(2) unsigned  NOT NULL DEFAULT '0' COMMENT '用户状态 0-正常 1-封禁',
    `create_time`      datetime         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '用户账号创建时间',
    `last_login_time`  datetime         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上次登录时间',
    `last_update_time` datetime         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '上次更新记录时间',
    `avatar`           varchar(255)              DEFAULT NULL COMMENT '头像',
    PRIMARY KEY (`id`),
    UNIQUE KEY `index_on_phone` (`phone_number`) USING BTREE COMMENT '用户手机号',
    UNIQUE KEY `index_on_username` (`name`) USING BTREE COMMENT '用户名索引',
    UNIQUE KEY `index_on_email` (`email`) USING BTREE COMMENT '电子邮箱索引'
) DEFAULT CHARSET = utf8mb4 COMMENT ='用户基本信息表';


INSERT INTO `subway`
VALUES ('1', '13号线', 'bj'),
       ('2', '1号线', 'bj'),
       ('3', '2号线', 'bj'),
       ('4', '10号线', 'bj'),
       ('5', '8号线', 'bj'),
       ('6', '9号线', 'bj'),
       ('7', '7号线', 'bj');

INSERT INTO `subway_station`
VALUES ('5', '1', '上地'),
       ('16', '1', '东直门'),
       ('4', '1', '五道口'),
       ('14', '1', '光熙门'),
       ('11', '1', '北苑'),
       ('8', '1', '回龙观'),
       ('2', '1', '大钟寺'),
       ('12', '1', '望京西'),
       ('15', '1', '柳芳'),
       ('3', '1', '知春路'),
       ('10', '1', '立水桥'),
       ('13', '1', '芍药居'),
       ('6', '1', '西二旗'),
       ('1', '1', '西直门'),
       ('9', '1', '霍营'),
       ('7', '1', '龙泽'),
       ('33', '4', '三元家庄'),
       ('51', '4', '三元桥'),
       ('41', '4', '丰台站'),
       ('52', '4', '亮马桥'),
       ('27', '4', '健德门'),
       ('46', '4', '公主坟'),
       ('44', '4', '六里桥'),
       ('53', '4', '农业展览馆'),
       ('62', '4', '分钟寺'),
       ('59', '4', '劲松'),
       ('28', '4', '北土城'),
       ('61', '4', '十里河'),
       ('58', '4', '双井'),
       ('55', '4', '呼家楼'),
       ('54', '4', '团结湖'),
       ('57', '4', '国贸'),
       ('35', '4', '大红门'),
       ('32', '4', '太阳宫'),
       ('29', '4', '安贞门'),
       ('64', '4', '宋家庄'),
       ('20', '4', '巴沟'),
       ('30', '4', '惠新西街南口'),
       ('48', '4', '慈寿寺'),
       ('63', '4', '成寿寺'),
       ('42', '4', '泥洼'),
       ('22', '4', '海淀黄庄'),
       ('60', '4', '潘家园'),
       ('19', '4', '火器营'),
       ('26', '4', '牡丹园'),
       ('24', '4', '知春路'),
       ('23', '4', '知春里'),
       ('34', '4', '石榴庄'),
       ('39', '4', '纪家庙'),
       ('31', '4', '芍药居'),
       ('21', '4', '苏州街'),
       ('38', '4', '草桥'),
       ('45', '4', '莲花桥'),
       ('25', '4', '西土城'),
       ('43', '4', '西局'),
       ('47', '4', '西钓鱼台'),
       ('36', '4', '角门东'),
       ('37', '4', '角门西'),
       ('17', '4', '车道沟'),
       ('56', '4', '金台夕照'),
       ('18', '4', '长春桥'),
       ('40', '4', '首经贸');

INSERT INTO `support_address`
VALUES ('4', 'bj', 'bj', '北京', 'city', '116.395645', '39.929986'),
       ('5', 'sh', 'sh', '上海', 'city', '121.487899', '31.249162'),
       ('6', 'hb', 'sjz', '石家庄', 'city', '114.522082', '38.048958'),
       ('7', 'hb', 'ts', '唐山', 'city', '118.183451', '39.650531'),
       ('8', 'hb', 'hd', '邯郸', 'city', '114.482694', '36.609308'),
       ('9', 'bj', 'dcq', '东城区', 'region', '116.42188470126446', '39.93857401298612'),
       ('10', 'bj', 'xcq', '西城区', 'region', '116.37319010401802', '39.93428014370851'),
       ('12', 'bj', 'hdq', '海淀区', 'region', '116.23967780102151', '40.03316204507791'),
       ('13', 'bj', 'cpq', '昌平区', 'region', '116.21645635689414', '40.2217235498323'),
       ('14', 'sh', 'ptq', '普陀区', 'region', '121.39844294374956', '31.263742929075534'),
       ('15', 'sjz', 'caq', '长安区', 'region', '114.59262155387033', '38.07687479578663'),
       ('16', 'sjz', 'qdq', '桥东区', 'region', '114.51078430496142', '38.06338975380927'),
       ('17', 'sjz', 'qxq', '桥西区', 'region', '114.43813995531943', '38.033364550068136'),
       ('18', 'sjz', 'xhq', '新华区', 'region', '114.4535014286928', '38.117218640478164'),
       ('19', 'bj', 'cyq', '朝阳区', 'region', '116.52169489108084', '39.95895316640668');