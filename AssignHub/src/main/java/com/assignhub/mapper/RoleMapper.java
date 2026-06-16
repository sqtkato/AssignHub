package com.assignhub.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.assignhub.entity.Role;

/**
 * 役割マスタ（m_role）に対するデータベース操作を定義するマッパー。
 *
 * @version 1.01 2026/06/01
 * @author SQT）Team Excel
 */
@Mapper
public interface RoleMapper {

	/**
	 * 論理削除されていない役割を全件取得する。
	 *
	 * @return 役割リスト
	 */
	List<Role> findAll();
}