package com.assignhub.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

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

@Controller
public class LoginController {

	private final LoginService loginService;

	public LoginController(LoginService loginService) {
		this.loginService = loginService;
	}

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
	}

	@GetMapping("/login")
	public String showLogin(Model model) {
		model.addAttribute("loginForm", new LoginForm());
		return "login";
	}

	@PostMapping("/accounts/login")
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

	@PostMapping("/logout")
	public String logout(HttpSession session) {
		session.invalidate();
		return "redirect:/login";
	}
}