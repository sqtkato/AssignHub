package com.assignhub.entity;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * アカウント情報を保持するエンティティクラス。
 * @version 1.00 2026/06/01
 * @author SQT）チームC
 */
@Data

public class Account {
	
	/** アカウントID（主キー） */
	private Integer accountId;

	/** ログインID */
	private String loginId;
	
	private String passwordHash;
	
	/** 権限 */
	private String permission;
	
	/** 削除フラグ */
	private String deleteFlg;

	/** 作成日時 */
	private LocalDateTime createdAt;

	/** 更新日時 */
	private LocalDateTime updatedAt;
	
	/** 社員名　（外部DBから取得）*/
	private String empName;

}
