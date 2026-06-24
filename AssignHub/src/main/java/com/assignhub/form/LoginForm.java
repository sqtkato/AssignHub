package com.assignhub.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Data;

/**
 * ログイン情報の入力値を受け取るフォームクラス。
 * 画面からの入力チェック（バリデーション）を担う。
 *  
 * @version 1.00 2026/06/18
 * @author チームポケットモンスター
 */
@Data
public class LoginForm {
    
	/** ログインID（必須、最小5文字 最大12文字 半角英数のみ） */
    @NotBlank(message = "ログインIDは必須です")                       
    @Size(min = 5, max = 12,                                          
          message = "ログインIDは5文字以上12文字以内で入力してください")
    @Pattern(regexp = "^[A-Za-z0-9]+$",                              
             message = "ログインIDは半角英数字のみで入力してください")
    private String loginId;

    /** パスワード（必須、最小8文字 最大20文字 半角英数と一部記号のみ） */
    @NotBlank(message = "パスワードは必須です")                      
    @Size(min = 8, max = 20,                                        
          message = "パスワードは8文字以上20文字以内で入力してください")
    @Pattern(regexp = "^[A-Za-z0-9@_]+$",                        
             message = "パスワードは半角英数字または記号(\"@\",\"_\")のみで入力してください")
    private String password;
}




   
