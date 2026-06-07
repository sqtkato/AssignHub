package com.assignhub.entity;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 顧客・パートナー企業情報を保持するエンティティクラス。
 * @version 1.00 2026/06/01
 * @author SQT）チームC
 */
@Data
public class Company {
	/** 企業ID（主キー） */
	private Integer companyId;

	/** 企業名 */
	private String companyName;

	/** 作成日時 */
	private LocalDateTime createdAt;

	/** 更新日時 */
	private LocalDateTime updatedAt;
}