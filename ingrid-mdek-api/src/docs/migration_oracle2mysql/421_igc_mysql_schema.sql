-- Grundlegendes Schema für Katalog Version 4.2.0 MySQL
-- Muss für einzelne MetaVer Oracle Kataloge noch angepasst werden vor Migration, damit Daten korrekt übernommen werden !

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;


CREATE TABLE `additional_field_data` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_id` bigint(20) DEFAULT NULL,
  `sort` int(11) DEFAULT '0',
  `field_key` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `list_item_id` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `data` mediumtext COLLATE utf8_unicode_ci,
  `parent_field_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idxAddField_ObjId` (`obj_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `address_comment` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `addr_id` bigint(20) DEFAULT NULL,
  `comment_` text COLLATE utf8_unicode_ci,
  `create_uuid` varchar(40) COLLATE utf8_unicode_ci DEFAULT NULL,
  `create_time` varchar(17) COLLATE utf8_unicode_ci DEFAULT NULL,
  `line` int(11) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idxAddrCom_AddrId` (`addr_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `address_metadata` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `expiry_state` int(11) DEFAULT '0',
  `lastexport_time` varchar(17) COLLATE utf8_unicode_ci DEFAULT NULL,
  `mark_deleted` char(1) COLLATE utf8_unicode_ci DEFAULT 'N',
  `assigner_uuid` varchar(40) COLLATE utf8_unicode_ci DEFAULT NULL,
  `assign_time` varchar(17) COLLATE utf8_unicode_ci DEFAULT NULL,
  `reassigner_uuid` varchar(40) COLLATE utf8_unicode_ci DEFAULT NULL,
  `reassign_time` varchar(17) COLLATE utf8_unicode_ci DEFAULT NULL,
  `lastexpiry_time` varchar(17) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `address_node` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `addr_uuid` varchar(40) COLLATE utf8_unicode_ci NOT NULL,
  `addr_id` bigint(20) NOT NULL,
  `addr_id_published` bigint(20) DEFAULT NULL,
  `fk_addr_uuid` varchar(40) COLLATE utf8_unicode_ci DEFAULT NULL,
  `tree_path` mediumtext COLLATE utf8_unicode_ci,
  PRIMARY KEY (`id`),
  UNIQUE KEY `addr_uuid` (`addr_uuid`),
  KEY `idxAddrN_AddrId` (`addr_id`),
  KEY `idxAddrN_AddrIdPub` (`addr_id_published`),
  KEY `idxAddrN_FAddrUuid` (`fk_addr_uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `adv_product_group` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_id` bigint(20) DEFAULT NULL,
  `line` int(11) DEFAULT '0',
  `product_key` int(11) DEFAULT NULL,
  `product_value` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idxObjConf_ObjId` (`obj_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `full_index_addr` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `addr_node_id` bigint(20) NOT NULL,
  `idx_name` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `idx_value` mediumtext COLLATE utf8_unicode_ci,
  PRIMARY KEY (`id`),
  KEY `idxAddrIdxName` (`idx_name`),
  KEY `idxFullIdxAddrId` (`addr_node_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `full_index_obj` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_node_id` bigint(20) NOT NULL,
  `idx_name` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `idx_value` mediumtext COLLATE utf8_unicode_ci,
  PRIMARY KEY (`id`),
  KEY `idxObjIdxName` (`idx_name`),
  KEY `idxFullObjNodedId` (`obj_node_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `hibernate_unique_key` (
  `next_hi` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `idc_group` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `name` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `create_time` varchar(17) COLLATE utf8_unicode_ci DEFAULT NULL,
  `mod_time` varchar(17) COLLATE utf8_unicode_ci DEFAULT NULL,
  `mod_uuid` varchar(40) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `idc_user` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `parent_id` bigint(20) DEFAULT NULL,
  `addr_uuid` varchar(40) COLLATE utf8_unicode_ci DEFAULT NULL,
  `create_time` varchar(17) COLLATE utf8_unicode_ci DEFAULT NULL,
  `mod_time` varchar(17) COLLATE utf8_unicode_ci DEFAULT NULL,
  `mod_uuid` varchar(40) COLLATE utf8_unicode_ci DEFAULT NULL,
  `idc_role` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `addr_uuid` (`addr_uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `idc_user_group` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `idc_user_id` bigint(20) DEFAULT NULL,
  `idc_group_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `idc_user_permission` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `permission_id` bigint(20) DEFAULT NULL,
  `idc_group_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `object_access` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_id` bigint(20) DEFAULT NULL,
  `line` int(11) DEFAULT '0',
  `restriction_key` int(11) DEFAULT NULL,
  `restriction_value` text COLLATE utf8_unicode_ci,
  PRIMARY KEY (`id`),
  KEY `idxObjAccess_ObjId` (`obj_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `object_comment` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_id` bigint(20) DEFAULT NULL,
  `comment_` text COLLATE utf8_unicode_ci,
  `create_uuid` varchar(40) COLLATE utf8_unicode_ci DEFAULT NULL,
  `create_time` varchar(17) COLLATE utf8_unicode_ci DEFAULT NULL,
  `line` int(11) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idxObjCom_ObjId` (`obj_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `object_conformity` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_id` bigint(20) DEFAULT NULL,
  `line` int(11) DEFAULT '0',
  `degree_key` int(11) DEFAULT NULL,
  `degree_value` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `specification_key` int(11) DEFAULT NULL,
  `specification_value` text COLLATE utf8_unicode_ci,
  PRIMARY KEY (`id`),
  KEY `idxObjConf_ObjId` (`obj_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `object_data_language` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_id` bigint(20) DEFAULT NULL,
  `line` int(11) DEFAULT '0',
  `data_language_key` int(11) DEFAULT NULL,
  `data_language_value` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idxObjDLang_ObjId` (`obj_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `object_data_quality` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_id` bigint(20) DEFAULT NULL,
  `dq_element_id` int(11) DEFAULT NULL,
  `line` int(11) DEFAULT '0',
  `name_of_measure_key` int(11) DEFAULT NULL,
  `name_of_measure_value` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `result_value` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `measure_description` varchar(4000) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idxObjDq_ObjId` (`obj_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `object_format_inspire` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_id` bigint(20) DEFAULT NULL,
  `line` int(11) DEFAULT '0',
  `format_key` int(11) DEFAULT NULL,
  `format_value` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idxObjFormatInsp_ObjId` (`obj_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `object_metadata` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `expiry_state` int(11) DEFAULT '0',
  `lastexport_time` varchar(17) COLLATE utf8_unicode_ci DEFAULT NULL,
  `mark_deleted` char(1) COLLATE utf8_unicode_ci DEFAULT 'N',
  `assigner_uuid` varchar(40) COLLATE utf8_unicode_ci DEFAULT NULL,
  `assign_time` varchar(17) COLLATE utf8_unicode_ci DEFAULT NULL,
  `reassigner_uuid` varchar(40) COLLATE utf8_unicode_ci DEFAULT NULL,
  `reassign_time` varchar(17) COLLATE utf8_unicode_ci DEFAULT NULL,
  `lastexpiry_time` varchar(17) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `object_node` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_uuid` varchar(40) COLLATE utf8_unicode_ci NOT NULL,
  `obj_id` bigint(20) NOT NULL,
  `obj_id_published` bigint(20) DEFAULT NULL,
  `fk_obj_uuid` varchar(40) COLLATE utf8_unicode_ci DEFAULT NULL,
  `tree_path` mediumtext COLLATE utf8_unicode_ci,
  PRIMARY KEY (`id`),
  UNIQUE KEY `obj_uuid` (`obj_uuid`),
  KEY `idxObjN_ObjId` (`obj_id`),
  KEY `idxObjN_ObjIdPub` (`obj_id_published`),
  KEY `idxObjN_FObjUuid` (`fk_obj_uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `object_open_data_category` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_id` bigint(20) DEFAULT NULL,
  `line` int(11) DEFAULT '0',
  `category_key` int(11) DEFAULT NULL,
  `category_value` text COLLATE utf8_unicode_ci,
  PRIMARY KEY (`id`),
  KEY `idxObjODCategory_ObjId` (`obj_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `object_reference` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_from_id` bigint(20) NOT NULL,
  `obj_to_uuid` varchar(40) COLLATE utf8_unicode_ci NOT NULL,
  `line` int(11) DEFAULT '0',
  `special_ref` int(11) DEFAULT '0',
  `special_name` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `descr` text COLLATE utf8_unicode_ci,
  PRIMARY KEY (`id`),
  KEY `idxObjRef_OFromId` (`obj_from_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `object_types_catalogue` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_id` bigint(20) DEFAULT NULL,
  `line` int(11) DEFAULT '0',
  `title_key` int(11) DEFAULT NULL,
  `title_value` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `type_date` varchar(17) COLLATE utf8_unicode_ci DEFAULT NULL,
  `type_version` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idxOTypCat_ObjId` (`obj_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `object_use` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_id` bigint(20) DEFAULT NULL,
  `line` int(11) DEFAULT '0',
  `terms_of_use_key` int(11) DEFAULT NULL,
  `terms_of_use_value` text COLLATE utf8_unicode_ci,
  PRIMARY KEY (`id`),
  KEY `idxObjUse_ObjId` (`obj_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `object_use_constraint` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_id` bigint(20) DEFAULT NULL,
  `line` int(11) DEFAULT '0',
  `license_key` int(11) DEFAULT NULL,
  `license_value` text COLLATE utf8_unicode_ci,
  PRIMARY KEY (`id`),
  KEY `idxObjUConstr_ObjId` (`obj_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `permission` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `class_name` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `name` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `action` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `permission_addr` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `uuid` varchar(40) COLLATE utf8_unicode_ci DEFAULT NULL,
  `permission_id` bigint(20) DEFAULT NULL,
  `idc_group_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXperm_addr_uuid` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `permission_obj` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `uuid` varchar(40) COLLATE utf8_unicode_ci DEFAULT NULL,
  `permission_id` bigint(20) DEFAULT NULL,
  `idc_group_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXperm_obj_uuid` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `searchterm_adr` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `adr_id` bigint(20) NOT NULL,
  `line` int(11) NOT NULL DEFAULT '0',
  `searchterm_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idxSTAdr_AdrId` (`adr_id`),
  KEY `idxSTAdr_STId` (`searchterm_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `searchterm_obj` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_id` bigint(20) NOT NULL,
  `line` int(11) NOT NULL DEFAULT '0',
  `searchterm_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `searchterm_sns` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `sns_id` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `expired_at` varchar(17) COLLATE utf8_unicode_ci DEFAULT NULL,
  `gemet_id` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idxTermSnsId` (`sns_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `searchterm_value` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `type` char(1) COLLATE utf8_unicode_ci DEFAULT NULL,
  `searchterm_sns_id` bigint(20) DEFAULT NULL,
  `term` text COLLATE utf8_unicode_ci,
  `entry_id` int(11) DEFAULT NULL,
  `alternate_term` text COLLATE utf8_unicode_ci,
  PRIMARY KEY (`id`),
  KEY `idxSTVal_STSNSId` (`searchterm_sns_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `spatial_reference` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_id` bigint(20) NOT NULL,
  `line` int(11) NOT NULL DEFAULT '0',
  `spatial_ref_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idxSRef_ObjId` (`obj_id`),
  KEY `idxSRef_SRefId` (`spatial_ref_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `spatial_ref_sns` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `sns_id` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `expired_at` varchar(17) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idxSpatialSnsId` (`sns_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `spatial_ref_value` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `type` char(1) COLLATE utf8_unicode_ci DEFAULT NULL,
  `spatial_ref_sns_id` bigint(20) DEFAULT NULL,
  `name_key` int(11) DEFAULT NULL,
  `name_value` text COLLATE utf8_unicode_ci,
  `nativekey` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `x1` double DEFAULT NULL,
  `y1` double DEFAULT NULL,
  `x2` double DEFAULT NULL,
  `y2` double DEFAULT NULL,
  `topic_type` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idxSRVal_SRefSNSId` (`spatial_ref_sns_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `spatial_system` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_id` bigint(20) NOT NULL,
  `line` int(11) NOT NULL DEFAULT '0',
  `referencesystem_key` int(11) DEFAULT NULL,
  `referencesystem_value` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idxSSys_ObjId` (`obj_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `sys_generic_key` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `key_name` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `value_string` mediumtext COLLATE utf8_unicode_ci,
  PRIMARY KEY (`id`),
  UNIQUE KEY `key_name` (`key_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `sys_job_info` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `job_type` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL,
  `user_uuid` varchar(40) COLLATE utf8_unicode_ci DEFAULT NULL,
  `start_time` varchar(17) COLLATE utf8_unicode_ci DEFAULT NULL,
  `end_time` varchar(17) COLLATE utf8_unicode_ci DEFAULT NULL,
  `job_details` mediumtext COLLATE utf8_unicode_ci,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `sys_list` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `lst_id` int(11) NOT NULL DEFAULT '0',
  `entry_id` int(11) NOT NULL DEFAULT '0',
  `lang_id` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `name` text COLLATE utf8_unicode_ci,
  `description` text COLLATE utf8_unicode_ci,
  `maintainable` int(11) DEFAULT '0',
  `is_default` char(1) COLLATE utf8_unicode_ci DEFAULT 'N',
  `line` int(11) DEFAULT '0',
  `data` text COLLATE utf8_unicode_ci,
  PRIMARY KEY (`id`),
  UNIQUE KEY `entry_id` (`entry_id`,`lst_id`,`lang_id`),
  KEY `idxSysList_LstId` (`lst_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `t01_object` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_uuid` varchar(40) COLLATE utf8_unicode_ci NOT NULL,
  `obj_name` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `org_obj_id` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `obj_class` int(11) DEFAULT NULL,
  `obj_descr` text COLLATE utf8_unicode_ci,
  `cat_id` bigint(20) DEFAULT NULL,
  `info_note` text COLLATE utf8_unicode_ci,
  `loc_descr` text COLLATE utf8_unicode_ci,
  `time_from` varchar(17) COLLATE utf8_unicode_ci DEFAULT NULL,
  `time_to` varchar(17) COLLATE utf8_unicode_ci DEFAULT NULL,
  `time_descr` text COLLATE utf8_unicode_ci,
  `time_period` int(11) DEFAULT NULL,
  `time_interval` varchar(40) COLLATE utf8_unicode_ci DEFAULT NULL,
  `time_status` int(11) DEFAULT NULL,
  `time_alle` varchar(40) COLLATE utf8_unicode_ci DEFAULT NULL,
  `time_type` varchar(5) COLLATE utf8_unicode_ci DEFAULT NULL,
  `publish_id` int(11) DEFAULT NULL,
  `dataset_alternate_name` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `dataset_character_set` int(11) DEFAULT NULL,
  `dataset_usage` text COLLATE utf8_unicode_ci,
  `metadata_character_set` int(11) DEFAULT NULL,
  `metadata_standard_name` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `metadata_standard_version` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `vertical_extent_minimum` double DEFAULT NULL,
  `vertical_extent_maximum` double DEFAULT NULL,
  `vertical_extent_unit` int(11) DEFAULT NULL,
  `ordering_instructions` text COLLATE utf8_unicode_ci,
  `is_catalog_data` varchar(1) COLLATE utf8_unicode_ci DEFAULT 'N',
  `work_state` char(1) COLLATE utf8_unicode_ci DEFAULT 'V',
  `create_time` varchar(17) COLLATE utf8_unicode_ci DEFAULT NULL,
  `mod_time` varchar(17) COLLATE utf8_unicode_ci DEFAULT NULL,
  `mod_uuid` varchar(40) COLLATE utf8_unicode_ci DEFAULT NULL,
  `responsible_uuid` varchar(40) COLLATE utf8_unicode_ci DEFAULT NULL,
  `obj_metadata_id` bigint(20) DEFAULT NULL,
  `metadata_language_key` int(11) DEFAULT NULL,
  `metadata_language_value` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `vertical_extent_vdatum_key` int(11) DEFAULT NULL,
  `vertical_extent_vdatum_value` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `is_inspire_relevant` varchar(1) COLLATE utf8_unicode_ci DEFAULT 'N',
  `is_open_data` varchar(1) COLLATE utf8_unicode_ci DEFAULT 'N',
  `is_adv_compatible` varchar(1) COLLATE utf8_unicode_ci DEFAULT 'N',
  `is_inspire_conform` varchar(1) COLLATE utf8_unicode_ci DEFAULT 'N',
  PRIMARY KEY (`id`),
  KEY `idxObj_CatId` (`cat_id`),
  KEY `idxObjClass` (`obj_class`),
  KEY `idx_ObjMeta` (`obj_metadata_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `t011_obj_data` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_id` bigint(20) DEFAULT NULL,
  `base` text COLLATE utf8_unicode_ci,
  `description` text COLLATE utf8_unicode_ci,
  PRIMARY KEY (`id`),
  KEY `idxOData_ObjId` (`obj_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `t011_obj_data_para` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_id` bigint(20) DEFAULT NULL,
  `line` int(11) DEFAULT NULL,
  `parameter` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `unit` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idxODataPara_ObjId` (`obj_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `t011_obj_geo` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_id` bigint(20) DEFAULT NULL,
  `special_base` text COLLATE utf8_unicode_ci,
  `data_base` text COLLATE utf8_unicode_ci,
  `method` text COLLATE utf8_unicode_ci,
  `rec_exact` double DEFAULT NULL,
  `rec_grade` double DEFAULT NULL,
  `hierarchy_level` int(11) DEFAULT NULL,
  `vector_topology_level` int(11) DEFAULT NULL,
  `pos_accuracy_vertical` double DEFAULT NULL,
  `keyc_incl_w_dataset` int(11) DEFAULT '0',
  `datasource_uuid` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `grid_pos_accuracy` double DEFAULT NULL,
  `transformation_parameter` varchar(1) COLLATE utf8_unicode_ci DEFAULT NULL,
  `num_dimensions` int(11) DEFAULT NULL,
  `axis_dim_name` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `axis_dim_size` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `cell_geometry` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `geo_rectified` varchar(1) COLLATE utf8_unicode_ci DEFAULT NULL,
  `geo_rect_checkpoint` varchar(1) COLLATE utf8_unicode_ci DEFAULT NULL,
  `geo_rect_description` text COLLATE utf8_unicode_ci,
  `geo_rect_corner_point` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `geo_rect_point_in_pixel` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `geo_ref_control_point` varchar(1) COLLATE utf8_unicode_ci DEFAULT NULL,
  `geo_ref_orientation_parameter` varchar(1) COLLATE utf8_unicode_ci DEFAULT NULL,
  `geo_ref_parameter` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idxOGeo_ObjId` (`obj_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `t011_obj_geo_scale` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_geo_id` bigint(20) DEFAULT NULL,
  `line` int(11) DEFAULT '0',
  `scale` int(11) DEFAULT NULL,
  `resolution_ground` double DEFAULT NULL,
  `resolution_scan` double DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idxOGeoScal_OGeoId` (`obj_geo_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `t011_obj_geo_spatial_rep` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_geo_id` bigint(20) DEFAULT NULL,
  `line` int(11) DEFAULT '0',
  `type` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idxOGeoSpat_OGeoId` (`obj_geo_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `t011_obj_geo_supplinfo` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_geo_id` bigint(20) DEFAULT NULL,
  `line` int(11) DEFAULT '0',
  `feature_type` text COLLATE utf8_unicode_ci,
  PRIMARY KEY (`id`),
  KEY `idxOGeoSupp_OGeoId` (`obj_geo_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `t011_obj_geo_symc` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_geo_id` bigint(20) DEFAULT NULL,
  `line` int(11) DEFAULT '0',
  `symbol_cat_key` int(11) DEFAULT NULL,
  `symbol_cat_value` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `symbol_date` varchar(17) COLLATE utf8_unicode_ci DEFAULT NULL,
  `edition` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idxOGeoSymc_OGeoId` (`obj_geo_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `t011_obj_geo_vector` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_geo_id` bigint(20) DEFAULT NULL,
  `line` int(11) DEFAULT '0',
  `geometric_object_type` int(11) DEFAULT NULL,
  `geometric_object_count` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idxOGeoVect_OGeoId` (`obj_geo_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `t011_obj_literature` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_id` bigint(20) DEFAULT NULL,
  `author` text COLLATE utf8_unicode_ci,
  `publisher` text COLLATE utf8_unicode_ci,
  `type_key` int(11) DEFAULT NULL,
  `type_value` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `publish_in` text COLLATE utf8_unicode_ci,
  `volume` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `sides` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `publish_year` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `publish_loc` text COLLATE utf8_unicode_ci,
  `loc` text COLLATE utf8_unicode_ci,
  `doc_info` text COLLATE utf8_unicode_ci,
  `base` text COLLATE utf8_unicode_ci,
  `isbn` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `publishing` text COLLATE utf8_unicode_ci,
  `description` text COLLATE utf8_unicode_ci,
  PRIMARY KEY (`id`),
  KEY `idxOLit_ObjId` (`obj_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `t011_obj_project` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_id` bigint(20) DEFAULT NULL,
  `leader` text COLLATE utf8_unicode_ci,
  `member` text COLLATE utf8_unicode_ci,
  `description` text COLLATE utf8_unicode_ci,
  PRIMARY KEY (`id`),
  KEY `idxOProj_ObjId` (`obj_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `t011_obj_serv` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_id` bigint(20) DEFAULT NULL,
  `type_key` int(11) DEFAULT NULL,
  `type_value` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `history` text COLLATE utf8_unicode_ci,
  `environment` text COLLATE utf8_unicode_ci,
  `description` text COLLATE utf8_unicode_ci,
  `base` text COLLATE utf8_unicode_ci,
  `has_access_constraint` varchar(1) COLLATE utf8_unicode_ci DEFAULT 'N',
  `coupling_type` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `has_atom_download` varchar(1) COLLATE utf8_unicode_ci DEFAULT 'N',
  PRIMARY KEY (`id`),
  KEY `idxObjServ_ObjId` (`obj_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `t011_obj_serv_operation` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_serv_id` bigint(20) DEFAULT NULL,
  `line` int(11) DEFAULT '0',
  `name_key` int(11) DEFAULT NULL,
  `name_value` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `descr` text COLLATE utf8_unicode_ci,
  `invocation_name` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idxOSerOper_OSerId` (`obj_serv_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `t011_obj_serv_op_connpoint` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_serv_op_id` bigint(20) DEFAULT NULL,
  `line` int(11) DEFAULT '0',
  `connect_point` text COLLATE utf8_unicode_ci,
  PRIMARY KEY (`id`),
  KEY `idxOSerOCP_OSerOId` (`obj_serv_op_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `t011_obj_serv_op_depends` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_serv_op_id` bigint(20) DEFAULT NULL,
  `line` int(11) DEFAULT '0',
  `depends_on` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idxOserODe_OSerOId` (`obj_serv_op_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `t011_obj_serv_op_para` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_serv_op_id` bigint(20) DEFAULT NULL,
  `line` int(11) DEFAULT '0',
  `name` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `direction` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `descr` text COLLATE utf8_unicode_ci,
  `optional` int(11) DEFAULT NULL,
  `repeatability` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idxOSerOPa_OSerOId` (`obj_serv_op_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `t011_obj_serv_op_platform` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_serv_op_id` bigint(20) DEFAULT NULL,
  `line` int(11) DEFAULT '0',
  `platform_key` int(11) DEFAULT NULL,
  `platform_value` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idxOSerOPl_OSerOId` (`obj_serv_op_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `t011_obj_serv_scale` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_serv_id` bigint(20) DEFAULT NULL,
  `line` int(11) DEFAULT '0',
  `scale` int(11) DEFAULT NULL,
  `resolution_ground` double DEFAULT NULL,
  `resolution_scan` double DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idxOSrvScal_OSrvId` (`obj_serv_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `t011_obj_serv_type` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_serv_id` bigint(20) DEFAULT NULL,
  `line` int(11) DEFAULT '0',
  `serv_type_key` int(11) DEFAULT NULL,
  `serv_type_value` text COLLATE utf8_unicode_ci,
  PRIMARY KEY (`id`),
  KEY `idxOSerType_OSerId` (`obj_serv_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `t011_obj_serv_url` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_serv_id` bigint(20) DEFAULT NULL,
  `line` int(11) DEFAULT '0',
  `name` varchar(1024) COLLATE utf8_unicode_ci DEFAULT NULL,
  `url` varchar(1024) COLLATE utf8_unicode_ci DEFAULT NULL,
  `description` varchar(4000) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idxOSerUrl_OSerId` (`obj_serv_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `t011_obj_serv_version` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_serv_id` bigint(20) DEFAULT NULL,
  `line` int(11) DEFAULT '0',
  `version_key` int(11) DEFAULT NULL,
  `version_value` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idxOSerVers_OSerId` (`obj_serv_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `t011_obj_topic_cat` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_id` bigint(20) DEFAULT NULL,
  `line` int(11) DEFAULT '0',
  `topic_category` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `t011_town_loc_town` (
  `township_no` varchar(12) COLLATE utf8_unicode_ci DEFAULT NULL,
  `name` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `t0110_avail_format` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_id` bigint(20) DEFAULT NULL,
  `line` int(11) DEFAULT '0',
  `format_value` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `format_key` int(11) DEFAULT NULL,
  `ver` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `file_decompression_technique` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `specification` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idxAvFormat_ObjId` (`obj_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `t0112_media_option` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_id` bigint(20) DEFAULT NULL,
  `line` int(11) DEFAULT '0',
  `medium_note` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `medium_name` int(11) DEFAULT NULL,
  `transfer_size` double DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idxMediaOp_ObjId` (`obj_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `t0113_dataset_reference` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_id` bigint(20) DEFAULT NULL,
  `line` int(11) DEFAULT '0',
  `reference_date` varchar(17) COLLATE utf8_unicode_ci DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idxDatRef_ObjId` (`obj_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `t0114_env_topic` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_id` bigint(20) NOT NULL,
  `line` int(11) DEFAULT NULL,
  `topic_key` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idxEnvTop_ObjId` (`obj_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `t012_obj_adr` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_id` bigint(20) NOT NULL,
  `adr_uuid` varchar(40) COLLATE utf8_unicode_ci NOT NULL,
  `type` int(11) NOT NULL DEFAULT '0',
  `line` int(11) DEFAULT '0',
  `special_ref` int(11) DEFAULT '0',
  `special_name` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `mod_time` varchar(17) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `obj_id` (`obj_id`,`adr_uuid`,`type`),
  KEY `idxObjAdr_ObjId` (`obj_id`),
  KEY `idxObjAdr_AdrUuid` (`adr_uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `t014_info_impart` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_id` bigint(20) DEFAULT NULL,
  `line` int(11) DEFAULT '0',
  `impart_value` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `impart_key` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idxInfImpart_ObjId` (`obj_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `t015_legist` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_id` bigint(20) DEFAULT NULL,
  `line` int(11) DEFAULT '0',
  `legist_value` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `legist_key` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idxLegis_ObjId` (`obj_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `t017_url_ref` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `obj_id` bigint(20) DEFAULT NULL,
  `line` int(11) DEFAULT '0',
  `url_link` text COLLATE utf8_unicode_ci,
  `special_ref` int(11) DEFAULT NULL,
  `special_name` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `content` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `descr` text COLLATE utf8_unicode_ci,
  `url_type` int(11) DEFAULT NULL,
  `datatype_key` int(11) DEFAULT NULL,
  `datatype_value` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idxUrlRef_ObjId` (`obj_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `t02_address` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `adr_uuid` varchar(40) COLLATE utf8_unicode_ci DEFAULT NULL,
  `org_adr_id` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `adr_type` int(11) DEFAULT NULL,
  `institution` text COLLATE utf8_unicode_ci,
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
  `hours_of_service` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `administrative_area_key` int(11) DEFAULT NULL,
  `administrative_area_value` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_AddrMeta` (`addr_metadata_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `t021_communication` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `adr_id` bigint(20) NOT NULL,
  `line` int(11) NOT NULL DEFAULT '0',
  `commtype_key` int(11) DEFAULT NULL,
  `commtype_value` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `comm_value` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `descr` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idxComm_AdrId` (`adr_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `t03_catalogue` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `cat_uuid` varchar(40) COLLATE utf8_unicode_ci NOT NULL,
  `cat_name` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `partner_name` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `provider_name` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `spatial_ref_id` bigint(20) DEFAULT NULL,
  `workflow_control` char(1) COLLATE utf8_unicode_ci DEFAULT 'N',
  `expiry_duration` int(11) DEFAULT NULL,
  `create_time` varchar(17) COLLATE utf8_unicode_ci DEFAULT NULL,
  `mod_uuid` varchar(40) COLLATE utf8_unicode_ci DEFAULT NULL,
  `mod_time` varchar(17) COLLATE utf8_unicode_ci DEFAULT NULL,
  `country_key` int(11) DEFAULT NULL,
  `country_value` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `language_key` int(11) DEFAULT NULL,
  `language_value` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `cat_namespace` varchar(1024) COLLATE utf8_unicode_ci DEFAULT NULL,
  `atom_download_url` text COLLATE utf8_unicode_ci,
  PRIMARY KEY (`id`),
  KEY `idxCat_SRefId` (`spatial_ref_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
