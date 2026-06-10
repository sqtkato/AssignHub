package com.assignhub.form;

import jakarta.validation.constraints.Digits;
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

	/** 企業名（必須、最大255文字） */
	@NotBlank(message = "企業名は必須です")
	@Size(max = 50, message = "企業名は255文字以内で入力してください")
	private String companyName;
	
	/** 企業名カナ（必須、最大100文字） */
    @NotBlank(message = "企業名カナは必須です。")
	@Size(max = 100, message = "企業名カナは100文字以内で入力してください")
    private String compNameKana;

    /** 設立年度（最大4文字） */
    @Digits(integer = 4,fraction = 0, message = "設立年度は４桁内で入力してください")
    private Integer foundedYear;
    
    /** 社員数（最大５文字） */
    @Digits(integer = 5,fraction = 0, message = "社員数は5文字内で入力してください")
    private Integer employeeCount;

    /** 郵便番号（必須） */
    @NotBlank(message = "郵便番号は必須です")
    private String compZipCode;

    /** 住所1（必須、最大100文字） */
    @NotBlank(message = "住所1は必須です")
    @Size(max = 100, message = "住所1は100文字内で入力してください")
    private String compAddress1;
    
    /** 住所2（最大100文字） */
    @Size(max = 100, message = "住所2は100文字内で入力してください")
    private String compAddress2;

    
    /** Tel（必須） */
    @NotBlank(message = "電話番号は必須です")
    private String compTel;

    /** Fax（必須） */
    @Pattern(regexp = "^[0-9]{0,20}$", message = "FAXの形式が正しくありません ハイフンなしで入力してください")
    private String fax;
    
    /** 代表者姓（最大50文字） */
    @Size(max = 50, message = "は50文字内で入力してください")
    private String repLastName;
    
    /** 代表者名（必須、最大50文字） */
    @Size(max = 50, message = "は50文字内で入力してください")
    private String repFirstName;
    
    /** 代表者姓カナ（最大100文字） */
    @Size(max = 100, message = "は100文字内で入力してください")
    private String repLastNameKana;
    
    /** 代表者名カナ（最大100文字） */
    @Size(max = 100, message = "は100文字内で入力してください")
    private String repFirstNameKana;
}