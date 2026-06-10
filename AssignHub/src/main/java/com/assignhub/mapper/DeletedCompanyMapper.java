package com.assignhub.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.assignhub.entity.DeletedCompany;

@Mapper
public interface DeletedCompanyMapper {
	/**
     * 論理削除済みの企業情報を全件取得する。
     * 
     */
    List<DeletedCompany> deletedfindAll(
            @Param("keyword") String keyword, 
            @Param("tel") String tel,
            @Param("sort") String sort, 
            @Param("order") String order);
    
    List<DeletedCompany> deletedfindByIds(@Param("ids") List<Integer> ids);
    
    int restore(@Param("id") Integer id);
    
    int restoreBulk(@Param("ids") List<Integer> ids);
    
    int physicalDelete(@Param("id") Integer id);
    
    int physicalDeleteBulk(@Param("ids") List<Integer> ids);
    
    /**
     * 指定された企業IDを「派遣先」または「所属元」として使用している社員数をカウント。
     */
    int countEmployeesByCompanyId(@Param("id") Integer id);
    int countEmployeesByCompanyIds(@Param("ids") List<Integer> ids);
    
    /**
     * 指定された企業IDを使用しているアサイン履歴数をカウント。
     */
    int countAssignmentsByCompanyId(@Param("id") Integer id);
    int countAssignmentsByCompanyIds(@Param("ids") List<Integer> ids);

}
