package com.assignhub.mapper;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.assignhub.entity.Assignment;
import com.assignhub.entity.SelectOption;

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
	 * 全てのアサイン情報を取得する。
	 *
	 * @param txtEmpName 社員名の検索キーワード
	 * @param txtCompanyName 企業名の検索キーワード
	 * @return アサイン情報のリスト
	 */
	List<Assignment> findAll(@Param("txtEmpName") String txtEmpName, 
			@Param("txtassignName") String txtAssignName,
			@Param("txtCompanyName") String txtCompanyName,
			@Param("txtContractStartDate") String txtContractStartDate,
			@Param("txtContractEndDate") String txtContractEndDate);

	List<SelectOption> findEmployeeOptions();

	List<SelectOption> findCompanyOptions();

	List<SelectOption> findRoleOptions();
	
	/**
	 * IDを指定してアサイン情報を1件取得する。
	 * 
	 * @param id アサイン情報ID
	 * @return アサイン情報エンティティ
	 */
    Assignment findById(@Param("id") Integer id);

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
    
    /**
     * アサイン情報の重複をチェックする。
     * 社員ID、企業ID、契約開始日、契約終了日がすべて一致するデータを取得する。
     *
     * @param assignmentId チェック対象のアサインID（更新時は自身のIDを除外するために指定、登録時はnull）
     * @param empId 社員ID
     * @param companyId 企業ID
     * @param contractStartDate 契約開始日
     * @param contractEndDate 契約終了日
     * @return 重複するアサイン情報エンティティ（重複がない場合はnull）
     */
	Assignment findDuplicate(
    	@Param("assignmentId") Integer assignmentId,
    	@Param("empId") Integer empId,
    	@Param("companyId") Integer companyId,
    	@Param("contractStartDate") LocalDate contractStartDate,
    	@Param("contractEndDate") LocalDate contractEndDate
	);

	int countActive();
}
