package com.assignhub.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.assignhub.entity.Company;
import com.assignhub.mapper.CompanyMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * 企業情報（顧客・パートナー）管理に関するビジネスロジックを提供するサービスクラス。
 * * @version 1.00 2026/06/01
 * @author SQT）チームC
 */
@Slf4j
@Service
public class CompanyService {

	private final CompanyMapper companyMapper;

	/**
	 * コンストラクタによる依存性の注入。
	 *
	 * @param companyMapper 企業マスタに対するマッパー
	 */
	public CompanyService(CompanyMapper companyMapper) {
		this.companyMapper = companyMapper;
	}

	/**
	 * 検索条件およびソート条件に合致する企業情報を全件取得する。
	 *
	 * @param keyword 検索キーワード（企業名の部分一致）
	 * @param sort    ソート対象のカラム名
	 * @param order   昇順（asc）または降順（desc）
	 * @return 企業エンティティのリスト
	 */
	public List<Company> findAll(String keyword, String sort, String order) {
		return companyMapper.findAll(keyword, sort, order);
	}

	/**
	 * 企業IDを指定して、企業情報を1件取得する。
	 *
	 * @param id 取得対象の企業ID
	 * @return 該当する企業エンティティ（存在しない、または論理削除済みの場合はnull）
	 */
	public Company findById(Integer id) {
		return companyMapper.findById(id);
	}

	
	/**
	 * 企業情報を保存する。
	 * IDが存在しない場合（nullまたは0）は新規登録（INSERT）、存在する場合は更新（UPDATE）を行う。
	 *
	 * @param company 登録または更新する企業エンティティ
	 */
	@Transactional(rollbackFor = Exception.class)
	public void save(Company company) {
		companyMapper.insert(company);
	}
	
	/**
	 * 企業名がすでに登録されているか（重複しているか）を判定する。
	 *
	 * @param 企業名 チェックする企業名
	 * @param excludecompanyId 除外する企業ID（新規登録時はnullを渡す）
	 * @return 重複していればtrue
	 */
	public boolean isCompanyNameDuplicate(String companyName, Integer excludeCompanyId) {
		int count = companyMapper.countByCompanyName(companyName, excludeCompanyId);
		return count > 0;
	}
	public void delete(Integer id) {
		companyMapper.delete(id);

	}

	/**
	 * 指定された複数の企業IDのデータを一括で物理削除する。
	 *
	 * @param ids 削除対象となる社員IDのリスト
	 */
	@Transactional
	public void deleteBulk(List<Integer> ids) {
		if (ids != null && !ids.isEmpty()) {
			companyMapper.deleteBulk(ids);
		}
	}
	/**
	 * TELがすでに登録されているか（重複しているか）を判定する。
	 *
	 * @param TEL チェックするTEL
	 * @param excludecompanyId 除外する企業ID（新規登録時はnullを渡す）
	 * @return 重複していればtrue
	 */
	public boolean isTelDuplicate(String compTel,Integer companyId ) {
		return companyMapper.existsByCompTel(compTel, companyId);
	}
	
	/**
	 * FAXがすでに登録されているか（重複しているか）を判定する。
	 *
	 * @param TEL チェックするFAX
	 * @param excludecompanyId 除外する企業ID（新規登録時はnullを渡す）
	 * @return 重複していればtrue
	 */
	public boolean isFaxDuplicate(String fax,Integer companyId ) {
		return companyMapper.existsByFax(fax, companyId);
	}

	/**
	 * インポート時の各行のエラー内容を保持するクラス。
	 */
	public static class CsvRowError {
		public int rowNum;
		public String field;
		public String message;

		public CsvRowError(int rowNum, String field, String message) {
			this.rowNum = rowNum;
			this.field = field;
			this.message = message;
		}
	}
		
}