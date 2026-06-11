package com.assignhub.entity;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Employee {
	/** 社員ID（主キー） */
	private Integer empId;

	/** 社員姓 */
	private String lastName;

	/** 社員名 */
	private String firstName;
	
	/** 社員姓カナ */
	private String lastNameKana;
	
	/** 社員名カナ */
	private String firstNameKana;
	
	/** 入社年月日 */
	private String hireDate;
	
	/** 勤続年数 */
	private Integer yearsOfService;
	
	/** 生年月日 */
	private String BirthDate;
	
	/** 郵便番号 */
	private String zipCode;
	
	/** 住所1 */
	private String address1;
	
	/** 住所2 */
	private String address2;
	
	/** エンジニアタイプ */
	private String engineerType;
	
	/** アカウントID */
	private Integer accountId;
	
	/** 企業ID */
	private Integer companyId;
	
	/** 所属先企業 */
	private String companyName;
	
	/** アサイン先企業 */
	private String companyNameAssign;
	
	/** 所属部署 */
	private String department;
	
	/** 役職 */
	private String jobTitle;
	
	/** 電話番号 */
	private String empTel;
	
	/** メールアドレス */
	private String email;
	
	/** 削除フラグ */
	private Integer deleteFlg;
	
	/** 作成日時 */
	private LocalDateTime createdAt;

	/** 更新日時 */
	private LocalDateTime updatedAt;

}
