package com.assignhub.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.assignhub.entity.DeletedAccount;
import com.assignhub.mapper.DeletedAccountMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DeletedAccountService {

	private final DeletedAccountMapper deletedaccountMapper;

	public DeletedAccount(DeletedAccountMapper deletedaccountMapper)
	{
		this.deletedaccountMapper = deletedaccountMapper;
}
//	検索条件およびソート条件に合致するアカウント情報を全件取得する。
	public List< DeletedAccount> findAll(String keyword, Integer deptId, String sort, String order) {
		return deletedaccountMapper.findAll(keyword, deptId, sort, order);
	}
	
//	論理削除済みの一覧
	public List< DeletedAccount> findAll(String keyword, Integer deptId, String sort, String order) {
		return deletedaccountMapper.findAll(keyword, deptId, sort, order);
	}

//	 IDをアカウント指定して、アカウント情報を1件取得する。
	public  DeletedAccount findById(Integer id) {
		return deletedaccountMapper.findById(id);
	}

//	一つ復元
	@Transactional
	public void recover() {
		
	}

//	一つ削除
	@Transactional
	public void deleted() {
		
	}

//	一括削除
	@Transactional
	public void bulkDeleted() {
			
		}
	
//	一括復元
	@Transactional
	public void bulkRecover() {
			
		}

	
}