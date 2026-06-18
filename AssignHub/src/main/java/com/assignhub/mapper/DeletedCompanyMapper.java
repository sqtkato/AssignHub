package com.assignhub.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.assignhub.entity.Company;

@Mapper
public interface DeletedCompanyMapper {
	/**
     * 論理削除済みの企業情報を全件取得する。
     * 
     */
    List<Company> findAll(
            @Param("companyName") String companyName, 
            @Param("companyTel") String companyTel);
    
    List<Company> findByIds(@Param("ids") List<Integer> ids);
    
    int restore(@Param("id") Integer id);
    
    int restoreBulk(@Param("ids") List<Integer> ids);
    
    int physicalDelete(@Param("id") Integer id);
    
    int physicalDeleteBulk(@Param("ids") List<Integer> ids);
    
    /**
     * 指定された企業IDを「派遣先」または「所属元」として使用している社員数をカウント。
     */
    int existEmployeesByCompanyId(@Param("id") Integer id);
    int existEmployeesByCompanyIds(@Param("ids") List<Integer> ids);
    
    /**
     * 指定された企業IDを使用しているアサイン履歴数をカウント。
     */
    int existAssignmentsByCompanyId(@Param("id") Integer id);
    int existAssignmentsByCompanyIds(@Param("ids") List<Integer> ids);
    
    /**
	 * 現在有効な（削除されていない）企業総数をカウントする。
	 * （500件登録上限チェック用）
	 */
	int countActiveCompanies();
	
	boolean existsByCompanyName(@Param("companyName") String companyName,@Param("companyId") Integer companyId);
    boolean existsByCompanyTel(@Param("companyTel") String companyTel,@Param("companyId") Integer companyId);
    boolean existsByCompanyFax(@Param("companyFax") String companyFax,@Param("companyId") Integer companyId);
    /**
	 * 指定されたIDのレコードがすでに存在しているか確認（重複チェック）。
	 */
	int countByCompanyId(@Param("excludeCompanyId") Integer excludeCompanyId);
	int countByCompanyIds(@Param("excludeCompanyIds") List<Integer> excludeCompanyIds);
}
