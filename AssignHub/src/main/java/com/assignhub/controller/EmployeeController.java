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

import com.assignhub.entity.Employee;
import com.assignhub.form.EmployeeForm;
import com.assignhub.service.EmployeeService;

/**
 * エンジニア（社員）管理機能の画面遷移およびHTTPリクエストを処理するコントローラー。
 *
 * @version 1.03 2026/06/01
 * @author SQT）チームB
 */
@Controller
@RequestMapping("/employees")
public class EmployeeController {

	private final EmployeeService employeeService;

	/**
	 * コンストラクタによる依存性の注入。
	 *
	 * @param employeeService 社員サービス
	 */
	public EmployeeController(EmployeeService employeeService) {
		this.employeeService = employeeService;
	}

	/**
	 * 社員一覧画面を表示する。検索・ソート条件に応じたデータを取得する。
	 *
	 * @param keyword 検索キーワード（任意）
	 * @param sort ソート対象のカラム名（デフォルト: emp_id）
	 * @param order ソート順（デフォルト: asc）
	 * @param model 画面描画用モデル
	 * @return 一覧画面のテンプレートパス
	 */
	@GetMapping
	public String index(@RequestParam(name = "keyword", required = false) String keyword,
			@RequestParam(name = "sort", defaultValue = "emp_id") String sort,
			@RequestParam(name = "order", defaultValue = "asc") String order, Model model) {
		model.addAttribute("employees", employeeService.findAll(keyword, null, null, null));
		model.addAttribute("keyward", keyword);
		model.addAttribute("currentSort", sort);
		model.addAttribute("currentOrder", order);
		return "employee/index";
	}

	/**
	 * 社員の新規登録画面を表示する。
	 *
	 * @param model 画面描画用モデル
	 * @return 新規登録画面のテンプレートパス
	 */
	@GetMapping("/new")
	public String create(Model model) {
		if (!model.containsAttribute("employeeForm")) {
			model.addAttribute("employeeForm", new EmployeeForm());
		}
		return "employee/create";
	}

	/**
	 * 入力された社員情報をデータベースに登録する。
	 *
	 * @param form 入力フォームデータ
	 * @param result バリデーション結果
	 * @param attributes リダイレクト先へ渡すフラッシュスコープ
	 * @return 成功時は一覧画面へリダイレクト、失敗時は登録画面へ戻る
	 */
	@PostMapping
	public String store(@Validated @ModelAttribute("employeeForm") EmployeeForm form, BindingResult result,
			RedirectAttributes attributes) {
		if (result.hasErrors()) {
			return "employee/create";
		}
		Employee employee = new Employee();
		copyFormToEntity(form, employee);
		employeeService.save(employee);
		attributes.addFlashAttribute("toastMessage", "社員情報を登録しました");
		return "redirect:/employees";
	}

	/**
	 * フォームオブジェクトからエンティティオブジェクトへ値の詰め替えを行う。
	 *
	 * @param f 入力フォーム
	 * @param e 更新対象のエンティティ
	 */
	private void copyFormToEntity(EmployeeForm f, Employee e) {
		e.setEmpName(f.getEmpName());
		e.setHireYear(20);
	}
}