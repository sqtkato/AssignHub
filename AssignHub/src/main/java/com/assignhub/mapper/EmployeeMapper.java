package com.assignhub.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.assignhub.entity.Employee;

/**
 * 社員マスタ（m_employee）に対するデータベース操作を定義するマッパー。
 * * @version 1.01 2026/06/01
 * @author SQT）チームB
 */
@Mapper
public interface EmployeeMapper {
	/**
	 * 条件に一致する論理削除されていない社員を全件取得する。
	 *
	 * @param keyword 検索ワード（社員名の部分一致）
	 * @param sort    ソート列
	 * @param order   ソート順
	 * @return 社員リスト
	 */
	List<Employee> findAll(
	        @Param("empName") String empName, 
	        @Param("empAssignCompany") String empAssignCompany, 
	        @Param("empCompany") String empCompany, 
	        @Param("empEngineerType") String empEngineerType, 
	        @Param("sort") String sort, 
	        @Param("order") String order
	    );

	/**
	 * IDを指定して社員を1件取得する。論理削除済みのデータは取得しない。
	 *
	 * @param id 社員ID
	 * @return 社員エンティティ
	 */
	Employee findById(@Param("id") Integer id);

	/**
	 * 社員を新規登録する。
	 *
	 * @param employee 社員エンティティ
	 */
	void insert(Employee employee);
	
	void delete(Integer id);
	
	void deleteBulk(@Param("ids") List<Integer> ids);
	
	void deleteByAccountId(Integer id);
	
	void deleteBulkByAccountId(@Param("ids") List<Integer> ids);
	
	
}