package com.assignhub.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 登录表单类。
 * 用来接收登录画面输入的「登录ID」和「密码」，
 * 并通过注解(annotation)对输入内容做校验(validation)。
 */
public class LoginForm {

    // ===== 登录ID =====
    @NotBlank(message = "ログインIDは必須です")                       // 不能为空(未输入时报错)
    @Size(min = 5, max = 12,                                          // 长度限制:5~12个字符
          message = "ログインIDは5文字以上12文字以内で入力してください")
    @Pattern(regexp = "^[A-Za-z0-9]+$",                              // 只允许半角英文字母和数字
             message = "ログインIDは半角英数字のみで入力してください")
    private String loginId;

    // ===== 密码 =====
    @NotBlank(message = "パスワードは必須です")                       // 不能为空
    @Size(min = 8, max = 20,                                          // 长度限制:8~20个字符
          message = "パスワードは8文字以上20文字以内で入力してください")
    @Pattern(regexp = "^[A-Za-z0-9@_]+$",                            // 只允许半角英数字 + 符号 @ 和 _
             message = "パスワードは半角英数字または記号(\"@\",\"_\")のみで入力してください")
    private String password;

    // ===== getter / setter(用来读写字段的值,Java 惯例)=====
    public String getLoginId() {
        return loginId;
    }
    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}