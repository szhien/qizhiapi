-- 创建库
create database if not exists qizhiapi;
-- 切换库
use qizhiapi;

-- 接口信息表
create table if not exists qizhiapi.`interface_info`
(
    `id` bigint not null auto_increment comment '主键' primary key,
    `name` varchar(256) not null comment '接口名',
    `url` varchar(512) not null comment '接口地址',
    `method` varchar(256) not null comment '接口类型',
    `description` varchar(256) null comment '接口描述',
    `status` int default 0 not null comment '状态[0 关闭 1开启]',
    `requestParams` text null comment '请求参数',
    `requestHeader` text null comment '请求头',
    `responseHeader` text null comment '响应头',
    `userId` bigint not null comment '创建人',
    `createTime` datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    `updateTime` datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    `isDeleted` tinyint default 0 not null comment '是否删除(0-未删, 1-已删)'
) comment '接口信息表';

insert into qizhiapi.`interface_info` (`name`, `url`, `method`, `status`, `requestHeader`, `responseHeader`, `userId`) values ('8zSv', 'www.lorenzo-rodriguez.com', '宋旭尧', 0, '7AW', 'RCv', 7);
insert into qizhiapi.`interface_info` (`name`, `url`, `method`, `status`, `requestHeader`, `responseHeader`, `userId`) values ('ImRg', 'www.beau-mante.org', '孔烨磊', 0, 'Ya', 'VR7', 3);
insert into qizhiapi.`interface_info` (`name`, `url`, `method`, `status`, `requestHeader`, `responseHeader`, `userId`) values ('aME5t', 'www.gene-witting.info', '金立辉', 0, 'Mk', 'Wud', 451067042);
insert into qizhiapi.`interface_info` (`name`, `url`, `method`, `status`, `requestHeader`, `responseHeader`, `userId`) values ('Jv', 'www.douglass-hermiston.info', '熊琪', 0, '5ehU3', 'QQ6j', 3);
insert into qizhiapi.`interface_info` (`name`, `url`, `method`, `status`, `requestHeader`, `responseHeader`, `userId`) values ('MDOu', 'www.jefferson-quitzon.info', '郭智宸', 0, 'oTVX', 'aYM9', 6);
insert into qizhiapi.`interface_info` (`name`, `url`, `method`, `status`, `requestHeader`, `responseHeader`, `userId`) values ('fCLRD', 'www.addie-hoppe.co', '魏锦程', 0, '9fVOM', '4UsGZ', 3837);
insert into qizhiapi.`interface_info` (`name`, `url`, `method`, `status`, `requestHeader`, `responseHeader`, `userId`) values ('bZ', 'www.branden-brakus.info', '戴凯瑞', 0, 'S0mP', 'ceXVh', 47);
insert into qizhiapi.`interface_info` (`name`, `url`, `method`, `status`, `requestHeader`, `responseHeader`, `userId`) values ('dh2E', 'www.ahmad-wintheiser.co', '高伟诚', 0, 'yz', '8s0x', 76);
insert into qizhiapi.`interface_info` (`name`, `url`, `method`, `status`, `requestHeader`, `responseHeader`, `userId`) values ('ONY', 'www.xavier-ankunding.net', '马文轩', 0, 'DXl', 'K5PL', 7150703011);
insert into qizhiapi.`interface_info` (`name`, `url`, `method`, `status`, `requestHeader`, `responseHeader`, `userId`) values ('Rt', 'www.adelia-wisozk.info', '邱子涵', 0, '3ip', 'PK', 2414);
insert into qizhiapi.`interface_info` (`name`, `url`, `method`, `status`, `requestHeader`, `responseHeader`, `userId`) values ('zq3Vs', 'www.katlyn-ferry.co', '郝文轩', 0, 'lGKn', '1bb', 828610);
insert into qizhiapi.`interface_info` (`name`, `url`, `method`, `status`, `requestHeader`, `responseHeader`, `userId`) values ('snger', 'www.toby-muller.org', '马明', 0, 'IXH', 'T2', 6360021759);
insert into qizhiapi.`interface_info` (`name`, `url`, `method`, `status`, `requestHeader`, `responseHeader`, `userId`) values ('uBh', 'www.roland-osinski.biz', '何智宸', 0, 'OeXQE', '6ZdZ', 220848209);
insert into qizhiapi.`interface_info` (`name`, `url`, `method`, `status`, `requestHeader`, `responseHeader`, `userId`) values ('izrRo', 'www.ozzie-bogan.com', '龙哲瀚', 0, 'eHM', 'skG', 3327);
insert into qizhiapi.`interface_info` (`name`, `url`, `method`, `status`, `requestHeader`, `responseHeader`, `userId`) values ('yAotK', 'www.cordell-considine.net', '龙航', 0, 'AYT', '0z', 21068);
insert into qizhiapi.`interface_info` (`name`, `url`, `method`, `status`, `requestHeader`, `responseHeader`, `userId`) values ('vQ', 'www.shanta-dibbert.org', '孙鑫磊', 0, 'YPx', 'PkaTR', 6);
insert into qizhiapi.`interface_info` (`name`, `url`, `method`, `status`, `requestHeader`, `responseHeader`, `userId`) values ('Wo', 'www.chad-bauch.biz', '段鸿煊', 0, 'XGIK', 'oh1zU', 5);
insert into qizhiapi.`interface_info` (`name`, `url`, `method`, `status`, `requestHeader`, `responseHeader`, `userId`) values ('PF', 'www.jamal-kshlerin.org', '覃天宇', 0, 'Qy2SC', '7u3G', 483631);
insert into qizhiapi.`interface_info` (`name`, `url`, `method`, `status`, `requestHeader`, `responseHeader`, `userId`) values ('Ql7Ko', 'www.ronald-nolan.org', '武耀杰', 0, 'FjG', 'NOzzi', 91449959);
insert into qizhiapi.`interface_info` (`name`, `url`, `method`, `status`, `requestHeader`, `responseHeader`, `userId`) values ('DDSx', 'www.refugia-koch.com', '韩炎彬', 0, 'AMJuP', 'Xc', 3922911181);


-- 用户调用接口关系表
create table if not exists qizhiapi.`user_interface_info`
(
    `id` bigint not null auto_increment comment '主键' primary key,
    `userId` bigint not null comment '调用用户id',
    `interfaceInfoId` bigint not null comment '接口id',
    `totalNum` int default 0 not null comment '总调用次数',
    `leftNum` int default 0 not null comment '剩余调用次数',
    `status` int default 0 not null comment '状态[0-正常 1-禁用]',
    `createTime` datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    `updateTime` datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    `isDeleted` tinyint default 0 not null comment '是否删除(0-未删, 1-已删)'
)comment '用户调用关系表';