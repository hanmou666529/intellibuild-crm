/*
SQLyog Ultimate v8.32 
MySQL - 5.5.30 : Database - crm
*********************************************************************
*/


/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`crm` /*!40100 DEFAULT CHARACTER SET utf8 */;

USE `crm`;

/*Table structure for table `account` */

DROP TABLE IF EXISTS `account`;

CREATE TABLE `account` (
  `id` BIGINT(20) PRIMARY KEY NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` VARCHAR(20) DEFAULT NULL COMMENT '姓名',
  `pwd` VARCHAR(40) DEFAULT NULL COMMENT '密码',
  `img_url` VARCHAR(200) DEFAULT 'default.png' COMMENT '头像地址',
  `create_time` DATETIME DEFAULT NULL COMMENT '注册时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `role` INT(11) DEFAULT NULL COMMENT '角色：1超级管理员，0普通管理员',
  `status` INT(11) DEFAULT NULL COMMENT '状态: 1启用,0禁用'
) ENGINE=INNODB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

/*Data for the table `account` */

INSERT  INTO `account`(`id`,`username`,`pwd`,`img_url`,`create_time`,`update_time`,`role`,`status`) VALUES (1,'admin','c2f365c379ea6da2c2e42675fae561ac','default.png','2022-06-17 11:02:29','2022-06-21 23:08:43',1,1),(2,'lisi','c2f365c379ea6da2c2e42675fae561ac','default.png','2021-06-17 11:02:50','2022-06-22 07:35:23',0,1),(3,'zhangsan','c2f365c379ea6da2c2e42675fae561ac','default.png','2022-06-18 16:27:14',NULL,0,0),(4,'wangwu','c2f365c379ea6da2c2e42675fae561ac','default.png','2022-06-20 14:31:05',NULL,0,1);

/*Table structure for table `dept` */

DROP TABLE IF EXISTS `dept`;

CREATE TABLE `dept` (
  `id` BIGINT(20) PRIMARY KEY NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` VARCHAR(20) DEFAULT NULL COMMENT '部门名称',
  `loc` VARCHAR(200) DEFAULT NULL COMMENT '部门地址'
) ENGINE=INNODB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

/*Data for the table `dept` */

INSERT  INTO `dept`(`id`,`name`,`loc`) VALUES (1,'Java开发部','武汉市江夏区久阳科技园5楼'),(2,'Java研发部','长沙市芙蓉区鲁巷广场4楼'),(3,'UI部','石家庄市石头区石头广场3楼'),(7,'JS开发部','石家庄市石头区石头广场3楼'),(8,'人力资源','广东省佛山市耀阳区万达广场'),(9,'这里是小卖部','小卖部小卖部小卖部小卖部');

/*Table structure for table `tbl_dic_value` */

DROP TABLE IF EXISTS `tbl_dic_value`;

CREATE TABLE `tbl_dic_value` (
  `id` CHAR(32) PRIMARY KEY NOT NULL COMMENT '主键，采用UUID',
  `value` VARCHAR(255) DEFAULT NULL COMMENT '不能为空，并且要求同一个字典类型下字典值不能重复，具有唯一性。',
  `text` VARCHAR(255) DEFAULT NULL COMMENT '可以为空',
  `order_no` VARCHAR(255) DEFAULT NULL COMMENT '可以为空，但不为空的时候，要求必须是正整数',
  `type_code` VARCHAR(255) DEFAULT NULL COMMENT '外键'
) ENGINE=INNODB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;

/*Data for the table `tbl_dic_value` */

