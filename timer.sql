CREATE DATABASE gx_timer

CREATE TABLE `timer_lock`(
`app_id` varchar(32) NOT NULL DEFAULT '' COMMENT '接入方ID',
`lock_owner` varchar(64) NOT NULL DEFAULT '' COMMENT '锁的拥有者',
`lock_name` varchar(64) NOT NULL DEFAULT '' COMMENT '锁的名称',
`lock_status` int NOT NULL DEFAULT 0 COMMENT '0 释放, 1 锁',
`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
`update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY `idx_lock` (`app_id`, `lock_name`),
 KEY `idx_ct` (`create_time`)
)ENGINE=InnoDB  DEFAULT CHARSET=utf8

CREATE TABLE `timer_job`(
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `app_id` varchar(32) NOT NULL DEFAULT '' COMMENT '接入方ID',
  `job_id` varchar(64) NOT NULL DEFAULT '' COMMENT '目标事件ID',
  `job_status` int NOT NULL DEFAULT 0 COMMENT '0 Idle, 1 成功, -1 失败, 2 在尝试中',
  `req_type`   int  NOT NULL DEFAULT 0 COMMENT '0 GET, 1 POST, 2 PUT',
  `data_type`  int NOT NULL DEFAULT 0 COMMENT '数据类型, 1 text/xml, 2 application/json, 3 application/x-www-form-urlencoded',
  `req_url` varchar(512) NOT NULL DEFAULT '' COMMENT '被调用的URL',
  `cb_url`  varchar(512) NOT NULL DEFAULT '' COMMENT '回调给调用方的URL，在成功或者最终失败调用',
  `req_body` varchar(8192) NOT NULL DEFAULT '' COMMENT '请求内容',
  `req_header` varchar(1024) NOT NULL DEFAULT '' COMMENT '请求头部',
  `next_time` long NOT NULL DEFAULT 0 COMMENT '下次尝试时间',
  `end_time`  long NOT NULL DEFAULT 0 COMMENT '结束时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_job` (`app_id`, `job_id`), 
  KEY `idx_nt` (`next_time`),
  KEY `idx_ct` (`create_time`)
)ENGINE=InnoDB  DEFAULT CHARSET=utf8


CREATE TABLE `timer_cb`(
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `app_id` varchar(32) NOT NULL DEFAULT '' COMMENT '接入方ID',
  `job_id` varchar(64) NOT NULL DEFAULT '' COMMENT '目标事件ID',
  `job_status` int NOT NULL DEFAULT 0 COMMENT '0 Idle, 1 成功, -1 失败, 2 在尝试中',
  `req_type`   int  NOT NULL DEFAULT 0 COMMENT '0 GET, 1 POST, 2 PUT',
  `data_type`  int NOT NULL DEFAULT 0 COMMENT '数据类型, 1 text/xml, 2 application/json, 3 application/x-www-form-urlencoded',
  `req_url` varchar(512) NOT NULL DEFAULT '' COMMENT '被调用的URL',
  `cb_url`  varchar(512) NOT NULL DEFAULT '' COMMENT '回调给调用方的URL，在成功或者最终失败调用',
  `req_body` varchar(8192) NOT NULL DEFAULT '' COMMENT '请求内容',
  `req_header` varchar(1024) NOT NULL DEFAULT '' COMMENT '请求头部',
  `next_time` long NOT NULL DEFAULT 0 COMMENT '下次尝试时间',
  `end_time`  long NOT NULL DEFAULT 0 COMMENT '结束时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_job` (`app_id`, `job_id`), 
  KEY `idx_nt` (`next_time`),
  KEY `idx_ct` (`create_time`)
)ENGINE=InnoDB  DEFAULT CHARSET=utf8