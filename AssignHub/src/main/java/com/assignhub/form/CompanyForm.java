package com.assignhub.form;

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
	private Integer companyId;

	/** 企業名（必須、最大255文字） */
	@NotBlank(message = "企業名は必須です")
	@Size(max = 255, message = "企業名は255文字以内で入力してください")
	private String companyName;
}