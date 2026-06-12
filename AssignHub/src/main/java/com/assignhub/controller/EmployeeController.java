package com.assignhub.controller;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
	public String index(@RequestParam(name = "txt_emp_name", required = false) String txt_emp_name,
			@RequestParam(name = "txt_emp_assign_company", required = false) String txt_emp_assign_company,
			@RequestParam(name = "cmb_emp_engineer_type", required = false) String cmb_emp_engineer_type,
			@RequestParam(name = "txt_emp_company", required = false) String txt_emp_company,
			Model model) {
        model.addAttribute("employees", employeeService.findAll(txt_emp_name, txt_emp_assign_company, cmb_emp_engineer_type, txt_emp_company));
        model.addAttribute("txt_emp_name", txt_emp_name);
        model.addAttribute("txt_emp_assign_company", txt_emp_assign_company);
		model.addAttribute("cmb_emp_engineer_type", cmb_emp_engineer_type);
		model.addAttribute("txt_emp_company", txt_emp_company);
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
	 * 社員の社員詳細画面を表示する。
	 *
	 * @param id    社員一覧で詳細選択した社員ID
	 * @param model 画面描画用のモデル
	 * @return 社員情報詳細画面のテンプレートパス
	 */
	@GetMapping("{id}/detail")
	public String detail(@PathVariable("id") Integer id, Model model) {
	    // 1. 社員情報を取得（アサイン情報、部署情報も一緒にロード）
	    Employee emp = employeeService.findById(id);
	    model.addAttribute("emp", emp);
	    
	    return "employee/detail";
	}
	
	@GetMapping("/{id}/edit")
	public String edit(@PathVariable("id") Integer id, Model model) {
		if (!model.containsAttribute("employeeForm")) {
			Employee emp = employeeService.findById(id);
			EmployeeForm form = new EmployeeForm();
			form.setLastName(emp.getLastName());
			form.setFirstName(emp.getFirstName());
			form.setLastNameKana(emp.getLastNameKana());
			form.setFirstNameKana(emp.getFirstNameKana());
			form.setBirthDate(emp.getBirthDate());
			form.setHireDate(emp.getHireDate());
			form.setYearsOfService(emp.getYearsOfService());
			form.setZipCode(emp.getZipCode());
			form.setAddress1(emp.getAddress1());
			form.setAddress2(emp.getAddress2());
			form.setEmpTel(emp.getEmpTel());
			form.setEmail(emp.getEmail());
			form.setEngineerType(emp.getEngineerType());
			form.setCompanyName(emp.getCompanyName());
			form.setAccountId(emp.getAccountId());
			form.setDepartment(emp.getDepartment());
			form.setJobTitle(emp.getJobTitle());
			model.addAttribute("employeeForm", form);
		}
//		model.addAttribute("company", companyService.findAll(null, "emp_company_name", "asc"));
		return "employee/edit";
	}
	
	
	@PostMapping("/{id}/edit")
	public String update(@PathVariable("id") Integer id,
			@Validated @ModelAttribute("employeeForm") EmployeeForm employeeForm,
			BindingResult result, RedirectAttributes attributes, Model model) {

		if (employeeService.isEmailDuplicate(employeeForm.getEmail(), id)) {
			result.rejectValue("email", "error.employeeForm", "このメールアドレスはすでに他の社員に使用されています");
		}

		if (result.hasErrors()) {
//			model.addAttribute("departments", companyService.findAll(null, "dept_id", "asc"));
			return "employee/edit";
		}

		Employee emp = new Employee();
		emp.setEmpId(id);
		copyFormToEntity(employeeForm, emp);
		employeeService.save(emp);
		attributes.addFlashAttribute("toastMessage", "社員情報を更新しました");
		return "redirect:/employees";
	}

	
	/**
	 * 社員情報を1件論理削除する。
	 *
	 * @param id    社員一覧で詳細選択した社員ID
	 * @param model 画面描画用のモデル
	 * @return 一覧画面へのリダイレクト
	 */
	@PostMapping("/{id}/delete")
	public String delete(@PathVariable("id") Integer id, RedirectAttributes attributes) {
		employeeService.delete(id);
		attributes.addFlashAttribute("toastMessage", "社員情報を削除しました");
		return "redirect:/employees";
	}
	
	/**
	 * 選択された複数の社員情報を一括で物理削除する。
	 *
	 * @param ids   削除対象となる社員IDのリスト
	 * @param attributes リダイレクト時に引き継ぐための属性
	 * @return 一覧画面へのリダイレクト
	 */
	@PostMapping("/bulk-delete")
	public String bulkDelete(@RequestParam(name = "ids", required = false) List<Integer> ids,
			RedirectAttributes attributes) {
		if (ids == null || ids.isEmpty()) {
			attributes.addFlashAttribute("toastError", "削除する対象が選択されていません");
			return "redirect:/employees";
		}
		employeeService.deleteBulk(ids);
		attributes.addFlashAttribute("toastMessage", ids.size() + "件の社員情報を削除しました");
		return "redirect:/employees";
	}
	
	/**
	 * 社員データのインポート画面を表示する。
	 *
	 * @return インポート画面のテンプレートパス
	 */
	@GetMapping("/import")
	public String showImport() {
		return "employee/import";
	}
	
	/**
	 * インポート用のCSVテンプレートをダウンロードする。
	 *
	 * @return ダウンロード用のCSVファイルバイナリデータ
	 */
	@GetMapping("/import/template")
	public ResponseEntity<byte[]> downloadTemplate() {
		String csvContent = "\"社員ID,社員姓,社員名,社員姓カナ,社員名カナ,入社年月日,勤続年数,\"\n"
				+ "				+ \"生年月日,郵便番号,住所1,住所2,エンジニアタイプ,ログインID,\"\n"
				+ "				+ \"所属企業,所属部署,役職,電話番号,メールアドレス\n";
		byte[] csvBytes = csvContent.getBytes(StandardCharsets.UTF_8);
		byte[] bom = new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
		byte[] result = new byte[bom.length + csvBytes.length];
		System.arraycopy(bom, 0, result, 0, bom.length);
		System.arraycopy(csvBytes, 0, result, bom.length, csvBytes.length);

		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "attachment; filename=employee_template.csv");
		headers.add("Content-Type", "text/csv; charset=UTF-8");
		return new ResponseEntity<>(result, headers, HttpStatus.OK);
	}
	
	/**
	 * 社員データのエクスポート画面を表示する。
	 *
	 * @param keyword     現在の検索キーワード（状態保持用）
	 * @param model  画面描画用のモデル
	 * @return エクスポート画面のテンプレートパス
	 */
	@GetMapping("/export")
	public String showExport(@RequestParam(name = "keyword", required = false) String keyword, Model model) {
	    	model.addAttribute("employees", employeeService.findAll(keyword , null , null,null));
	    model.addAttribute("count", employeeService.findAll(null,null , null,null).size());
	    model.addAttribute("keyword", keyword);
	    return "employee/export";
	}
		
	/**
	 * 検索条件に合致する社員データをCSV形式でダウンロードする。
	 *
	 * @return ダウンロード用のCSVファイルバイナリデータ
	 */
	@GetMapping("/export/download")
	public ResponseEntity<byte[]> downloadCsv() {
		List<Employee> employees = employeeService.findAll(null , null , null,null);
		StringBuilder csvBuilder = new StringBuilder(
				"社員ID,社員姓,社員名,社員姓カナ,社員名カナ,入社年月日,勤続年数,"
				+ "生年月日,郵便番号,住所1,住所2,エンジニアタイプ,ログインID,"
				+ "所属企業,所属部署,役職,電話番号,メールアドレス\n"
				);
		for (Employee emp : employees) {
			csvBuilder.append(emp.getEmpId()).append(",")
					.append(emp.getLastName()).append(",")
					.append(emp.getFirstName()).append(",")
					.append(emp.getLastNameKana()).append(",")
					.append(emp.getFirstNameKana()).append(",")
					.append(emp.getHireDate()).append(",")
					.append(emp.getYearsOfService()).append(",")
					.append(emp.getBirthDate()).append(",")
					.append(emp.getZipCode()).append(",")
					.append(emp.getAddress1()).append(",")
					.append(emp.getAddress2()).append(",")
					.append(emp.getEngineerType()).append(",")
					.append(emp.getAccountId()).append(",")
					.append(emp.getCompanyName()).append(",")
					.append(emp.getDepartment()).append(",")
					.append(emp.getJobTitle()).append(",")
					.append(emp.getEmpTel()).append(",")
					.append(emp.getEmail()).append("\n");
		}
		byte[] csvBytes = csvBuilder.toString().getBytes(StandardCharsets.UTF_8);
		byte[] bom = new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
		byte[] result = new byte[bom.length + csvBytes.length];
		System.arraycopy(bom, 0, result, 0, bom.length);
		System.arraycopy(csvBytes, 0, result, bom.length, csvBytes.length);

		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "attachment; filename=employees.csv");
		headers.add("Content-Type", "text/csv; charset=UTF-8");
		return new ResponseEntity<>(result, headers, HttpStatus.OK);
	}

	/**
	 * フォームオブジェクトからエンティティオブジェクトへ値の詰め替えを行う。
	 *
	 * @param f 入力フォーム
	 * @param e 更新対象のエンティティ
	 */
	private void copyFormToEntity(EmployeeForm f, Employee e) {
		
		e.setLastName(f.getLastName());
		e.setFirstName(f.getFirstName());
		e.setLastNameKana(f.getLastNameKana());
		e.setFirstNameKana(f.getFirstNameKana());
		e.setBirthDate(f.getBirthDate());
		e.setHireDate(f.getHireDate());
		e.setYearsOfService(f.getYearsOfService());
		e.setZipCode(f.getZipCode());
		e.setAddress1(f.getAddress1());
		e.setAddress2(f.getAddress2());
		e.setEmpTel(f.getEmpTel());
		e.setEmail(f.getEmail());
		e.setEngineerType(f.getEngineerType());
		e.setCompanyName(f.getCompanyName());
		e.setAccountId(f.getAccountId());
		e.setDepartment(f.getDepartment());
		e.setJobTitle(f.getJobTitle());
	}
}