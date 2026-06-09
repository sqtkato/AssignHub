package com.assignhub.form;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
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
	@NotBlank(message = "企業IDは必須です")
	private Integer companyId;

	/** 企業名（必須、最大50文字） */
	@NotBlank(message = "企業名は必須です")
	@Size(max = 50, message = "企業名は50文字以内で入力してください")
	private String companyName;
	
	@NotBlank(message = "企業名カナは必須です")
	@Size(max = 100, message = "企業名カナは100文字以内で入力してください")
	private String compNameKana;
	
	@NotBlank(message = "郵便番号は必須です")
	@Size(max = 7, message = "郵便番号は7桁以内で入力してください")
	private String compZipCode;
	
	@NotBlank(message = "住所１は必須です")
	@Size(max = 100, message = "住所１は100文字以内で入力してください")
	private String compAddress1;
	
	@Size(max = 100, message = "住所２は100文字以内で入力してください")
	private String compAddress2;
	
	@NotBlank(message = "電話番号は必須です")
	@Size(max = 11, message = "電話番号10桁または11桁で入力してください")
	private String compTel;
	
	@Size(max = 20, message = "FAX番号は20文字以内で入力してください")
	private String fax;
	
	@Size(max = 4, message = "設立年度は4桁で入力してください")
	private Integer foundedYear;
	
	@Size(max = 5, message = "社員数は5桁以内で入力してください")
	private Integer employeeCount;
	
	@Size(max = 50, message = "代表者姓は50文字以内で入力してください")
	private String repLastName;
	
	@Size(max = 100, message = "代表者姓カナは100文字以内で入力してください")
	private String repLastNameKana;
	
	@Size(max = 50, message = "代表者名は50文字以内で入力してください")
	private String repFirstName;
	
	@Size(max = 100, message = "代表者名カナは100文字以内で入力してください")
	private String repFirstNameKana;
	
	private LocalDateTime createdAt;
	
	private LocalDateTime updatedAt;
}