package com.assignhub.form;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

	/** 社員名（必須、最大50文字） */
	@NotBlank(message = "社員名は必須です")
	@Size(max = 50, message = "社員名は50文字以内で入力してください")
	private String empName;

	/** 入社年（必須、1900年〜2100年の範囲を許可） */
	@NotNull(message = "入社年は必須です")
	@Min(value = 1900, message = "入社年が不正です")
	@Max(value = 2100, message = "入社年が不正です")
	private Integer hireYear;

}