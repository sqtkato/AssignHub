package com.assignhub.entity;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Company {
	/** 企業ID（主キー） */
	private Integer companyId;

	/** 企業名 */
	private String companyName;
	
	/** 企業名カナ */
	private String companyNameKana;
	
	/** 郵便番号 */
	private String companyZipCode;
	
	/** 住所1 */
	private String companyAddress1;
	
	/** 住所2 */
	private String companyAddress2;
	
	/** TEL */
	private String companyTel;
	
	/** FAX */
	private String companyFax;
	
	/** 設立年度 */
	private Integer foundedYear;
	
	/** 社員数 */
	private Integer employeeCount;
	
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
