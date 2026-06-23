package com.assignhub.form; 

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;


/**
 * アカウント情報の新規登録・編集画面の入力値を受け取るフォームクラス。
 * 画面からの入力チェック（バリデーション）を担う。
 *  
 * @version 1.00 2026/06/18
 * @author チームポケットモンスター
 */
@Data
public class AccountForm {

	/** ログインID（必須、最小5文字 最大12文字 半角英数のみ） */
    @NotBlank(message = "ログインIDは必須です")
    @Size(min = 5, max = 12, message = "ログインIDは5文字以上12文字以内で入力してください")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "ログインIDは半角英数字のみで入力してください")
    private String loginId;

    /** パスワード（必須、最小8文字 最大20文字 半角英数と一部記号のみ） */
    @NotBlank(message = "パスワードは必須です")
    @Size(min = 8, max = 20, message = "パスワードは8文字以上20文字以内で入力してください")
    @Pattern(regexp = "^[a-zA-Z0-9@_]+$", message = "パスワードは半角英数字または記号(\"@\",\"_\")のみで入力してください")
    private String passwordHash;

    /** 権限（一般か管理で選択。新規登録時、初期値は一般） */
    private Integer permission;

    /** アカウントID（更新処理の際に対象を特定するために使用、新規登録時はnull。自動で割り振り） */
    private Integer accountId;
}
