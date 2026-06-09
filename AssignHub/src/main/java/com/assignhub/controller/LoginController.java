package com.assignhub.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.assignhub.entity.Account;
import com.assignhub.form.LoginForm;
import com.assignhub.service.LoginService;

/**
 * 登录功能的控制器(Controller)。
 * 负责处理登录画面的显示、登录验证、以及登出。
 */
@Controller
public class LoginController {

    // 登录业务逻辑(密码比对等)交给 LoginService 处理
    private final LoginService loginService;

    // 构造器注入:Spring 启动时自动把 LoginService 传进来
    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    /**
     * 显示登录画面。
     * 访问 /login(GET)时,准备一个空的 LoginForm 给画面绑定,然后显示 login.html。
     */
    @GetMapping("/login")
    public String showLogin(Model model) {
        model.addAttribute("loginForm", new LoginForm());  // 空表单,供画面输入
        return "login";                                    // 对应 templates/login.html
    }

    /**
     * 处理登录提交。
     * 点「ログイン」按钮(POST /login)时执行。
     */
    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginForm") LoginForm loginForm, // @Valid:触发 LoginForm 里的校验
                        BindingResult bindingResult,   // 装校验结果(有没有错)
                        HttpSession session,            // 登录成功后存用户信息用
                        Model model) {

        // ① 输入校验失败(空、长度、格式不对)→ 退回登录页显示错误红字
        if (bindingResult.hasErrors()) {
            return "login";
        }

        // ② 校验通过 → 用输入的ID和密码去数据库验证账号
        Account account = loginService.authenticate(loginForm.getLoginId(), loginForm.getPassword());

        // ③ 账号不存在 或 密码不对 → 显示错误信息,退回登录页
        if (account == null) {
            model.addAttribute("loginError", "ログインIDまたはパスワードに誤りがあります");
            return "login";
        }

        // ④ 登录成功 → 把登录账号存进 session(会话),后续页面可识别已登录
        session.setAttribute("loginAccount", account);

        // ⑤ 跳转。设计书要求跳「社员信息一览(C00101 /employees)」,
        //    但该画面由其他人负责、暂未完成,所以临时跳「账户一览 /accounts」。
        return "redirect:/accounts";
    }

    /**
     * 登出处理。
     * 点登出(POST /logout)时,清空 session,跳回登录页。
     */
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();        // 销毁会话(登出)
        return "redirect:/login";    // 回到登录页
    }
}