package com.assignhub.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
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
			@RequestParam(name = "tel", required = false) String tel,
			@RequestParam(name = "sort", defaultValue = "company_id") String sort,
			@RequestParam(name = "order", defaultValue = "asc") String order, Model model) {
		model.addAttribute("companies", companyService.findAll(keyword, sort, order));
		model.addAttribute("keyword", keyword);
		model.addAttribute("phone", tel);
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
	public String store(@Validated @ModelAttribute("companyForm") CompanyForm companyForm, BindingResult result,
			RedirectAttributes attributes) {
		if (companyService.isDuplicate(companyForm.getCompanyName(), null)) {
			result.rejectValue("companyName", "error.companyForm", "この企業名は既に登録されています");
		}
		
		//設立年度が来年度以上で入力された場合
		if (companyForm.getFoundedYear() != null) {
		    int currentYear = java.time.Year.now().getValue();
		    if (companyForm.getFoundedYear() > currentYear) {
		        result.rejectValue("foundedYear", "error", "設立年度は現在年度以前を入力してください");
		    }
		}
		
		//郵便番号の形式が不正の場合
		if (companyForm.getCompanyZipCode() != null && !companyForm.getCompanyZipCode().isEmpty()) {
		    if (!companyForm.getCompanyZipCode().matches("^[0-9]*$")) {
		        result.rejectValue("compZipCode", "error", "郵便番号の形式が正しくありません ハイフンなしで入力してください");
		//郵便番号が8桁以上入力された場合        
		    } else if (companyForm.getCompanyZipCode().length() != 7) {
		        result.rejectValue("compZipCode", "error", "郵便番号は7桁で入力してください");
		    }
		}
		
		//TELが重複している場合
		if (companyService.isTelDuplicate(companyForm.getCompanyTel(), null)) {
			result.rejectValue("compTel", "error.companyForm", "この電話番号は既に登録されています");
		}
		if (companyService.isFaxDuplicate(companyForm.getFax(), null)) {
			result.rejectValue("fax", "error.companyForm", "このFAX電話は既に登録されています");
		}
		// TELの形式が不正の場合
		if (companyForm.getCompanyTel() != null && !companyForm.getCompanyTel().isEmpty()) {
		    if (!companyForm.getCompanyTel().matches("^[0-9]*$")) {
		        result.rejectValue("compTel", "error", "電話番号の形式が正しくありません ハイフンなしで入力してください");
		//TELが9桁以下または12桁以上入力の場合
		    } else if (companyForm.getCompanyTel().length() < 10 || companyForm.getCompanyTel().length() > 11) {
		        result.rejectValue("compTel", "error", "電話番号は10桁または11桁内で入力してください");
		    }
		}
		if (result.hasErrors()) {
			return "company/create";
		}
		Company company = new Company();
		copyFormToEntity(companyForm, company);
		companyService.save(company);
		attributes.addFlashAttribute("toastMessage", "企業情報を登録しました");
		return "redirect:/companies";
	}
	
	/**
	 * 企業情報を1件物理削除する。
	 *
	 * @param id         削除対象の社員ID
	 * @param attributes リダイレクト時にメッセージを引き継ぐための属性
	 * @return 一覧画面へのリダイレクト
	 */
	@PostMapping("/{id}/delete")
	public String delete(@PathVariable("id") Integer id, RedirectAttributes attributes) {
		companyService.delete(id);
		attributes.addFlashAttribute("toastMessage", "企業情報を削除しました");
		return "redirect:/companies";
	}
	
	/**
	 * 選択された複数の社員情報を一括で物理削除する。
	 *
	 * @param ids        削除対象となる社員IDのリスト
	 * @param attributes リダイレクト時にメッセージを引き継ぐための属性
	 * @return 一覧画面へのリダイレクト
	 */
	@PostMapping("/bulk-delete")
	public String bulkDelete(@RequestParam(name = "ids", required = false) List<Integer> ids,
			RedirectAttributes attributes) {
		if (ids == null || ids.isEmpty()) {
			attributes.addFlashAttribute("toastError", "削除対象が選択されていません");
			return "redirect:/companies";
		}
		companyService.deleteBulk(ids);
		attributes.addFlashAttribute("toastMessage", ids.size() + "件の企業情報を削除しました");
		return "redirect:/companies";
	}
	
	/**
	 * 社員データのインポート画面を表示する。
	 *
	 * @return インポート画面のテンプレートパス
	 */
	@GetMapping("/import")
	public String showImport() {
		return "company/import";
	}
	
	

	/**
	 * フォームオブジェクトからエンティティオブジェクトへ値の詰め替えを行う。
	 *
	 * @param f 入力フォーム
	 * @param e 更新対象のエンティティ
	 */
	private void copyFormToEntity(CompanyForm f, Company e) {
		e.setCompanyName(f.getCompanyName());
	    e.setCompanyNameKana(f.getCompanyNameKana());
	    e.setCompanyZipCode(f.getCompanyZipCode());
	    e.setCompanyAddress1(f.getCompanyAddress1());
	    e.setCompanyAddress2(f.getCompanyAddress2());
	    e.setCompanyTel(f.getCompanyTel());
	    e.setFax(f.getFax());
	    e.setFoundedYear(f.getFoundedYear());
	    e.setEmployeeCount(f.getEmployeeCount());
	    e.setRepFirstName(f.getRepFirstName());
	    e.setRepLastName(f.getRepLastName());
	    e.setRepFirstNameKana(f.getRepFirstNameKana());
	    e.setRepLastNameKana(f.getRepLastNameKana());
	}
}