INSERT  INTO `tbl_dic_value`(`id`,`value`,`text`,`order_no`,`type_code`) VALUES ('06e3cbdf10a44eca8511dddfc6896c55','虚假线索','虚假线索','4','clueState'),('0fe33840c6d84bf78df55d49b169a894','销售邮件','销售邮件','8','source'),('12302fd42bd349c1bb768b19600e6b20','交易会','交易会','11','source'),('1615f0bb3e604552a86cde9a2ad45bea','最高','最高','2','returnPriority'),('176039d2a90e4b1a81c5ab8707268636','教授','教授','5','appellation'),('1e0bd307e6ee425599327447f8387285','将来联系','将来联系','2','clueState'),('2173663b40b949ce928db92607b5fe57','丢失线索','丢失线索','5','clueState'),('2876690b7e744333b7f1867102f91153','未启动','未启动','1','returnState'),('29805c804dd94974b568cfc9017b2e4c','07成交','07成交','7','stage'),('310e6a49bd8a4962b3f95a1d92eb76f4','试图联系','试图联系','1','clueState'),('31539e7ed8c848fc913e1c2c93d76fd1','博士','博士','4','appellation'),('37ef211719134b009e10b7108194cf46','01资质审查','01资质审查','1','stage'),('391807b5324d4f16bd58c882750ee632','08丢失的线索','08丢失的线索','8','stage'),('3a39605d67da48f2a3ef52e19d243953','聊天','聊天','14','source'),('474ab93e2e114816abf3ffc596b19131','低','低','3','returnPriority'),('48512bfed26145d4a38d3616e2d2cf79','广告','广告','1','source'),('4d03a42898684135809d380597ed3268','合作伙伴研讨会','合作伙伴研讨会','9','source'),('59795c49896947e1ab61b7312bd0597c','先生','先生','1','appellation'),('5c6e9e10ca414bd499c07b886f86202a','高','高','1','returnPriority'),('67165c27076e4c8599f42de57850e39c','夫人','夫人','2','appellation'),('68a1b1e814d5497a999b8f1298ace62b','09因竞争丢失关闭','09因竞争丢失关闭','9','stage'),('6b86f215e69f4dbd8a2daa22efccf0cf','web调研','web调研','13','source'),('72f13af8f5d34134b5b3f42c5d477510','合作伙伴','合作伙伴','6','source'),('7c07db3146794c60bf975749952176df','未联系','未联系','6','clueState'),('86c56aca9eef49058145ec20d5466c17','内部研讨会','内部研讨会','10','source'),('9095bda1f9c34f098d5b92fb870eba17','进行中','进行中','3','returnState'),('954b410341e7433faa468d3c4f7cf0d2','已有业务','已有业务','1','transactionType'),('966170ead6fa481284b7d21f90364984','已联系','已联系','3','clueState'),('96b03f65dec748caa3f0b6284b19ef2f','推迟','推迟','2','returnState'),('97d1128f70294f0aac49e996ced28c8a','新业务','新业务','2','transactionType'),('9ca96290352c40688de6596596565c12','完成','完成','4','returnState'),('9e6d6e15232549af853e22e703f3e015','需要条件','需要条件','7','clueState'),('9ff57750fac04f15b10ce1bbb5bb8bab','02需求分析','02需求分析','2','stage'),('a70dc4b4523040c696f4421462be8b2f','等待某人','等待某人','5','returnState'),('a83e75ced129421dbf11fab1f05cf8b4','推销电话','推销电话','2','source'),('ab8472aab5de4ae9b388b2f1409441c1','常规','常规','5','returnPriority'),('ab8c2a3dc05f4e3dbc7a0405f721b040','05提案/报价','05提案/报价','5','stage'),('b924d911426f4bc5ae3876038bc7e0ad','web下载','web下载','12','source'),('c13ad8f9e2f74d5aa84697bb243be3bb','03价值建议','03价值建议','3','stage'),('c83c0be184bc40708fd7b361b6f36345','最低','最低','4','returnPriority'),('db867ea866bc44678ac20c8a4a8bfefb','员工介绍','员工介绍','3','source'),('e44be1d99158476e8e44778ed36f4355','04确定决策者','04确定决策者','4','stage'),('e5f383d2622b4fc0959f4fe131dafc80','女士','女士','3','appellation'),('e81577d9458f4e4192a44650a3a3692b','06谈判/复审','06谈判/复审','6','stage'),('fb65d7fdb9c6483db02713e6bc05dd19','在线商场','在线商场','5','source'),('fd677cc3b5d047d994e16f6ece4d3d45','公开媒介','公开媒介','7','source'),('ff802a03ccea4ded8731427055681d48','外部介绍','外部介绍','4','source');

/*Table structure for table `tbl_tran` */

DROP TABLE IF EXISTS `tbl_tran`;

