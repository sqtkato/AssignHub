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
	private Integer compId;

	/** 企業名 */
	private String compName;
	
	/** 企業名カナ */
	private String compNameKana;
	
	/** 郵便番号 */
	private String compZipCode;
	
	/** 住所1 */
	private String compAddress1;
	
	/** 住所2 */
	private String compAddress2;
	
	/** TEL */
	private String compTel;
	
	/** FAX */
	private String fax;
	
	/** 設立年度 */
	private Integer foundedYear;
	
	/** 社員数 */
	private Integer empCount;
	
	/** 代表者姓 */
	private String repLastName;
	
	/** 代表者姓カナ */
	private String repLastNameKana;
	
	/** 代表者名 */
	private String repFirstName;
	
	/** 代表者名カナ */
	private String repFirstNameKana;

	/** 作成日時 */
	private LocalDateTime createdAt;

	/** 更新日時 */
	private LocalDateTime updatedAt;
	
	/** 削除フラグ*/
	private Integer deleteFlg;
	
}