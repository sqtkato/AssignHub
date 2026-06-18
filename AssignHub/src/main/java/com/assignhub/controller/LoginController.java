package com.assignhub.controller;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.assignhub.entity.Account;
import com.assignhub.form.LoginForm;
import com.assignhub.service.LoginService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

/**
 * ログイン情報管理機能の画面遷移およびHTTPリクエストを処理するコントローラー。
 *
 * @version 1.03 2026/06/18
 * @author チームポケットモンスター
 */
@Controller
public class LoginController {

	private final LoginService loginService;

	/**
	 * コンストラクタによる依存性の注入。
	 *
	 * @param loginService ログインサービス
	 */
	public LoginController(LoginService loginService) {
		this.loginService = loginService;
	}

	/**
	 * バインダーの設定。
	 *
	 * @param binder バインダーの設定
	 */
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
	}

	/**
	 * ログイン画面を表示する。
	 * 
	 * @param model 画面描画用モデル
	 * @return ログイン画面のテンプレートパス
	 */
	@GetMapping("/login")
	public String showLogin(Model model) {
		model.addAttribute("loginForm", new LoginForm());
		return "login";
	}

	/**
	 * ログイン認証処理を実行する。
	 * 
	 * @param loginForm		入力されたログイン情報フォーム
	 * @param bindingResult バリデーション結果
	 * @param session		ログインしたアカウント情報を保持するHTTPセッション
	 * @param model			画面描画用のモデル
	 * @return
	 */
	@PostMapping("/login")
	public String login(@Valid @ModelAttribute("loginForm") LoginForm loginForm,
			BindingResult bindingResult,
			HttpSession session,
			Model model) {
		if (bindingResult.hasErrors()) {
			return "login";
		}
		Account account = loginService.authenticate(loginForm.getLoginId(), loginForm.getPassword());
		if (account == null) {
			model.addAttribute("loginError", "ログインIDまたはパスワードに誤りがあります");
			return "login";
		}
		session.setAttribute("loginId", account.getLoginId());
		session.setAttribute("permission", account.getPermission());
		return "redirect:/employees";
	}

	/**
	 * ログアウト処理を実行する。
	 * 
	 * @param session ログアウト時に破棄するログインしたアカウント情報を保持しているHTTPセッション
	 * @return ログイン画面のリダイレクトパス
	 */
	@PostMapping("/logout")
	public String logout(HttpSession session) {
		session.invalidate();
		return "redirect:/login";
	}
}