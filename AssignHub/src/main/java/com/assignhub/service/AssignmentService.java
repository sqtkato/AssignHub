package com.assignhub.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.assignhub.entity.Assignment;
import com.assignhub.entity.SelectOption;
import com.assignhub.mapper.AssignmentMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * AssignmentServiceは、アサイン情報の管理に関するビジネスロジックを実装するサービスクラスです。
 * このクラスは、AssignmentMapperを介してデータベース操作を行い、アサイン情報の一覧取得、新規登録などの機能を提供します。
 * 
 * @author Team Excel
 * @version 1.00 2024/06/04
 */
@Slf4j
@Service
public class AssignmentService {
	private final AssignmentMapper assignmentMapper;
	
	/**
	 * コンストラクタによる依存性の注入。
	 *
	 * @param assignmentMapper アサイン情報に対するマッパー
	 */
    public AssignmentService(AssignmentMapper assignmentMapper) {
        this.assignmentMapper = assignmentMapper;
    }

    /**
	 * アサイン情報の一覧を取得する。
	 *
	 * @param txtEmpName 社員名の検索キーワード（nullまたは空文字の場合は全件取得）
	 * @param txtAssignName アサイン先企業名の検索キーワード（nullまたは空文字の場合は全件取得）
	 * @param txtCompanyName 所属企業名の検索キーワード（nullまたは空文字の場合は全件取得）
	 * @return アサイン情報のリスト
	 */
    public List<Assignment> findAll(String txtEmpName, String txtAssignName, String txtCompanyName,
    		String txtContractStartDate, String txtContractEndDate) {
        return assignmentMapper.findAll(txtEmpName, txtAssignName, txtCompanyName, txtContractStartDate, txtContractEndDate);
    }

    public List<SelectOption> findEmployeeOptions() {
        return assignmentMapper.findEmployeeOptions();
    }

    public List<SelectOption> findCompanyOptions() {
        return assignmentMapper.findCompanyOptions();
    }

    public List<SelectOption> findRoleOptions() {
        return assignmentMapper.findRoleOptions();
    }
    
    /**
	 * アサイン情報をIDで取得する。
	 *
	 * @param id アサイン情報のID
	 * @return IDに対応するアサイン情報、存在しない場合はnull
	 */
    public Assignment findById(Integer id) {
        return assignmentMapper.findById(id);
    }
    
    /**
	 * アサイン情報を保存する。
	 * IDが存在しない場合（nullまたは0）は新規登録（INSERT）、存在する場合は更新（UPDATE）を行う。
	 *
	 * @param assignment 登録または更新するアサインエンティティ
	 */
    @Transactional
    public void save(Assignment assignment) {
    	if (assignment.getAssignmentId() == null || assignment.getAssignmentId() == 0) {
    		assignmentMapper.insert(assignment);
		} else {
			assignmentMapper.update(assignment);
		}
    }
    
    public void deleteById(Integer id) {
		assignmentMapper.delete(id);
	}
    
    public void deleteBulk(List<Integer> ids) {
    	assignmentMapper.deleteBulk(ids);
    }
    
    /**
     * アサイン情報の重複をチェックする。
     * @param assignment チェック対象のアサインエンティティ
     * @return
     */
	public boolean existsDuplicate(Assignment assignment) {
    	Assignment duplicate = assignmentMapper.findDuplicate(
            assignment.getAssignmentId(),
            assignment.getEmpId(),
            assignment.getCompanyId(),
            assignment.getContractStartDate(),
            assignment.getContractEndDate()
    	);

    	return duplicate != null;
	}
	
	/**
	 * アサイン情報の最大件数をチェックする。
	 * @return アサイン情報の件数が500件以上の場合はtrue、そうでない場合はfalse
	 */
	public boolean isMaxCount() {
	    return assignmentMapper.countActive() >= 500;
	}
}
