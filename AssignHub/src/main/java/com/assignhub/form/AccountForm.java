package com.assignhub.form; 

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern; // 追加
import jakarta.validation.constraints.Size;    // 追加

import lombok.Data;

@Data
public class AccountForm {

    @NotBlank(message = "ログインIDは必須です")
    @Size(min = 5, max = 12, message = "ログインIDは5文字以上12文字以内で入力してください")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "ログインIDは半角英数字のみで入力してください")
    private String loginId;

    @NotBlank(message = "パスワードは必須です")
    @Size(min = 8, max = 20, message = "パスワードは8文字以上20文字以内で入力してください")
    @Pattern(regexp = "^[a-zA-Z0-9@_]+$", message = "パスワードは半角英数字または記号(\"@\",\"_\")のみで入力してください")
    private String passwordHash;

    private Integer permission;
}
