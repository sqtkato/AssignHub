package com.assignhub.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AssignmentMapper {
	/**
     * アサイン情報を論理削除する。
     * @param id
     */
    void deleteByCompanyId(@Param("id") Integer id);
    
    void deleteBulkByCompanyId(@Param("ids") List<Integer> ids);
    
}
