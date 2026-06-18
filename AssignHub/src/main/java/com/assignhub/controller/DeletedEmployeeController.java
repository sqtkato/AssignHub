package com.assignhub.controller;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.assignhub.entity.Employee;
import com.assignhub.service.DeletedEmployeeService;

/**
 * 論理削除された社員情報の管理、復元、物理削除、エクスポートを処理するコントローラー。
 * @author Cit)土手内
 */
@Controller
@RequestMapping("/deleted-employees")
public class DeletedEmployeeController {

	private final DeletedEmployeeService deletedEmployeeService;

	/**
	 * コンストラクタによる依存性の注入。
	 *
	 * @param deletedEmployeeService 論理削除済み社員管理サービス
	 */
	public DeletedEmployeeController(DeletedEmployeeService deletedEmployeeService) {
		this.deletedEmployeeService = deletedEmployeeService;
	}

	/**
	 * 論理削除済み社員一覧画面を表示する。
	 *
	 * @param txt_emp_name_keyword 検索キーワード（社員名）
	 * @param txt_emp_assign_company_keyword 検索キーワード（アサイン先企業名）
	 * @param txt_emp_company_keyword 検索キーワード（所属企業名）
	 * @param cmb_engtineer_type_keyword 検索キーワード（エンジニアタイプ）
	 * @param model   画面描画用のモデル
	 * @return 一覧画面のテンプレートパス
	 */
	@GetMapping
	public String index(@RequestParam(name = "empName", required = false) String empName,
			@RequestParam(name = "empAssignCompany", required = false) String empAssignCompany,
			@RequestParam(name = "empEngineerType", required = false) String empEngineerType,
			@RequestParam(name = "empCompany", required = false) String empCompany,
			Model model) {
		model.addAttribute("employees",
				deletedEmployeeService.findAll(empName, empAssignCompany, empEngineerType, empCompany));
		model.addAttribute("empName", empName);
		model.addAttribute("empAssignCompany", empAssignCompany);
		model.addAttribute("empEngineerType", empEngineerType);
		model.addAttribute("empCompany", empCompany);
		return "deleted_employee/index";
	}

	/**
	 * 一件の論理削除済み社員情報を復元する。
	 *
	 * @param id 復元する社員情報のID
	 * @param attributes リダイレクト時にメッセージを引き継ぐための属性
	 * @return 一覧画面へのリダイレクト
	 */
	@PostMapping("/{id}/restore")
	public String recover(@PathVariable("id") Integer id, RedirectAttributes attributes) {
		if (deletedEmployeeService.isEmployeeLimitReachedAfterRestore(1)) {
			attributes.addFlashAttribute("toastError", "登録件数が上限（500件）に達するため、復元できません。");
			return "redirect:/deleted-employees";
		}
		if (deletedEmployeeService.existAccountsByEmployeeId(id)) {
			attributes.addFlashAttribute("toastError", "紐づくアカウント情報が削除状態のため、復元できません。先にアカウント情報を復元してください。");
			return "redirect:/deleted-employees";
		}
		if (deletedEmployeeService.existCompaniesByEmployeeId(id)) {
			attributes.addFlashAttribute("toastError", "所属元の企業情報が削除状態のため、復元できません。先に企業情報を復元してください。");
			return "redirect:/deleted-employees";
		}
		if (deletedEmployeeService.isEmailDuplicate(id)) {
			attributes.addFlashAttribute("toastError", "このメールアドレスは既に使用されています");
			return "redirect:/deleted-employees";
		}
		deletedEmployeeService.restore(id);
		return "redirect:/deleted-employees";
	}

