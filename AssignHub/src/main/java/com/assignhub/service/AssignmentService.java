package com.assignhub.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.assignhub.mapper.AssignmentMapper;

@Service
public class AssignmentService {
		private final AssignmentMapper assignmentMapper;

	/**
	* コンストラクタによる依存性の注入。
	*
	* @param employeeMapper 社員マスタに対するマッパー
	*/
	public AssignmentService(AssignmentMapper assignmentMapper) {
		this.assignmentMapper = assignmentMapper;
	}
	
	@Transactional
	public void deleteByCompanyId(Integer id) {
        assignmentMapper.deleteByCompanyId(id);
    }
	
	@Transactional
	public void deleteBulkByCompanyId(List<Integer> ids) {
		if (ids != null && !ids.isEmpty()) {
			assignmentMapper.deleteBulkByCompanyId(ids);
		}
	}
		
}
