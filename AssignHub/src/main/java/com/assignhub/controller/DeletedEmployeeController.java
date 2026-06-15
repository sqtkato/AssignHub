package com.assignhub.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.assignhub.service.DeletedEmployeeService;

/**
 * 社員管理機能の画面遷移およびHTTPリクエストを処理するコントローラー。
 * @author C3S) 野本
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
        model.addAttribute("employees", deletedEmployeeService.findAll(empName, empAssignCompany, empEngineerType, empCompany));
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
		if(deletedEmployeeService.countAccountsByEmpolyeeId(id)) {
			attributes.addFlashAttribute("toastMessage","紐づくアカウント情報が削除状態のため、復元できません。先にアカウント情報を復元してください。");
			return "redirect:/deleted-employees";
		}
		if(deletedEmployeeService.countCompaniesByEmpolyeeId(id)) {
			attributes.addFlashAttribute("toastMessage","所属元の企業情報が削除状態のため、復元できません。先に企業情報を復元してください。");
			return "redirect:/deleted-employees";
		}
        deletedEmployeeService.restore(id);
        attributes.addFlashAttribute("toastMessage", "社員情報を復元しました");
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
    public String bulkRecover(@RequestParam(name = "ids", required = false) List<Integer> ids, RedirectAttributes attributes) {
        if (ids == null || ids.isEmpty()) {
            attributes.addFlashAttribute("toastError", "復元対象が選択されていません");
            return "redirect:/deleted-employees";
        }
        if(deletedEmployeeService.countAccountsByEmpolyeeIds(ids)) {
        	attributes.addFlashAttribute("toastError","紐づくアカウント情報が削除状態のため、復元できません。先にアカウント情報を復元してください。");
        	return "redirect:/deleted-employees";
        }
        if(deletedEmployeeService.countCompaniesByEmpolyeeIds(ids)) {
        	attributes.addFlashAttribute("toastError","所属元の企業情報が削除状態のため、復元できません。先に企業情報を復元してください。");
        	return "redirect:/deleted-employees";
        }
        deletedEmployeeService.restoreBulk(ids);
        attributes.addFlashAttribute("toastMessage", ids.size() + "件の社員情報を復元しました");
        return "redirect:/deleted-employees";
    }
		
	/**
	 * 社員情報を1件物理削除する。
	 *
	 * @param id         削除対象の社員ID
	 * @param attributes リダイレクト時にメッセージを引き継ぐための属性
	 * @return 一覧画面へのリダイレクト
	 */
	@PostMapping("/{id}/delete")
	public String delete(@PathVariable("id") Integer id, RedirectAttributes attributes) {
		if(deletedEmployeeService.countAssignmentsByEmpolyeeId(id)) {
			attributes.addFlashAttribute("toastMessage","紐づくアサイン履歴情報が存在するため、削除できません。先にアサイン履歴情報を削除してください。");
			return "redirect:/employees";
		}
	
		deletedEmployeeService.physicalDelete(id);
		attributes.addFlashAttribute("toastMessage", "社員情報を削除しました");
		return "redirect:/employees";
	}
	
	/**
	 * 選択された複数の社員情報を一括で物理削除する。
	 *
	 * @param ids        削除対象となる社員IDのリスト
	 * @param attributes リダイレクト時にメッセージを引き継ぐための属性
	 * @return 一覧画面へのリダイレクト
	 */
	@PostMapping("/bulk-delete")
    public String bulkDeleted(@RequestParam(name = "ids", required = false) List<Integer> ids, RedirectAttributes attributes) {
        if (ids == null || ids.isEmpty()) {
            attributes.addFlashAttribute("toastError", "削除対象が選択されていません");
            return "redirect:/deleted-Employees";
        }
        
        // Serviceの判定メソッドを使って一括不在条件をチェック
        if (deletedEmployeeService.countAssignmentsByEmpolyeeIds(ids)) {
            attributes.addFlashAttribute("toastError", "紐づくアサイン履歴情報が存在するアカウントが含まれているため、物理削除できません。先にアサイン履歴情報を削除してください。");
            return "redirect:/deleted-Employees";
        }
        
        deletedEmployeeService.physicalDeleteBulk(ids);
        attributes.addFlashAttribute("toastMessage", ids.size() + "件のアカウント情報を完全に削除しました");
        return "redirect:/deleted-employees";
    }
	
	/**
	 * 社員データのエクスポート画面を表示する。
	 *
	 * @param txt_emp_name_keyword 現在の検索キーワード（社員名）（状態保持用）
	 * @param txt_emp_assign_company_keyword 現在の検索キーワード（アサイン先企業名）（状態保持用）
	 * @param txt_emp_company_keyword 現在の検索キーワード（所属企業名）（状態保持用）
	 * @param cmb_engtineer_type_keyword 現在の検索キーワード（エンジニアタイプ）（状態保持用）
	 * @param model  画面描画用のモデル
	 * @return エクスポート画面のテンプレートパス
	 */