	/**
	 * 選択済みのすべての論理削除済み社員情報を復元する。
	 *
	 * @param ids 復元する社員情報のID
	 * @param attributes リダイレクト時にメッセージを引き継ぐための属性
	 * @return 一覧画面へのリダイレクト
	 */
	@PostMapping("/bulk-restore")
	public String bulkRecover(@RequestParam(name = "ids", required = false) List<Integer> ids,
			RedirectAttributes attributes) {
		if (ids == null || ids.isEmpty()) {
			attributes.addFlashAttribute("toastError", "復元対象が選択されていません");
			return "redirect:/deleted-employees";
		}
		if (deletedEmployeeService.isEmployeeLimitReachedAfterRestore(ids.size())) {
			attributes.addFlashAttribute("toastError", "復元後の件数が上限に達しています。企業情報の登録上限は500件です。");
			return "redirect:/deleted-employees";
		}
		if (deletedEmployeeService.existAccountsByEmployeeIds(ids)) {
			attributes.addFlashAttribute("toastError", "紐づくアカウント情報が削除状態のため、復元できません。先にアカウント情報を復元してください。");
			return "redirect:/deleted-employees";
		}
		if (deletedEmployeeService.existCompaniesByEmployeeIds(ids)) {
			attributes.addFlashAttribute("toastError", "所属元の企業情報が削除状態のため、復元できません。先に企業情報を復元してください。");
			return "redirect:/deleted-employees";
		}
		if (deletedEmployeeService.isEmailDuplicate(ids)) {
			attributes.addFlashAttribute("toastError", "このメールアドレスは既に使用されています");
			return "redirect:/deleted-employees";
		}
		deletedEmployeeService.restoreBulk(ids);
		return "redirect:/deleted-employees";
	}

	// ==========================================
    // 物理削除処理
    // ==========================================
	
	/**
	 * 社員情報を1件物理削除する。
	 *
	 * @param id         削除対象の社員ID
	 * @param attributes リダイレクト時にメッセージを引き継ぐための属性
	 * @return 一覧画面へのリダイレクト
	 */
	@PostMapping("/{id}/delete")
	public String delete(@PathVariable("id") Integer id, RedirectAttributes attributes) {
		if (deletedEmployeeService.existAssignmentsByEmployeeId(id)) {
			attributes.addFlashAttribute("toastError", "紐づくアサイン履歴情報が存在するため、削除できません。先にアサイン履歴情報を削除してください。");
			return "redirect:/deleted-employees";
		}

		deletedEmployeeService.physicalDelete(id);
		return "redirect:/deleted-employees";
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
			return "redirect:/deleted-employees";
		}

		// Serviceの判定メソッドを使って一括不在条件をチェック
		if (deletedEmployeeService.existAssignmentsByEmployeeIds(ids)) {
			attributes.addFlashAttribute("toastError", "紐づくアサイン履歴情報が存在するアカウントが含まれているため、物理削除できません。先にアサイン履歴情報を削除してください。");
			return "redirect:/deleted-employees";
		}

		deletedEmployeeService.physicalDeleteBulk(ids);
		return "redirect:/deleted-employees";
	}

	/**
	 * 社員データのエクスポート画面を表示する。
	 *
	 * @param ids    エクスポート対象となる社員IDのリスト 
	 * @param model  画面描画用のモデル
	 * @return エクスポート画面のテンプレートパス
	 */
	@PostMapping("/export")
	public String showExport(
			@RequestParam(name = "ids", required = false) List<Integer> ids,
			Model model, RedirectAttributes attributes) {

		if (ids == null || ids.isEmpty()) {
			attributes.addFlashAttribute("toastError", "エクスポートする対象が選択されていません");
			return "redirect:/deleted-employees";
		}
		List<Employee> employees = deletedEmployeeService.findByIds(ids);
		model.addAttribute("employees", employees);
		model.addAttribute("count", employees.size());
		model.addAttribute("ids", ids);
		return "deleted_employee/export";
	}

	/**
	 * 検索条件に合致する論理削除済みアカウント情報をCSV形式でダウンロードする。
	 *
	 * @param ids    エクスポート対象となる社員IDのリスト 
	 * @return ダウンロード用のCSVファイルバイナリデータ
	 */
	@PostMapping("/export/download")
	public ResponseEntity<byte[]> downloadCsv(
			@RequestParam(name = "ids", required = false) List<Integer> ids) {
		List<Employee> employees = deletedEmployeeService.findByIds(ids);
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
		headers.add("Content-Disposition", "attachment; filename=deleted-employees.csv");
		headers.add("Content-Type", "text/csv; charset=UTF-8");
		return new ResponseEntity<>(result, headers, HttpStatus.OK);
	}
}
