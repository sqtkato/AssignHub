package com.assignhub.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.assignhub.entity.Company;
import com.assignhub.mapper.DeletedCompanyMapper;

@Service
public class DeletedCompanyService {
	@Autowired
    private DeletedCompanyMapper deletedCompanyMapper;

    /**
     * 論理削除済みの企業情報を全件取得する（一覧表示・検索用）。
     */
    public List<Company> deletedfindAll(String companyName, String companyTel) {
        return deletedCompanyMapper.deletedfindAll(companyName, companyTel);
    }

    /**
     * 選択された企業情報を取得する（CSVエクスポート用）。
     */
    public List<Company> deletedfindByIds(List<Integer> ids) {
        if (ids != null && !ids.isEmpty()) {
            return deletedCompanyMapper.deletedfindByIds(ids);
        }
        return null;
    }

    /**
     * 単一復元処理。
     */
    @Transactional
    public void restore(Integer id) {
        deletedCompanyMapper.restore(id);
    }

    /**
     * 一括復元処理。
     */
    @Transactional
    public void restoreBulk(List<Integer> ids) {
        if (ids != null && !ids.isEmpty()) {
            deletedCompanyMapper.restoreBulk(ids);
        }
    }

    /**
     * 単一物理削除処理。
     */
    @Transactional
    public void physicalDelete(Integer id) {
        deletedCompanyMapper.physicalDelete(id);
    }

    /**
     * 一括物理削除処理。
     */
    @Transactional
    public void physicalDeleteBulk(List<Integer> ids) {
        if (ids != null && !ids.isEmpty()) {
            deletedCompanyMapper.physicalDeleteBulk(ids);
        }
    }

    // =======================================================
    // Controllerからのチェック用メソッド（booleanを返す）
    // =======================================================

    /** * 単一企業に紐づく社員情報（派遣先 または パートナー所属元）が存在するか判定 
     */
    public boolean countEmployeesByCompanyId(Integer id) {
        return deletedCompanyMapper.countEmployeesByCompanyId(id) > 0;
    }

    /** * 複数企業の中に、紐づく社員情報が存在するものが含まれているか判定 
     */
    public boolean countEmployeesByCompanyIds(List<Integer> ids) {
        return deletedCompanyMapper.countEmployeesByCompanyIds(ids) > 0;
    }

    /** * 単一企業に紐づくアサイン履歴（現場）が存在するか判定 
     */
    public boolean countAssignmentsByCompanyId(Integer id) {
        return deletedCompanyMapper.countAssignmentsByCompanyId(id) > 0;
    }

    /** * 複数企業の中に、紐づくアサイン履歴が存在するものが含まれているか判定 
     */
    public boolean countAssignmentsByCompanyIds(List<Integer> ids) {
        return deletedCompanyMapper.countAssignmentsByCompanyIds(ids) > 0;
    }

}
