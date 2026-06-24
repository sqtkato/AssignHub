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
	 * @param empName 検索ワード（社員名の部分一致）
	 * @param empAssignCompany 検索ワード(アサイン先企業名の部分一致)
	 * @param empEngineerType 検索ワード(エンジニアタイプ)
	 * @param empCompany 検索ワード(所属企業名の部分一致)
	 * @return 社員リスト
	 */
	List<Employee> findAll(@Param("empName") String empName, @Param("empAssignCompany") String empAssignCompany, @Param("empEngineerType") String empEngineerType, @Param("empCompany") String empCompany);
	
	/**
	 * IDを指定して社員を1件取得する。論理削除済みのデータは取得しない。
	 *
	 * @param id 社員ID
	 * @return 社員エンティティ
	 */
	Employee findById(@Param("id") Integer id);
    
	/**
	 * IDを指定して社員を複数件取得する。論理削除済みのデータは取得しない。
	 *
	 * @param ids 社員idのリスト
	 * @return 社員リスト
	 */
	List<Employee> findByIds(@Param("ids") List<Integer> ids);
	
	/**
	 * 社員を新規登録する。
	 *
	 * @param employee 社員エンティティ
	 */
	void insert(Employee employee);
	
	/**
	 * 社員を論理削除する。
	 *
	 * @param id 社員id
	 */
	void delete(Integer id);
	
	/**
	 * 複数の社員を一括で論理削除する。
	 *
	 * @param ids 削除対象IDリスト
	 */
	void deleteBulk(@Param("ids") List<Integer> ids);
	
	/**
	 * 社員情報を更新する。
	 *
	 * @param employee 社員エンティティ
	 */
	void update(Employee employee);
	
	/**
	 * 指定されたメールアドレスの登録件数を取得する（指定IDを除外）。
	 *
	 * @param email        重複チェックするメールアドレス
	 * @param excludeEmpId チェックから除外する自身の社員ID
	 * @return 一致するメールアドレスの件数
	 */
	int countByEmail(@Param("email") String email, @Param("excludeEmpId") Integer excludeEmpId);
	
	/**
	 * 論理削除されていない社員の登録件数を取得する。
	 * 
	 * @return 論理削除されていない社員の登録件数
	 */
	int countAll();
	
	int countByAccountId(@Param("accountId") Integer accountId, @Param("excludeEmpId") Integer excludeEmpId);

	
	
	
	void deleteByAccountId(Integer id);
	
	void deleteBulkByAccountId(@Param("ids") List<Integer> ids);
	
	void deleteByCompanyId(Integer id);
	
	void deleteBulkByCompanyId(@Param("ids") List<Integer> ids);
}