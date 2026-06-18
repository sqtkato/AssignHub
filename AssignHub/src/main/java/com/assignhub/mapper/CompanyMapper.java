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
	 * 
	 * 企業一覧画面を表示する。検索条件に応じたデータを取得する。
	 * @param companyName 企業名検索キーワード（任意）
	 * @param companyTel TEL検索キーワード（任意）
	 * @return 企業エンティティリスト
	 */
	List<Company> findAll(@Param("companyName") String companyName, @Param("companyTel") String companyTel);

	/**
	 * 
	 * 選択された複数の企業情報を表示する。
	 * @param ids 取得対象の企業IDリスト
	 * @return 企業エンティティリスト
	 */
	List<Company> findByIds(@Param("ids") List<Integer> ids);

	/**
	 * 
	 * 企業IDを指定して企業情報を1件取得する。
	 * 論理削除済みのデータは取得対象外とする。
	 * @param id 取得対象の企業ID
	 */
	Company findById(@Param("id") Integer id);

	/**
	 * 
	 * 企業情報を新規登録する。
	 * 論理削除フラグはデフォルトで0（有効）として登録される。
	 * @param company 登録する企業エンティティ
	 */
	void insert(Company company);

	/** 
	 * 
	 * 企業情報を更新する。
	 *@param  company 企業エンティティ
	 */
	void update(Company company);

	/**
	 * 
	 * 企業名、電話番号、FAX、企業IDがすでに登録されているか（重複しているか）を判定する。
	 * @param companyName チェックする企業名
	 * @param companyTel チェックする電話番号
	 * @param companyFax チェックするFAX
	 * @param companyId 除外する企業ID（新規登録時はnullを渡す）
	 */
	boolean existsByCompanyNameAndTelAndFax(
			@Param("companyName") String companyName,
			@Param("companyTel") String companyTel,
			@Param("companyFax") String companyFax,
			@Param("companyId") Integer companyId);

	/**
	 * 
	 * 企業情報を論理削除する。
	 * @param id 企業ID
	 */
	void delete(@Param("companyId") Integer companyId);

	/**
	 * 
	 * 複数の企業情報を一括で論理削除する。
	 * @param ids 削除対象IDリスト
	 */
	void deleteBulk(@Param("ids") List<Integer> ids);

	/**
	 * 
	 * 登録済みの企業情報を数える。
	 */
	int countAll();

}