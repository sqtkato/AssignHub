package com.assignhub.mapper;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.assignhub.entity.Assignment;

/**
 * AssignmentMapperは、Assignmentエンティティに対するデータベース操作を定義するMyBatisのMapper Interfaceです。
 * このインターフェースは、AssignmentServiceクラスから呼び出され、Assignmentテーブルに対するCRUD操作を実装します。
 * 
 * @author Team Excel
 * @version 1.00 2024/06/04
 */
@Mapper
public interface AssignmentMapper {
	
	/**
	 * アサイン情報を検索条件に基づいて取得する。
	 * 
	 * @param empName 社員名（部分一致）
	 * @param assignName アサイン名（部分一致）
	 * @param companyName 企業名（部分一致）
	 * @param contractStartDate 契約開始日（yyyy-MM-dd形式、指定された日付以降のデータを取得）
	 * @param contractEndDate 契約終了日（yyyy-MM-dd形式、指定された日付以前のデータを取得）
	 * @return アサイン情報エンティティのリスト
	 */
	List<Assignment> findAll(@Param("empName") String empName, 
			@Param("assignName") String assignName,
			@Param("companyName") String companyName,
			@Param("contractStartDate") String contractStartDate,
			@Param("contractEndDate") String contractEndDate);
	
	/**
	 * IDを指定してアサイン情報を1件取得する。
	 * 
	 * @param id アサイン情報ID
	 * @return アサイン情報エンティティ
	 */
    Assignment findById(@Param("id") Integer id);
    
    /**
     * チェックボックスにチェックされたアサイン情報IDを取得する
     * @param ids エクスポート対象のアサインIDリスト
     * @return アサイン情報エンティティのリスト
     */
    List<Assignment> findByIds(@Param("ids") List<Integer> ids);

    /**
	 * アサイン情報を新規登録する。
	 *
	 * @param assignment アサイン情報エンティティ
	 */
    void insert(Assignment assignment);
    
    /**
	 * アサイン情報を更新する。
	 * 
	 * @param assignment アサイン情報エンティティ
	 * 
	*/
    void update(Assignment assignment);
    
    /**
     * アサイン情報を論理削除する。
     * @param id
     */
    void delete(@Param("id") Integer id);
    
    /**
	 * 複数のアサイン情報を一括で論理削除する。
	 * @param ids 削除対象IDリスト
	 */
    void deleteBulk(@Param("ids") List<Integer> ids);
    
    void deleteByAccountId(@Param("id") Integer id);
    
    void deleteBulkByAccountId(@Param("ids") List<Integer> ids);
    
    /**
     * 指定した社員IDが有効な社員として存在するか確認する。
     * @param empId 社員ID
     * @return 存在すれば1以上、存在しなければ0
     */
    int existsEmployee(@Param("empId") Integer empId);
    
    /**
     * 指定した企業IDが有効な社員として存在するか確認する。
     * @param companyId 企業ID
     * @return 存在すれば1以上、存在しなければ0
     */
    int existsCompany(@Param("companyId") Integer companyId);
    
    /**
     * 企業名から企業IDを取得する（企業名はUNIQUE）。
     * @param companyName 企業名
     * @return 企業ID。存在しなければnull
     */
    Integer findCompanyIdByName(@Param("companyName") String companyName);
    /**
     * 役割名から役割IDを取得する。
     * @param role 役割名
     * @return 役割ID。存在しなければnull
     */
    Integer findRoleIdByName(@Param("role") String role);
    
    int countDuplicate(
    	    @Param("assignmentId") Integer assignmentId,
    	    @Param("empId") Integer empId,
    	    @Param("companyId") Integer companyId,
    	    @Param("contractStartDate") LocalDate contractStartDate,
    	    @Param("contractEndDate") LocalDate contractEndDate);

	int countAll();
	
	/**
     * アサイン情報を論理削除する。
     * @param id
     */
    void deleteByCompanyId(@Param("id") Integer id);
    
    void deleteBulkByCompanyId(@Param("ids") List<Integer> ids);

    /**
     * アサイン情報を論理削除する。
     * @param id 社員ID
     */
	void deleteByEmpId(@Param("id") Integer id);
	
	 /**
	  * 複数のアサイン情報を一括で論理削除する。
	  * @param ids 削除対象の社員IDリスト
	  */
	void deleteBulkByEmpId(@Param("ids") List<Integer> ids);
}