-- Vor Migration Oracle nach MySQL das MySQL Schema auf den gleichen Stand wie Oracle bringen, damit Daten korrekt überführt werden (Spaltenreihenfolge ...)

DROP TABLE IF EXISTS `searchterm_value`;
CREATE TABLE `searchterm_value` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `type` char(1) COLLATE utf8_unicode_ci DEFAULT NULL,
  `searchterm_sns_id` bigint(20) DEFAULT NULL,
  `entry_id` int(11) DEFAULT NULL,
  `term` text COLLATE utf8_unicode_ci,
  `alternate_term` text COLLATE utf8_unicode_ci,
  PRIMARY KEY (`id`),
  KEY `idxSTVal_STSNSId` (`searchterm_sns_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

DROP TABLE IF EXISTS `spatial_ref_value`;
CREATE TABLE `spatial_ref_value` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `type` char(1) COLLATE utf8_unicode_ci DEFAULT NULL,
  `spatial_ref_sns_id` bigint(20) DEFAULT NULL,
  `name_key` int(11) DEFAULT NULL,
  `nativekey` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `x1` double DEFAULT NULL,
  `y1` double DEFAULT NULL,
  `x2` double DEFAULT NULL,
  `y2` double DEFAULT NULL,
  `topic_type` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `name_value` text COLLATE utf8_unicode_ci,
  PRIMARY KEY (`id`),
  KEY `idxSRVal_SRefSNSId` (`spatial_ref_sns_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

DROP TABLE IF EXISTS `sys_list`;
CREATE TABLE `sys_list` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `lst_id` int(11) NOT NULL DEFAULT '0',
  `entry_id` int(11) NOT NULL DEFAULT '0',
  `lang_id` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `maintainable` int(11) DEFAULT '0',
  `is_default` char(1) COLLATE utf8_unicode_ci DEFAULT 'N',
  `line` int(11) DEFAULT '0',
  `data` text COLLATE utf8_unicode_ci,
  `name` text COLLATE utf8_unicode_ci,
  `description` text COLLATE utf8_unicode_ci,
  PRIMARY KEY (`id`),
  UNIQUE KEY `entry_id` (`entry_id`,`lst_id`,`lang_id`),
  KEY `idxSysList_LstId` (`lst_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

DROP TABLE IF EXISTS `t011_obj_data`;
CREATE TABLE `t011_obj_data` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_id` bigint(20) DEFAULT NULL,
  `description` text COLLATE utf8_unicode_ci,
  `base` text COLLATE utf8_unicode_ci,
  PRIMARY KEY (`id`),
  KEY `idxOData_ObjId` (`obj_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

DROP TABLE IF EXISTS `t011_obj_literature`;
CREATE TABLE `t011_obj_literature` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_id` bigint(20) DEFAULT NULL,
  `type_key` int(11) DEFAULT NULL,
  `type_value` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `volume` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `sides` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `publish_year` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `base` text COLLATE utf8_unicode_ci,
  `isbn` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `description` text COLLATE utf8_unicode_ci,
  `author` text COLLATE utf8_unicode_ci,
  `publisher` text COLLATE utf8_unicode_ci,
  `publish_in` text COLLATE utf8_unicode_ci,
  `publish_loc` text COLLATE utf8_unicode_ci,
  `loc` text COLLATE utf8_unicode_ci,
  `doc_info` text COLLATE utf8_unicode_ci,
  `publishing` text COLLATE utf8_unicode_ci,
  PRIMARY KEY (`id`),
  KEY `idxOLit_ObjId` (`obj_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

DROP TABLE IF EXISTS `t011_obj_project`;
CREATE TABLE `t011_obj_project` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_id` bigint(20) DEFAULT NULL,
  `description` text COLLATE utf8_unicode_ci,
  `leader` text COLLATE utf8_unicode_ci,
  `member` text COLLATE utf8_unicode_ci,
  PRIMARY KEY (`id`),
  KEY `idxOProj_ObjId` (`obj_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

DROP TABLE IF EXISTS `t011_obj_serv`;
CREATE TABLE `t011_obj_serv` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_id` bigint(20) DEFAULT NULL,
  `type_key` int(11) DEFAULT NULL,
  `type_value` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `history` text COLLATE utf8_unicode_ci,
  `environment` text COLLATE utf8_unicode_ci,
  `base` text COLLATE utf8_unicode_ci,
  `description` text COLLATE utf8_unicode_ci,
  `has_access_constraint` varchar(1) COLLATE utf8_unicode_ci DEFAULT 'N',
  `coupling_type` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `has_atom_download` varchar(1) COLLATE utf8_unicode_ci DEFAULT 'N',
  PRIMARY KEY (`id`),
  KEY `idxObjServ_ObjId` (`obj_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

DROP TABLE IF EXISTS `t017_url_ref`;
CREATE TABLE `t017_url_ref` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_id` bigint(20) DEFAULT NULL,
  `line` int(11) DEFAULT '0',
  `special_ref` int(11) DEFAULT NULL,
  `special_name` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `content` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `descr` text COLLATE utf8_unicode_ci,
  `url_type` int(11) DEFAULT NULL,
  `datatype_key` int(11) DEFAULT NULL,
  `datatype_value` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `url_link` text COLLATE utf8_unicode_ci,
  PRIMARY KEY (`id`),
  KEY `idxUrlRef_ObjId` (`obj_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

DROP TABLE IF EXISTS `t02_address`;
CREATE TABLE `t02_address` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `adr_uuid` varchar(40) COLLATE utf8_unicode_ci DEFAULT NULL,
  `org_adr_id` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `adr_type` int(11) DEFAULT NULL,
  `lastname` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `firstname` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `address_key` int(11) DEFAULT NULL,
  `address_value` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `title_key` int(11) DEFAULT NULL,
  `title_value` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `street` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `postcode` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `postbox` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `postbox_pc` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `city` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `job` text COLLATE utf8_unicode_ci,
  `work_state` char(1) COLLATE utf8_unicode_ci DEFAULT 'V',
  `create_time` varchar(17) COLLATE utf8_unicode_ci DEFAULT NULL,
  `mod_time` varchar(17) COLLATE utf8_unicode_ci DEFAULT NULL,
  `mod_uuid` varchar(40) COLLATE utf8_unicode_ci DEFAULT NULL,
  `responsible_uuid` varchar(40) COLLATE utf8_unicode_ci DEFAULT NULL,
  `addr_metadata_id` bigint(20) DEFAULT NULL,
  `country_key` int(11) DEFAULT NULL,
  `country_value` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `hide_address` varchar(1) COLLATE utf8_unicode_ci DEFAULT 'N',
  `publish_id` int(11) DEFAULT NULL,
  `institution` text COLLATE utf8_unicode_ci,
  `hours_of_service` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `administrative_area_key` int(11) DEFAULT NULL,
  `administrative_area_value` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_AddrMeta` (`addr_metadata_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
