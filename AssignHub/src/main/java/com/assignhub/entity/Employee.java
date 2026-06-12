package com.assignhub.entity;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 社員（エンジニア）情報を保持するエンティティクラス。
 * データベースの m_employee テーブルに対応する。
 * * @version 1.01 2026/06/01
 * @author SQT）チームB
 */
@Data
public class Employee {
	/** 社員ID（主キー） */
	private Integer empId;

	/** 社員名 */
	private String empName;
	
	private String empNameKana;
	
	
	private String engineerType;

	/** 入社年 */
	private Integer hireYear;

	/** 作成日時 */
	private LocalDateTime createdAt;

	/** 更新日時 */
	private LocalDateTime updatedAt;
	
	private Company company;
	
}