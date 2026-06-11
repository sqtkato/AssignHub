package com.assignhub.form;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DeletedCompanyForm {
	/** 企業ID（更新処理の際に対象を特定するために使用、新規登録時はnull） */
	private Integer companyId;

	/** 企業名（必須、最大50文字） */
	@NotBlank(message = "企業名は必須です")
	@Size(max = 50, message = "企業名は50文字以内で入力してください")
	private String companyName;
	
	@NotBlank(message = "企業名カナは必須です")
	@Size(max = 100, message = "企業名カナは100文字以内で入力してください")
	private String companyNameKana;
	
	@NotBlank(message = "郵便番号は必須です")
	private String companyZipCode;
	
	@NotBlank(message = "住所１は必須です")
	@Size(max = 100, message = "住所１は100文字以内で入力してください")
	private String companyAddress1;
	
	@Size(max = 100, message = "住所２は100文字以内で入力してください")
	private String companyAddress2;
	
	@NotBlank(message = "電話番号は必須です")
	private String companyTel;
	
	@Size(max = 20, message = "FAX番号は20文字以内で入力してください")
	private String fax;
	
	@Digits(integer = 4, fraction = 0, message = "設立年度は4桁で入力してください")
	private Integer foundedYear;
	
	
	@Digits(integer = 5, fraction = 0, message = "社員数は5桁以内で入力してください")
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
