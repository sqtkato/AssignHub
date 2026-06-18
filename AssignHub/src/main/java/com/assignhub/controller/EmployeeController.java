package com.assignhub.controller;

import java.nio.charset.MalformedInputException;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.assignhub.entity.Employee;
import com.assignhub.form.EmployeeForm;
import com.assignhub.service.AccountService;
import com.assignhub.service.CompanyService;
import com.assignhub.service.EmployeeService;

import lombok.extern.slf4j.Slf4j;

/**
 * エンジニア（社員）管理機能の画面遷移およびHTTPリクエストを処理するコントローラー。
 *
 * @version 1.03 2026/06/01
 * @author SQT）チームB
 */
@Slf4j
@Controller
@RequestMapping("/employees")
public class EmployeeController {

	private final EmployeeService employeeService;
	private final CompanyService companyService;
	private final AccountService accountService;

	/**
	 * コンストラクタによる依存性の注入。
	 *
	 * @param employeeService 社員サービス
	 */
	public EmployeeController(EmployeeService employeeService,AccountService accountService,
			CompanyService companyService) {
		this.employeeService = employeeService;
		this.accountService = accountService;
		this.companyService = companyService;

	}

	/**
	 * 社員一覧画面を表示する。検索・ソート条件に応じたデータを取得する。
	 *
	 * @param model 画面描画用モデル
	 * @return 一覧画面のテンプレートパス
	 */
	@GetMapping
	public String index(@RequestParam(name = "empName", required = false) String empName,
			@RequestParam(name = "empAssignCompany", required = false) String empAssignCompany,
			@RequestParam(name = "empEngineerType", required = false) String empEngineerType,
			@RequestParam(name = "empCompany", required = false) String empCompany,
			Model model) {
		model.addAttribute("employees",
				employeeService.findAll(empName, empAssignCompany, empEngineerType, empCompany));
		model.addAttribute("empName", empName);
		model.addAttribute("empAssignCompany", empAssignCompany);
		model.addAttribute("empEngineerType", empEngineerType);
		model.addAttribute("empCompany", empCompany);
		return "employee/index";
	}

