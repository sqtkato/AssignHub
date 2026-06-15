package com.assignhub.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.assignhub.entity.Company;
import com.assignhub.form.CompanyForm;
import com.assignhub.service.CompanyService;

/**
 * 企業管理機能の画面遷移およびHTTPリクエストを処理するコントローラー。
 *
 * @version 1.03 2026/06/01
 * @author SQT）チームC
 */
@Controller
@RequestMapping("/companies")
public class CompanyController {

	private final CompanyService companyService;

	/**
	 * コンストラクタによる依存性の注入。
	 *
	 * @param companyService 企業サービス
	 */
	public CompanyController(CompanyService companyService) {
		this.companyService = companyService;
	}

	/**
	 * 企業一覧画面を表示する。検索・ソート条件に応じたデータを取得する。
	 *
	 * @param keyword 検索キーワード（任意）
	 * @param sort ソート対象のカラム名（デフォルト: company_id）
	 * @param order ソート順（デフォルト: asc）
	 * @param model 画面描画用モデル
	 * @return 一覧画面のテンプレートパス
	 */
	@GetMapping
	public String index(@RequestParam(name = "keyword", required = false) String keyword,
			@RequestParam(name = "sort", defaultValue = "company_id") String sort,
			@RequestParam(name = "order", defaultValue = "asc") String order, Model model) {
		model.addAttribute("companies", companyService.findAll(keyword, sort, order));
		model.addAttribute("keyward", keyword);
		model.addAttribute("currentSort", sort);
		model.addAttribute("currentOrder", order);
		return "company/index";
	}

	/**
	 * 企業の新規登録画面を表示する。
	 *
	 * @param model 画面描画用モデル
	 * @return 新規登録画面のテンプレートパス
	 */
	@GetMapping("/new")
	public String create(Model model) {
		if (!model.containsAttribute("companyForm")) {
			model.addAttribute("companyForm", new CompanyForm());
		}
		return "company/create";
	}

	/**
	 * 入力された企業情報をデータベースに登録する。
	 *
	 * @param form 入力フォームデータ
	 * @param result バリデーション結果
	 * @param attributes リダイレクト先へ渡すフラッシュスコープ
	 * @return 成功時は一覧画面へリダイレクト、失敗時は登録画面へ戻る
	 */
	@PostMapping
	public String store(@Validated @ModelAttribute("companyForm") CompanyForm form, BindingResult result,
			RedirectAttributes attributes) {
		if (result.hasErrors()) {
			return "company/create";
		}
		Company company = new Company();
		copyFormToEntity(form, company);
		companyService.save(company);
		attributes.addFlashAttribute("toastMessage", "企業情報を登録しました");
		return "redirect:/companies";
	}

	/**
	 * フォームオブジェクトからエンティティオブジェクトへ値の詰め替えを行う。
	 *
	 * @param f 入力フォーム
	 * @param e 更新対象のエンティティ
	 */
	private void copyFormToEntity(CompanyForm f, Company e) {
		e.setCompanyName(" ");
	}
}