//	@GetMapping("/export")
//	public String showExport(@RequestParam(name = "txt_emp_name_keyword", required = false) String txt_emp_name_keyword,
//			@RequestParam(name = "txt_emp_assign_company_keyword", required = false) Integer txt_emp_assign_company_keyword,
//			@RequestParam(name = "txt_emp_company_keyword", required = false) String txt_emp_company_keyword,
//			@RequestParam(name = "cmb_engtineer_type_keyword", required = false) String cmb_engtineer_type_keyword,
//			Model model) {
//		model.addAttribute("count", deletedEmployeeService.findAll(txt_emp_name_keyword, txt_emp_assign_company_keyword, txt_emp_company_keyword, cmb_engtineer_type_keyword).size());
//		model.addAttribute("txt_emp_name_keyword", txt_emp_name_keyword);
//		model.addAttribute("txt_emp_assign_company_keyword", txt_emp_assign_company_keyword);
//		model.addAttribute("txt_emp_company_keyword", txt_emp_company_keyword);
//		model.addAttribute("cmb_engtineer_type_keyword", cmb_engtineer_type_keyword);
//		return "employee/export";
//	}

	/**
	 * 検索条件に合致する社員データをCSV形式でダウンロードする。
	 *
	 * @param txt_emp_name_keyword 現在の検索キーワード（社員名）（状態保持用）
	 * @param txt_emp_assign_company_keyword 現在の検索キーワード（アサイン先企業名）（状態保持用）
	 * @param txt_emp_company_keyword 現在の検索キーワード（所属企業名）（状態保持用）
	 * @param cmb_engtineer_type_keyword 現在の検索キーワード（エンジニアタイプ）（状態保持用）
	 * @return ダウンロード用のCSVファイルバイナリデータ
	 */
//	@GetMapping("/export/download")
//	public ResponseEntity<byte[]> downloadCsv(
//			@RequestParam(name = "txt_emp_name_keyword", required = false) String txt_emp_name_keyword,
//			@RequestParam(name = "txt_emp_assign_company_keyword", required = false) Integer txt_emp_assign_company_keyword,
//			@RequestParam(name = "txt_emp_company_keyword", required = false) Integer txt_emp_company_keyword,
//			@RequestParam(name = "cmb_engtineer_type_keyword", required = false) Integer cmb_engtineer_type_keyword
//			) {
//		List<Employee> employees = deletedEmployeeService.findAll(txt_emp_name_keyword, txt_emp_assign_company_keyword, txt_emp_company_keyword, cmb_engtineer_type_keyword);
//	
//		StringBuilder csvBuilder = new StringBuilder("社員ID,社員姓,社員名,社員姓カナ,社員名カナ,入社年,郵便番号,住所,メールアドレス\n");
//		for (Employee emp : employees) {
//			csvBuilder.append(emp.getEmpId()).append(",")
//					.append(emp.getEmpName()).append(",")
//					.append(emp.getDeptId()).append(",")
//					.append(emp.getHireYear()).append(",")
//					.append(emp.getPostalCode() != null ? emp.getPostalCode() : "").append(",")
//					.append(emp.getAddress() != null ? emp.getAddress() : "").append(",")
//					.append(emp.getEmail()).append("\n");
//		}
//		byte[] csvBytes = csvBuilder.toString().getBytes(StandardCharsets.UTF_8);
//		byte[] bom = new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
//		byte[] result = new byte[bom.length + csvBytes.length];
//		System.arraycopy(bom, 0, result, 0, bom.length);
//		System.arraycopy(csvBytes, 0, result, bom.length, csvBytes.length);
//
//		HttpHeaders headers = new HttpHeaders();
//		headers.add("Content-Disposition", "attachment; filename=employees.csv");
//		headers.add("Content-Type", "text/csv; charset=UTF-8");
//		return new ResponseEntity<>(result, headers, HttpStatus.OK);
//	}
	
	
	

}
