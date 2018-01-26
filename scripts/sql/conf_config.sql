CREATE TABLE `conf_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `conf_owner` varchar(50) DEFAULT NULL COMMENT '配置拥有者如应用id',
  `path` varchar(200) DEFAULT NULL COMMENT 'config path',
  `conf_key` varchar(50) DEFAULT NULL COMMENT 'config key',
  `conf_value` varchar(2048) DEFAULT NULL COMMENT 'config value',
  `version` int(11) DEFAULT NULL COMMENT 'version',
  `timestamp` datetime NOT NULL COMMENT '时间戳',
  PRIMARY KEY (`id`),
  UNIQUE KEY (`path`, `conf_key`, `version`, `conf_owner`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