	/**
	 * 社員の新規登録画面を表示する。
	 *
	 * @param model 画面描画用モデル
	 * @return 新規登録画面のテンプレートパス
	 */
	@GetMapping("/new")
	public String create(Model model, RedirectAttributes attributes) {
		if (employeeService.isMaxCount()) {
			attributes.addFlashAttribute("toastError", "登録件数が上限(500件)に達しているため登録できません。");
			return "redirect:/employees";
		}
		model.addAttribute("employeeForm", new EmployeeForm());
		model.addAttribute("companies", companyService.findAll(null, null, null));
		model.addAttribute("accounts", accountService.findLoginId());
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
			RedirectAttributes attributes, Model model) {

		if ("プロパー".equals(form.getEngineerType()) && form.getAccountId() == null) {
			result.rejectValue("accountId", "error.employeeForm", "ログインIDは必須です");
		}
		if ("パートナー".equals(form.getEngineerType()) && form.getCompanyId() == null) {
			result.rejectValue("companyId", "error.employeeForm", "所属企業は必須です");
		}

		if (result.hasErrors()) {
			// 新規登録画面（create）を開いたときと同じように、コンボボックスのリストを再セットする
			model.addAttribute("companies", companyService.findAll(null, null, null));
			model.addAttribute("accounts", accountService.findLoginId());

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
		Employee emp = employeeService.findById(id);
		model.addAttribute("employee", emp);

		return "employee/detail";
	}

	@GetMapping("/{id}/edit")
	public String edit(@PathVariable("id") Integer id,
			@RequestParam(value = "from", required = false) String from,
			Model model) {
		if (!model.containsAttribute("employeeForm")) {
			Employee emp = employeeService.findById(id);
			model.addAttribute("employee", emp);
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
			form.setCompanyId(emp.getCompanyId());
			form.setAccountId(emp.getAccountId());
			form.setDepartment(emp.getDepartment());
			form.setJobTitle(emp.getJobTitle());
			model.addAttribute("employeeForm", form);
			model.addAttribute("fromPage", from);
		}
		model.addAttribute("companies", companyService.findAll(null, "emp_company_name", "asc"));
		model.addAttribute("accounts", accountService.findLoginId());
		return "employee/edit";
	}

	@PostMapping("/{id}/edit")
	public String update(@PathVariable("id") Integer id,
			@Validated @ModelAttribute("employeeForm") EmployeeForm employeeForm,
			BindingResult result, RedirectAttributes attributes,
			@RequestParam(value = "fromPage", required = false) String fromPage, Model model) {

		if ("プロパー".equals(employeeForm.getEngineerType())) {
			employeeForm.setCompanyId(null);
		} else if ("パートナー".equals(employeeForm.getEngineerType())) {
			employeeForm.setAccountId(null); 
		}

		if ("プロパー".equals(employeeForm.getEngineerType()) && employeeForm.getAccountId() == null) {

			result.rejectValue("accountId", "error.employeeForm", "ログインIDは必須です");
		}
		if ("パートナー".equals(employeeForm.getEngineerType()) && employeeForm.getCompanyId() == null) {

			result.rejectValue("companyId", "error.employeeForm", "所属企業は必須です");
		}

		if (employeeService.isEmailDuplicate(employeeForm.getEmail(), id)) {
			result.rejectValue("email", "error.employeeForm", "このメールアドレスは既に使用されています");
		}

		if (result.hasErrors()) {
			model.addAttribute("fromPage", fromPage);
			model.addAttribute("companies", companyService.findAll(null, null, null));
			model.addAttribute("accounts", accountService.findLoginId());
			return "employee/edit";
		}
		

		Employee emp = new Employee();
		emp.setEmpId(id);
		copyFormToEntity(employeeForm, emp);
		employeeService.save(emp);
		attributes.addFlashAttribute("toastMessage", "社員情報を更新しました");

		if ("detail".equals(fromPage)) {
			return "redirect:/employees/" + id + "/detail";
		}
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
	public String showImport(Model model, RedirectAttributes attributes) {
		if (employeeService.isMaxCount()) {
			attributes.addFlashAttribute("toastError", "登録件数が上限(500件)に達しているため登録できません。");
			return "redirect:/employees";
		}
		return "employee/import";
	}

	/**
	 * CSVファイルを用いた社員情報の一括インポート処理を実行する。
	 *
	 * @param file  アップロードされたCSVファイル
	 * @param model 画面描画用のモデル
	 * @return インポート画面のテンプレートパス
	 */
	@PostMapping("/import")
	public String importCsv(
	        @RequestParam(name = "file", required = false) MultipartFile file,
	        Model model) {

	    if (file == null || file.isEmpty()) {
	        model.addAttribute("toastError", "ファイルを選択してください");
	        return "employee/import";
	    }

	    String filename = file.getOriginalFilename();
	    if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
	        model.addAttribute("toastError", "ファイルの形式が正しくありません。CSVファイルを選択してください");
	        return "employee/import";
	    }

	    if (file.getSize() > 5 * 1024 * 1024) {
	        model.addAttribute("toastError", "ファイルサイズは5MB以内にしてください");
	        return "employee/import";
	    }

	    try {
	        EmployeeService.ImportResult result = employeeService.importCsv(file);
	        model.addAttribute("importResult", result);
	        if (result.errorCount > 0) {
	            model.addAttribute("toastError", "一部の行でエラーが発生しました");
	        } else {
	            model.addAttribute("toastMessage", result.successCount + "件のインポート処理が完了しました");
	        }
	        return "employee/import";
	    } catch (MalformedInputException e) {
	        model.addAttribute("toastError", "UTF-8のCSVファイルを選択してください");
	        return "employee/import";
	    } catch (Exception e) {
	        model.addAttribute("toastError", "ファイルの読み込みに失敗しました");
	        return "employee/import";
	    }
	}

	/**
	 * インポート用のCSVテンプレートをダウンロードする。
	 *
	 * @return ダウンロード用のCSVファイルバイナリデータ
	 */
	@GetMapping("/import/template")
	public ResponseEntity<byte[]> downloadTemplate() {
		String csvContent = "社員ID,社員姓,社員名,社員姓カナ,社員名カナ,入社年月日,勤続年数,"
				+ "生年月日,郵便番号,住所1,住所2,エンジニアタイプ,ログインID,"
				+ "所属企業,所属部署,役職,電話番号,メールアドレス\n";
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
	 * @param ids   削除対象となる社員IDのリスト
	 * @param model  画面描画用のモデル
	 * @return エクスポート画面のテンプレートパス
	 */
	@PostMapping("/export")
	public String showExport(
			@RequestParam(name = "ids", required = false) List<Integer> ids,
			Model model, RedirectAttributes attributes) {

		if (ids == null || ids.isEmpty()) {
			attributes.addFlashAttribute("toastError", "エクスポートする対象が選択されていません");
			return "redirect:/employees";
		}
		List<Employee> employees = employeeService.findByIds(ids);
		model.addAttribute("employees", employees);
		model.addAttribute("count", employees.size());
		model.addAttribute("ids", ids);
		return "employee/export";
	}

	/**
	 * 検索条件に合致する社員データをCSV形式でダウンロードする。
	 *
	 * @return ダウンロード用のCSVファイルバイナリデータ
	 */
	@PostMapping("/export/download")
	public ResponseEntity<byte[]> downloadCsv(
			@RequestParam(name = "ids", required = false) List<Integer> ids) {
		List<Employee> employees = employeeService.findByIds(ids);
		StringBuilder csvBuilder = new StringBuilder(
				"社員ID,社員姓,社員名,社員姓カナ,社員名カナ,入社年月日,勤続年数,"
						+ "生年月日,郵便番号,住所1,住所2,エンジニアタイプ,ログインID,"
						+ "所属企業,所属部署,役職,電話番号,メールアドレス\n");
		for (Employee emp : employees) {
			csvBuilder.append(emp.getEmpId()).append(",")
					.append(emp.getLastName()).append(",")
					.append(emp.getFirstName()).append(",")
					.append(emp.getLastNameKana()).append(",")
					.append(emp.getFirstNameKana()).append(",")
					.append(emp.getHireDate() != null ? emp.getHireDate() : "").append(",")
					.append(emp.getYearsOfService() != null ? emp.getYearsOfService() : "").append(",")
					.append(emp.getBirthDate() != null ? emp.getBirthDate() : "").append(",")
					.append(emp.getZipCode()).append(",")
					.append(emp.getAddress1()).append(",")
					.append(emp.getAddress2() != null ? emp.getAddress2() : "").append(",")
					.append(emp.getEngineerType()).append(",")
					.append(emp.getAccount() != null && emp.getAccount().getLoginId() != null
							? emp.getAccount().getLoginId()
							: "")
					.append(",")
					.append(emp.getCompany() != null ? emp.getCompany().getCompanyName() : "").append(",")
					.append(emp.getDepartment() != null ? emp.getDepartment() : "").append(",")
					.append(emp.getJobTitle() != null ? emp.getJobTitle() : "").append(",")
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
		e.setAccountId(f.getAccountId());
		e.setCompanyId(f.getCompanyId());
		e.setDepartment(f.getDepartment());
		e.setJobTitle(f.getJobTitle());
	}
}