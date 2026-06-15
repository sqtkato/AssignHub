package com.assignhub.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.assignhub.entity.Employee;
import com.assignhub.mapper.EmployeeMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * 社員管理に関するビジネスロジックを提供するサービスクラス。
 *
 * @version 1.01 2026/06/01
 * @author SQT）チームB
 */
@Slf4j
@Service
public class EmployeeService {

	private final EmployeeMapper employeeMapper;

	/**
	 * コンストラクタによる依存性の注入。
	 *
	 * @param employeeMapper 社員マスタに対するマッパー
	 */
	public EmployeeService(EmployeeMapper employeeMapper) {
		this.employeeMapper = employeeMapper;
	}

	/**
	 * 検索条件およびソート条件に合致する社員情報を全件取得する。
	 *
	 * @param keyword 検索キーワード（社員名の部分一致）
	 * @param sort    ソート対象のカラム名
	 * @param order   昇順（asc）または降順（desc）
	 * @return 社員エンティティのリスト
	 */
	public List<Employee> findAll(String empName, String empAssignCompany, String empCompany, String empEngineerType, String sort, String order) {
		// Controller から受け取った6つの引数を、そのまま Mapper へ中継します
		return employeeMapper.findAll(empName, empAssignCompany, empCompany, empEngineerType, sort, order);
	}

	/**
	 * 社員IDを指定して、社員情報を1件取得する。
	 *
	 * @param id 取得対象の社員ID
	 * @return 該当する社員エンティティ（存在しない、または論理削除済みの場合はnull）
	 */
	public Employee findById(Integer id) {
		return employeeMapper.findById(id);
	}

	/**
	 * 社員情報を保存する。
	 * IDが存在しない場合（nullまたは0）は新規登録（INSERT）、存在する場合は更新（UPDATE）を行う。
	 *
	 * @param employee 登録または更新する社員エンティティ
	 */
	@Transactional(rollbackFor = Exception.class)
	public void save(Employee employee) {
		employeeMapper.insert(employee);
	}
	
	

	@Transactional
	public void delete(Integer id) {
		employeeMapper.delete(id);
	}

	@Transactional
	public void deleteBulk(List<Integer> ids) {
		if (ids != null && !ids.isEmpty()) {
			employeeMapper.deleteBulk(ids);
		}
	}
}