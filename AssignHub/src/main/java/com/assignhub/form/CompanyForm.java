package com.assignhub.form;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Data;
/**
 * 企業登録・編集画面の入力値を受け取るフォームクラス。
 * 画面からの入力チェック（バリデーション）を担う。
 *
 * @version 1.00 2026/06/01
 * @author SQT）チームC
 */
@Data
public class CompanyForm {

	/** 企業ID（更新処理の際に対象を特定するために使用、新規登録時はnull） */
	private Integer companyId;

	/** 企業名（必須、最大50文字） */
	@NotBlank(message = "企業名は必須です")
	@Size(max = 50, message = "企業名は50文字以内で入力してください")
	private String companyName;
	
	@NotBlank(message = "企業名カナは必須です")
	@Size(max = 100, message = "企業名カナは100文字以内で入力してください")
	@Pattern(regexp = "^[ァ-ヶー]*$", message = "全角カタカナで入力してください")
	private String companyNameKana;
	
	@NotBlank(message = "郵便番号は必須です")
	@Size(max = 7, message = "郵便番号は7桁以内で入力してください")
	@Pattern(regexp = "^[0-9]*$",message = "郵便番号の形式が正しくありません　ハイフンなしで入力してください")
	private String companyZipCode;
	
	@NotBlank(message = "住所１は必須です")
	@Size(max = 100, message = "住所１は100文字以内で入力してください")
	private String companyAddress1;
	
	@Size(max = 100, message = "住所２は100文字以内で入力してください")
	private String companyAddress2;
	
	@NotBlank(message = "電話番号は必須です")
	@Size(min =10,max = 11, message = "電話番号10桁または11桁で入力してください")
	@Pattern(regexp = "^[0-9]*$",message = "電話番号の形式が正しくありません　ハイフンなしで入力してください")
	private String companyTel;
	
	@Size(max = 20, message = "FAX番号は20文字以内で入力してください")
	@Pattern(regexp = "^[0-9]*$",message = "FAX番号の形式が正しくありません　ハイフンなしで入力してください")
	private String companyFax;
	
	@Size(min = 4, max = 4, message = "設立年度は4桁で入力してください")
	@Pattern(regexp = "^[0-9]*$", message = "設立年度は数字で入力してください")
	private String foundedYear;

	@Size(max = 5, message = "社員数は5桁以内で入力してください")
	@Pattern(regexp = "^[0-9]*$", message = "社員数は数字で入力してください")
	private String employeeCount;
	
	@Size(max = 50, message = "代表者姓は50文字以内で入力してください")
	private String repLastName;
	
	@Size(max = 100, message = "代表者姓カナは100文字以内で入力してください")
	@Pattern(regexp = "^[ァ-ヶー]*$", message = "全角カナで入力してください")
	private String repLastNameKana;
	
	@Size(max = 50, message = "代表者名は50文字以内で入力してください")
	private String repFirstName;
	
	@Size(max = 100, message = "代表者名カナは100文字以内で入力してください")
	@Pattern(regexp = "^[ァ-ヶー]*$", message = "全角カナで入力してください")
	private String repFirstNameKana;
	
	private LocalDateTime createdAt;
	
	private LocalDateTime updatedAt;
}