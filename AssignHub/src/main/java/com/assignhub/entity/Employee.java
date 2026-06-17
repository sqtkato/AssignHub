package com.assignhub.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Employee {
	/** 社員ID（主キー） */
	private Integer empId;

	/** アカウントID（外部キー） */
	private Integer accountId;

	/** 企業ID （外部キー）*/
	private Integer companyId;

	/** 社員姓 */
	private String lastName;

	/** 社員名 */
	private String firstName;

	/** 社員姓カナ */
	private String lastNameKana;

	/** 社員名カナ */
	private String firstNameKana;

	/** 入社年月日 */
	private LocalDate hireDate;

	/** 勤続年数 */
	private Integer yearsOfService;

	/** 生年月日 */
	private LocalDate BirthDate;

	/** 郵便番号 */
	private String zipCode;

	/** 住所1 */
	private String address1;

	/** 住所2 */
	private String address2;

	/** エンジニアタイプ */
	private String engineerType;

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

	/** アサイン情報 （外部DBからの取得）*/
	private Assignment Assignment;

	/** アカウント情報 （外部DBからの取得）*/
	private Account Account;

	/** 企業情報 （外部DBからの取得）*/
	private Company Company;
}
