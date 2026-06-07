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
}