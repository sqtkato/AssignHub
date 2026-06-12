package com.assignhub.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.assignhub.entity.Company;

/**
 * 企業マスタ（m_company）に対するデータベース操作を定義するマッパー。
 * @version 1.00 2026/06/01
 * @author SQT）加藤
 */
@Mapper
public interface CompanyMapper {
	/**
	 * 条件に一致する論理削除されていない企業情報を全件取得する。
	 *
	 * @param keyword 検索ワード（企業名の部分一致）
	 * @param sort    ソート対象のカラム名
	 * @param order   ソート順（asc または desc）
	 * @return 企業エンティティのリスト
	 */
	List<Company> findAll(@Param("companyName") String companyName, @Param("companyTel") String companyTel);
	/**
	 * 企業IDを指定して企業情報を1件取得する。
	 * 論理削除済みのデータは取得対象外とする。
	 *
	 * @param id 取得対象の企業ID
	 * @return 企業エンティティ（存在しない場合、または論理削除済みの場合はnull）
	 */
	Company findById(@Param("id") Integer id);

	/**
	 * 企業情報を新規登録する。
	 * 論理削除フラグはデフォルトで0（有効）として登録される。
	 *
	 * @param company 登録する企業エンティティ
	 */
	void insert(Company company);
	
}