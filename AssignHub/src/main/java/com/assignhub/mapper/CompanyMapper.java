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
	List<Company> findAll(@Param("companySearch") String keyword,@Param("companyTelSearch") String tel,@Param("sort") String sort, @Param("order") String order);

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

	boolean existsByCompanyName( @Param("companyName") String companyName, 
		    @Param("companyId") Integer companyId);
	
	/**
	 * 指定されたTELの登録件数を取得する（指定IDを除外）。
	 *
	 * @param TEL  重複チェックするTEL
	 * @param excludecompanyId チェックから除外する自身の企業ID
	 * @return 一致するTELの件数
	 */
	boolean existsByCompTel( @Param("companyTel") String companyTel, 
		    @Param("companyId") Integer companyId);
	
	/**
	 * 指定されたFAXの登録件数を取得する（指定IDを除外）。
	 *
	 * @param FAX 重複チェックするFAX
	 * @param excludecompanyId チェックから除外する自身の企業ID
	 * @return 一致するFAXの件数
	 */boolean existsByFax( @Param("fax") String fax, 
			    @Param("companyId") Integer companyId);

/**
 * 企業情報を物理削除する。
 *
 * @param id 企業ID
 */
void delete(Integer id);

/**
 * 複数のを一括で物理削除する。
 *
 * @param ids 削除対象IDリスト
 */
void deleteBulk(@Param("ids") List<Integer> ids);


int countAll();








}