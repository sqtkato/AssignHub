package com.assignhub.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

/**
 * 社員（エンジニア）登録・編集画面の入力値を受け取るフォームクラス。
 * 画面からの入力チェック（バリデーション）を担う。
 *
 * @version 1.02 2026/06/01
 * @author SQT）チームB
 */
@Data
public class EmployeeForm {

	/** 社員ID（更新処理の際に対象を特定するために使用、新規登録時はnull） */
	private Integer empId;

	/** 社員姓（必須、最大50文字） */
	@NotBlank(message = "社員姓は必須です")
	@Size(max = 50, message = "社員姓は50文字以内で入力してください")
	private String lastName;
	
	/** 社員名（必須、最大50文字） */
	@NotBlank(message = "社員名は必須です")
	@Size(max = 50, message = "社員名は50文字以内で入力してください")
	private String firstName;
	
	/** 社員姓カナ（必須、最大50文字） */
	@NotBlank(message = "社員姓カナは必須です")
	@Size(max = 50, message = "社員姓カナは50文字以内で入力してください")
	private String lastNameKana;
	
	/** 社員名カナ（必須、最大50文字） */
	@NotBlank(message = "社員名カナは必須です")
	@Size(max = 50, message = "社員名カナは50文字以内で入力してください")
	private String firstNameKana;

	/** 生年月日 */
	@DateTimeFormat(pattern="YYYY-MM-DD")
	
	private String birthDate;
	
	/** 入社年月 */
	@DateTimeFormat(pattern="YYYY-MM-DD")
	
	private String hireDate;
	
	/**勤続年数任意、最大200文字） */
	@Max(value = 3, message = "勤続年数は3桁以内で入力してください")
	private Integer yearsOfService;
	
	/**郵便番号任意、最大200文字） */
	@NotBlank(message = "郵便番号は必須です")
    @Pattern(regexp = "^[0-9]{7}$", message = "郵便番号の形式が正しくありませんハイフンなしで入力してください")
	@Size(max = 7, message = "郵便番号は7桁以内で入力してください")
	private String zipCode;

	/**住所１任意、最大200文字） */
	@NotBlank(message = "住所1は必須です")
	@Size(max = 100, message ="住所1は100文字以内で入力してください")
	private String Address1 ;
	
	/**住所２任意、最大200文字） */
	@Size(max = 100, message = "住所2は100文字以内で入力してください")
	private String Address2;
	
	/**電話番号任意、最大200文字） */
	 @NotBlank(message = "電話番号は必須です")
	 @Size(max =11, message = "電話番号は10桁または11桁で入力してください")
	    // 半角数字の10桁または11桁のみを許容する正規表現
	    @Pattern(regexp = "^0[0-9]{9,10}$", message = "電話番号の形式が正しくありませんハイフンなしで入力してください")
	
	private String empTel;
	
	/**メールアドレス任意、最大200文字） */
	 @NotBlank(message = "メールアドレスは必須です")

	@Size(max = 255, message = "メールアドレスは255文字以内で入力してください")
	 @Email(message = "メールアドレスの形式が正しくありません＠を含めて入力してください")
	private String Email;
	
	/**エンジニアタイプ */
	@NotBlank(message = "エンジニアタイプは必須です")
	 private String EngineerType;

	/**所属企業 */
	@NotBlank(message = "所属企業は必須です")
	 private String CompanyName;
	/**ログインID */
	@NotNull(message = "ログインIDは必須です")
	 private Integer AccountId;
	
	/**所属部署任意、最大200文字） */
	@Size(max = 100, message = "所属部署は100文字以内で入力してください")
	private String department;
	
	/**役職任意、最大200文字） */
	@Size(max = 100, message = "役職は100文字以内で入力してください")
	private String jobTitle;

}