CREATE TABLE `tbl_tran` (
  `id` CHAR(32) PRIMARY KEY NOT NULL,
  `owner` CHAR(32) DEFAULT NULL,
  `money` VARCHAR(255) DEFAULT NULL,
  `name` VARCHAR(255) DEFAULT NULL,
  `expected_date` CHAR(10) DEFAULT NULL,
  `customer_id` CHAR(32) DEFAULT NULL,
  `stage` VARCHAR(255) DEFAULT NULL,
  `type` VARCHAR(255) DEFAULT NULL,
  `source` VARCHAR(255) DEFAULT NULL,
  `activity_id` CHAR(32) DEFAULT NULL,
  `contacts_id` CHAR(32) DEFAULT NULL,
  `create_by` VARCHAR(255) DEFAULT NULL,
  `create_time` CHAR(19) DEFAULT NULL,
  `edit_by` VARCHAR(255) DEFAULT NULL,
  `edit_time` CHAR(19) DEFAULT NULL,
  `description` VARCHAR(255) DEFAULT NULL,
  `contact_summary` VARCHAR(255) DEFAULT NULL,
  `next_contact_time` CHAR(10) DEFAULT NULL
) ENGINE=INNODB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;

/*Data for the table `tbl_tran` */

INSERT  INTO `tbl_tran`(`id`,`owner`,`money`,`name`,`expected_date`,`customer_id`,`stage`,`type`,`source`,`activity_id`,`contacts_id`,`create_by`,`create_time`,`edit_by`,`edit_time`,`description`,`contact_summary`,`next_contact_time`) VALUES ('001fe571286f4eeaaf31c9ab59f224fc','40f6cdea0bd34aceb77492a1656d9fb3','6500','交易重试','2009-09-08','d76ec257f738409e9e458ea3a4cb4ccb','68a1b1e814d5497a999b8f1298ace62b','954b410341e7433faa468d3c4f7cf0d2','48512bfed26145d4a38d3616e2d2cf79','4b601e03b120407eac94cd11668513fe','e119bda7a567471d87888ef28f7083a0','40f6cdea0bd34aceb77492a1656d9fb3','2021-05-26 16:21:35',NULL,NULL,'烦烦烦方法','烦烦烦','2011-09-08'),('0085ec609b61420fac94860ccf6309bf','40f6cdea0bd34aceb77492a1656d9fb3','5600','第一次购买','2021-09-08','00f8d48cc6384facabfb5caf900869a8','29805c804dd94974b568cfc9017b2e4c','954b410341e7433faa468d3c4f7cf0d2','86c56aca9eef49058145ec20d5466c17','31ae55adbb324364af50f33aaa906bec','2939e47610454fb98d7410af16ec5005','40f6cdea0bd34aceb77492a1656d9fb3','2021-05-26 16:09:11',NULL,NULL,'很好','和那好','2021-9-9'),('00cc28d187d64ed89248c07c110b8a03','06f5fc056eac41558a964f96daa7f27c','7855','测试','2021-11-06','5f871dee7daa42cbaedc0ecdac34eaae','391807b5324d4f16bd58c882750ee632','97d1128f70294f0aac49e996ced28c8a','12302fd42bd349c1bb768b19600e6b20','31ae55adbb324364af50f33aaa906bec','2939e47610454fb98d7410af16ec5005','40f6cdea0bd34aceb77492a1656d9fb3','2021-05-26 16:09:59',NULL,NULL,NULL,NULL,NULL),('2a6abe0c9a7f4b9c88fbf5aace442574','06f5fc056eac41558a964f96daa7f27c','3000','新名称','2020-09-27','407c5630feb44e8eba49dc7848314b22','391807b5324d4f16bd58c882750ee632','954b410341e7433faa468d3c4f7cf0d2','12302fd42bd349c1bb768b19600e6b20','31ae55adbb324364af50f33aaa906bec','2939e47610454fb98d7410af16ec5005','98fecbb7b87111eb947254e1ad709136','2020-09-27 22:39:00',NULL,NULL,'123','123','2020-09-27'),('53c47de10dec4f0a8cccad852c14da43','06f5fc056eac41558a964f96daa7f27c','111','111','2020-10-05','ec43928e18bb417a8ed5febe63c80ed5','68a1b1e814d5497a999b8f1298ace62b','97d1128f70294f0aac49e996ced28c8a','12302fd42bd349c1bb768b19600e6b20','31ae55adbb324364af50f33aaa906bec','2939e47610454fb98d7410af16ec5005','98fecbb7b87111eb947254e1ad709136','2020-10-05 21:39:02',NULL,NULL,'111','111','2020-10-05'),('74ae280a9f0047afac0d8f920ddee5b8','40f6cdea0bd34aceb77492a1656d9fb3','100000000','有钱任性','2020-10-10','407c5630feb44e8eba49dc7848314b21','37ef211719134b009e10b7108194cf46','954b410341e7433faa468d3c4f7cf0d2','12302fd42bd349c1bb768b19600e6b20','31ae55adbb324364af50f33aaa906bec','2939e47610454fb98d7410af16ec5005','98fecbb7b87111eb947254e1ad709136','2020-09-24 17:26:56',NULL,NULL,'线索描述：有钱','联系纪要：任性','2020-10-10'),('80e929b5bb064b69bfe8042f82321d88','40f6cdea0bd34aceb77492a1656d9fb3','6666','lllll','2020-09-25','d0e3f67471cb4ae2a12fd7d60fdef34c','391807b5324d4f16bd58c882750ee632','97d1128f70294f0aac49e996ced28c8a','86c56aca9eef49058145ec20d5466c17','4b601e03b120407eac94cd11668513fe','00f8d48cc6384facabfb5caf900869a8','98fecbb7b87111eb947254e1ad709136','2020-09-25 22:46:51',NULL,NULL,'666','666','2020-10-10'),('9974c88e7a6b4314978467037259baba','06f5fc056eac41558a964f96daa7f27c','8000','交易02','2020-10-03','d61780c4c3cc451f8236259b248fbd46','68a1b1e814d5497a999b8f1298ace62b','97d1128f70294f0aac49e996ced28c8a','86c56aca9eef49058145ec20d5466c17','31ae55adbb324364af50f33aaa906bec','e119bda7a567471d87888ef28f7083a0','98fecbb7b87111eb947254e1ad709136','2020-09-24 16:57:29',NULL,NULL,'111','联系纪要中','2020-09-30'),('9c3a982e74b345339ef73f57b6878096','06f5fc056eac41558a964f96daa7f27c','100000','交易123','2020-09-27','ec43928e18bb417a8ed5febe63c80ed5','68a1b1e814d5497a999b8f1298ace62b','954b410341e7433faa468d3c4f7cf0d2','86c56aca9eef49058145ec20d5466c17','31ae55adbb324364af50f33aaa906bec','2939e47610454fb98d7410af16ec5005','98fecbb7b87111eb947254e1ad709136','2020-09-27 22:41:41',NULL,NULL,'12','123','2020-09-27'),('9fd3f3fb0f804e7fb8876102e302b450','06f5fc056eac41558a964f96daa7f27c','4589','AAAA','2021-05-01','4773e1ae93d64625acde4bafafe732db','68a1b1e814d5497a999b8f1298ace62b','954b410341e7433faa468d3c4f7cf0d2','86c56aca9eef49058145ec20d5466c17','31d63a3514564d51b6758f974c9fafcf','34046b48f12346e9ba43768914c99640','40f6cdea0bd34aceb77492a1656d9fb3','2021-05-09 14:55:37',NULL,NULL,'水水水水水水水水水水','水水水水水水水水水水水','2021-05-15'),('b98aaa782adb4f7aa9287c618bd9d328','06f5fc056eac41558a964f96daa7f27c','123','123啊啊啊啊','2020-10-05','0efcfaceb7d94611a94daa2027b2322b','29805c804dd94974b568cfc9017b2e4c','954b410341e7433faa468d3c4f7cf0d2','86c56aca9eef49058145ec20d5466c17','15a871e8b4e84b85a1069874fff33c02','2939e47610454fb98d7410af16ec5005','98fecbb7b87111eb947254e1ad709136','2020-10-05 22:13:37','李四','2020-10-11 15:58:47','33621','665561','2020-11-02'),('e6d809225a78443784ba8629d55d0599','40f6cdea0bd34aceb77492a1656d9fb3','5600','第一次购买','2021-09-08','00f8d48cc6384facabfb5caf900869a8','29805c804dd94974b568cfc9017b2e4c','954b410341e7433faa468d3c4f7cf0d2','86c56aca9eef49058145ec20d5466c17','31ae55adbb324364af50f33aaa906bec','2939e47610454fb98d7410af16ec5005','40f6cdea0bd34aceb77492a1656d9fb3','2021-05-26 16:09:40',NULL,NULL,'很好','和那好','2021-9-9'),('f40aeedc07b449f9ae4d46c9675b68bf','40f6cdea0bd34aceb77492a1656d9fb3','5600','BBB','2021-12-13','00f8d48cc6384facabfb5caf900869a8','e44be1d99158476e8e44778ed36f4355','954b410341e7433faa468d3c4f7cf0d2','48512bfed26145d4a38d3616e2d2cf79','15a871e8b4e84b85a1069874fff33c02','2939e47610454fb98d7410af16ec5005','40f6cdea0bd34aceb77492a1656d9fb3','2021-05-26 16:14:37',NULL,NULL,'sssss','ssss','2001-09-09');

/*Table structure for table `user` */

DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
  `id` BIGINT(20) PRIMARY KEY NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` VARCHAR(20) DEFAULT NULL COMMENT '客户姓名',
  `birthday` DATETIME DEFAULT NULL COMMENT '生日',
  `sex` VARCHAR(2) DEFAULT NULL COMMENT '1男、0女',
  `tel` VARCHAR(20) DEFAULT NULL COMMENT '手机',
  `sal` FLOAT(10,2) DEFAULT NULL COMMENT '薪资',
  `profession` VARCHAR(2) DEFAULT NULL COMMENT '1攻城狮、2程序猿、3，码龙',
  `address` VARCHAR(200) DEFAULT NULL COMMENT '客户住址',
  `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',
  `dept_id` BIGINT(20) DEFAULT NULL COMMENT '部门编号'
) ENGINE=INNODB AUTO_INCREMENT=50 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

/*Data for the table `user` */

INSERT  INTO `user`(`id`,`username`,`birthday`,`sex`,`tel`,`sal`,`profession`,`address`,`remark`,`dept_id`) VALUES (1,'大骚跨','2020-03-08 00:00:00','1','18812342278',2000.00,'3','湖北省武汉市江夏区光谷大道','哈哈',2),(2,'凤姐的姐我拼命的写','2020-03-15 00:00:00','0','13887654321',30004.00,'1','湖北省武汉市江夏区光谷大道汤逊湖街道','呜呼呼',2),(5,'傻了','2020-03-12 00:00:00','0','13812345699',3000.00,'2','湖北省武汉市江夏区光谷大道','哈哈',2),(7,'阿达','2020-03-12 00:00:00','1','13812345699',300023.00,'2','湖北省武汉市江夏区光谷大道','哈哈',2),(8,'大根儿','2020-03-12 00:00:00','0','15812345699',3000.00,'2','湖北省武汉市江夏区光谷大道','哈哈',2),(9,'雪亿雕','2020-03-10 00:00:00','0','13812345690',5000.00,'2','湖北省武汉市江夏区光谷','哈哈',3),(10,'叶仁界','2020-03-12 00:00:00','1','13812312312',30000.00,'2','湖北省武汉市江夏区光谷大道','哈哈',2),(11,'胡图图','2010-03-12 00:00:00','1','13812345749',30000.00,'3','翻斗大街翻斗花园翻斗小区5号','哈哈',2),(12,'小丁丁的丁丁','2020-03-12 00:00:00','0','13812345690',999999.00,'2','湖北省武汉市江夏区光谷大道','我是小丁丁的丁丁',2),(13,'老丁丁','2020-03-17 00:00:00','1','13865748984',3000.00,'2','湖北省武汉市江夏区光谷大道久阳科技园','真的够细',3),(14,'雪亿','2020-03-11 00:00:00','0','13812345690',3000.00,'2','湖北省武汉市江夏区光谷','哈哈',2),(15,'吴顺意','2020-03-12 00:00:00','0','13812345766',3000.00,'2','湖北省武汉市江夏区光谷大道','哈哈',2),(16,'旺仔','2010-05-12 00:00:00','1','13812345128',2000.00,'3','旺旺碎冰冰旺旺小小酥','哈哈',2),(17,'海绵宝宝','2020-03-12 00:00:00','1','13935647890',3000.00,'2','湖北省武汉市江夏区光谷大道','蟹黄堡真好吃',2),(18,'雪亿','2020-03-11 00:00:00','0','13812345695',3000.00,'2','湖北省武汉市江夏区光谷','哈哈',2),(20,'凤姐的爷','2020-03-12 00:00:00','0','13838383838',3000.00,'2','湖北省武汉市江夏区光谷大道','哈哈',2),(21,'子龙','2020-03-11 00:00:00','0','13812345695',3000.00,'2','湖北省武汉市江夏区光谷','哈哈',2),(22,'吴顺意','2020-03-12 00:00:00','0','13817345766',3000.00,'2','湖北省武汉市江夏区光谷大道','哈哈',2),(23,'超人不想飞','2020-03-12 00:00:00','1','15812345888',3000.00,'2','湖北省武汉市江夏区光谷大道','交接',1),(24,'小小鸟','2020-03-12 00:00:00','0','15915915915',8888.00,'2','湖北省武汉市江夏区纸坊','哈哈',2),(25,'壮壮哥','2009-03-12 00:00:00','1','13813245749',35000.00,'3','翻斗大街翻斗花园翻斗小区7号','哈哈',2),(26,'东兴耀阳','2020-03-11 00:00:00','0','13812345695',3000.00,'2','湖北省武汉市江夏区光谷','哈哈',2),(27,'丁丁','2020-03-08 00:00:00','1','13865748988',50000.00,'1','湖北省武汉市江夏区光谷大道久阳科技园','真的够细',1),(28,'丁丁','2020-03-12 00:00:00','1','13814523679',30000.00,'1','湖北省武汉市江夏区光谷大道','哈哈',2),(29,'耀阳','2020-03-11 00:00:00','0','13812345695',3000.00,'2','湖北省武汉市江夏区光谷','哈哈',2),(30,'测试数据一','2050-03-12 00:00:00','0','17648522861',1.00,'2','湖北省武汉市江夏区光谷大大大道','remark',3),(31,'继康','2020-03-12 00:00:00','1','15857522736',2000.00,'3','湖北省武汉市江夏区光谷大道','我爱老丁',3),(32,'阿达的哥','2020-03-12 00:00:00','1','13869874563',300023.00,'2','湖北省武汉市江夏区光谷大道','哈哈',1),(33,'汪汪','2020-03-11 00:00:00','0','13812345695',3000.00,'2','湖北省武汉市江夏区光谷','哈哈',2),(34,'王钰隆','2020-06-22 00:00:00','0','13833344455',6000.00,'2','湖北省襄阳市蜜汁汉堡店','我是老八',2),(35,'翠花','1970-03-12 00:00:00','0','13718345666',999999.00,'3','湖北省武汉市江夏区无赖小区','嘿嘿',2),(36,'老根儿','2020-03-12 00:00:00','0','15812345699',3000.00,'2','湖北省武汉市江夏区光谷大道','哈哈',2),(37,'嘤嘤嘤','2020-03-12 00:00:00','1','15812345999',3000.00,'2','湖北省武汉市江夏区光谷大道','草',2),(38,'小姐姐','2000-03-12 00:00:00','0','13812345666',9999.00,'2','湖北省武汉市江夏区光谷大道大唐沐足','哈哈',1),(39,'丁真','2020-03-12 00:00:00','0','13574185269',3000.00,'2','萨达撒飒飒阿打算打11','哈哈哈哈',2),(40,'爷的凤姐','2020-03-12 00:00:00','0','13838383837',3000.00,'2','湖北省武汉市江夏区光谷大道','哈哈',2),(41,'凤姐的奶','2020-03-12 00:00:00','0','13812345625',3000.00,'2','湖北省武汉市江夏区光谷','哈哈',2),(42,'我是林北','2020-03-11 00:00:00','0','13812345695',3000.00,'2','湖北省武汉市江夏区光谷','哈哈',2),(43,'赵薇杨','2020-03-12 00:00:00','0','13743274590',2000.00,'1','安徽省宣城市宁国市南霁翔','哈哈',1),(44,'希瓦娜','2020-03-12 00:00:00','0','13814523679',30000.00,'1','湖北省武汉市江夏区光谷大道','哈哈',2),(45,'呆呆','2020-03-12 00:00:00','0','13869874563',6666.00,'2','湖北省武汉市江夏区光谷大道','哈哈',1),(46,'健康哥哥','2009-03-12 00:00:00','1','13813245549',2000.00,'1','翻斗大街翻斗花园翻斗小区阳光幼儿园','哈哈',2),(47,'顶针巨毒','2020-03-12 00:00:00','0','15812345699',3000.00,'3','湖北省武汉市江夏区光谷大道','哈哈',2),(48,'汪武阳','1970-03-12 00:00:00','1','13718345566',5.10,'1','湖北省武汉市江夏区无赖小区','嘿嘿',2);

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
