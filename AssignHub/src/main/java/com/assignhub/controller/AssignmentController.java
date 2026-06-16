package com.assignhub.controller;

import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
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

import com.assignhub.entity.Assignment;
import com.assignhub.form.AssignmentForm;
import com.assignhub.form.SearchForm;
import com.assignhub.service.AssignmentService;
import com.assignhub.service.CompanyService;
import com.assignhub.service.EmployeeService;
import com.assignhub.service.RoleService;

/**
 * アサイン履歴情報のコントローラークラス
 * 
 * @author Team Excel
 * @version 1.00 2026/06/16
 */
@Controller
@RequestMapping("/assignments")
public class AssignmentController {

	private final AssignmentService assignmentService;
	private final RoleService roleService;
	private final CompanyService companyService;
	private final EmployeeService employeeService;

	/**
	 * コンストラクタによる依存性の注入。
	 * 
	 * @param assignmentService アサイン履歴情報のサービス
	 * @param roleService 役割情報のサービス
	 * @param companyService 企業情報のサービス
	 * @param employeeService 社員情報のサービス
	 */
	public AssignmentController(
			AssignmentService assignmentService,
			RoleService roleService,
			CompanyService companyService,
			EmployeeService employeeService) {
		this.assignmentService = assignmentService;
		this.roleService = roleService;
		this.companyService = companyService;
		this.employeeService = employeeService;
	}

	/**
	 * アサイン履歴情報の一覧画面を表示する
	 * 
	 * @param searchForm 検索条件のフォームオブジェクト
	 * @param result バリデーション結果
	 * @param model 画面描画用のモデル
	 * @return アサイン履歴情報の一覧画面のテンプレートパス
	 */
	@GetMapping
	public String index(@Validated @ModelAttribute("searchForm") SearchForm searchForm,
			BindingResult result,
			Model model) {
		LocalDate startDate = searchForm.getContractStartDate();
		LocalDate endDate = searchForm.getContractEndDate();
		String toastError = null;
		if (result.hasErrors()) {
			toastError = "契約開始日より前の日付は入力できません";
		}
		if (toastError != null) {
			model.addAttribute("toastError", toastError);
			model.addAttribute("assignments", assignmentService.findAll(null, null, null, null, null));
			return "assignment/index";
		}
		model.addAttribute("assignments", assignmentService.findAll(
				searchForm.getEmpName(),
				searchForm.getAssignName(),
				searchForm.getCompanyName(),
				startDate == null ? null : startDate.toString(),
				endDate == null ? null : endDate.toString()));
		return "assignment/index";
	}

	/**
	 * アサイン履歴情報の新規登録画面を表示する。
	 *
	 * @param model 画面描画用のモデル
	 * @return アサイン履歴情報新規登録画面のテンプレートパス
	 */
	@GetMapping("/new")
	public String create(Model model, RedirectAttributes attributes) {
		if (!model.containsAttribute("assignmentForm")) {
			model.addAttribute("assignmentForm", new AssignmentForm());
		}
		if (assignmentService.isMaxCount()) {
			attributes.addFlashAttribute(
					"toastError",
					"登録件数が上限（500件）に達しています");
			return "redirect:/assignments";
		}
		model.addAttribute("employees", employeeService.findAll(null, null, null, null));
		model.addAttribute("companies", companyService.findAll(null, null));
		model.addAttribute("role", roleService.findAll());
		return "assignment/create";
	}

	/**
	 * アサイン履歴情報の新規登録処理を実行する。
	 *
	 * @param assignmentForm 入力されたアサイン履歴情報フォーム
	 * @param result       バリデーション結果
	 * @param attributes   リダイレクト時にメッセージを引き継ぐための属性
	 * @param model        画面描画用のモデル
	 * @return 成功時は一覧画面へのリダイレクト、失敗時は登録画面のテンプレートパス
	 */
	@PostMapping
	public String store(@Validated @ModelAttribute("assignmentForm") AssignmentForm form,
			BindingResult result, RedirectAttributes attributes, Model model) {
		Assignment assignment = new Assignment();
		copyFormToEntity(form, assignment);
		if (assignment.getContractEndDate() != null
				&& assignment.getContractStartDate().isAfter(assignment.getContractEndDate())) {
			result.rejectValue(
					"contractEndDate",
					"date.order",
					"契約開始日より前の日付は入力できません");
		}
		if (result.hasErrors()) {
			model.addAttribute("employees", employeeService.findAll(null, null, null, null));
			model.addAttribute("companies", companyService.findAll(null, null));
			model.addAttribute("role", roleService.findAll());
			return "assignment/create";
		}
		if (assignmentService.existsDuplicate(assignment)) {
			result.reject(
					"duplicate",
					"既に同じ内容が登録されています");
			model.addAttribute("employees", employeeService.findAll(null, null, null, null));
			model.addAttribute("companies", companyService.findAll(null, null));
			model.addAttribute("role", roleService.findAll());
			return "assignment/create";
		}
		assignmentService.save(assignment);
		attributes.addFlashAttribute("toastMessage", "アサイン履歴情報を登録しました");
		return "redirect:/assignments";
	}

	/**
	 * アサイン履歴情報の詳細画面を表示する。
	 *
	 * @param id    表示対象のアサインID
	 * @param model 画面描画用のモデル
	 * @return アサイン履歴情報詳細画面のテンプレートパス
	 */
	@GetMapping("/{id}/detail")
	public String detail(@PathVariable Integer id, Model model) {
		Assignment assignment = assignmentService.findById(id);
		model.addAttribute("assignment", assignment);
		return "assignment/detail";
	}

	/**
	 * アサイン履歴情報の編集画面を表示する。
	 *
	 * @param id    編集対象のアサインID
	 * @param from どの画面から遷移してきたかを示すパラメータ
	 * @param model 画面描画用のモデル
	 * @return アサイン履歴情報編集画面のテンプレートパス
	 */
	@GetMapping("/{id}/edit")
	public String edit(@PathVariable("id") Integer id,
			@RequestParam(value = "from", required = false) String from,
			Model model) {
		if (!model.containsAttribute("assignmentForm")) {
			model.addAttribute("assignmentForm", new AssignmentForm());
			Assignment assign = assignmentService.findById(id);
			AssignmentForm form = new AssignmentForm();
			form.setEmpId(assign.getEmpId());
			form.setCompanyId(assign.getCompanyId());
			form.setContractStartDate(assign.getContractStartDate());
			form.setContractEndDate(assign.getContractEndDate());
			form.setUnitPrice(assign.getUnitPrice());
			form.setRoleId(assign.getRoleId());
			model.addAttribute("assignmentForm", form);
		}
		model.addAttribute("fromPage", from);
		model.addAttribute("employees", employeeService.findAll(null, null, null, null));
		model.addAttribute("companies", companyService.findAll(null, null));
		model.addAttribute("role", roleService.findAll());
		return "assignment/edit";
	}

	/**
	 * アサイン履歴情報の更新処理を実行する。
	 *
	 * @param id           更新対象のアサインID
	 * @param assignmentForm 入力されたアサイン履歴情報フォーム
	 * @param result       バリデーション結果
	 * @param attributes   リダイレクト時にメッセージを引き継ぐための属性
	 * @param model        画面描画用のモデル
	 * @return 成功時は一覧画面へのリダイレクト、失敗時は編集画面のテンプレートパス
	 */
	@PostMapping("/{id}/edit")
	public String update(@PathVariable("id") Integer id,
			@Validated @ModelAttribute("assignmentForm") AssignmentForm form,
			BindingResult result, RedirectAttributes attributes,
			@RequestParam(value = "fromPage", required = false) String fromPage, Model model) {
		Assignment assignment = new Assignment();
		copyFormToEntity(form, assignment);
		if (assignment.getContractEndDate() != null
				&& assignment.getContractStartDate().isAfter(assignment.getContractEndDate())) {
			result.rejectValue(
					"contractEndDate",
					"date.order",
					"契約開始日より前の日付は入力できません");
		}
		if (result.hasErrors()) {
			model.addAttribute("fromPage", fromPage);
			model.addAttribute("employees", employeeService.findAll(null, null, null, null));
			model.addAttribute("companies", companyService.findAll(null, null));
			model.addAttribute("role", roleService.findAll());
			return "assignment/edit";
		}
		assignment.setAssignmentId(id);
		assignmentService.save(assignment);
		attributes.addFlashAttribute("toastMessage", "アサイン履歴情報を更新しました");
		if ("detail".equals(fromPage)) {
			return "redirect:/assignments/" + id + "/detail";
		}
		return "redirect:/assignments";
	}

	/**
	 * アサイン履歴情報のインポート画面を表示する。
	 *
	 * @return アサイン履歴情報のインポート画面のテンプレートパス
	 */
	@GetMapping("/import")
	public String showImport() {
		return "assignment/import";
	}

	/**
	 * CSVファイルを用いたアサイン履歴情報の一括インポート処理を実行する。
	 *
	 * @param file  アップロードされたCSVファイル
	 * @param model 画面描画用のモデル
	 * @return インポート画面のテンプレートパス
	 */
	@PostMapping("/import")
	public String importCsv(
	        @RequestParam(name = "input_assign_file_upload", required = false) MultipartFile file,
	        Model model) {

	    if (file == null || file.isEmpty()) {
	        model.addAttribute("toastError", "ファイルを選択してください");
	        return "assignment/import";
	    }

	    String filename = file.getOriginalFilename();
	    if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
	        model.addAttribute("toastError", "ファイルの形式が正しくありません。CSVファイルを選択してください");
	        return "assignment/import";
	    }

	    if (file.getSize() > 5 * 1024 * 1024) {
	        model.addAttribute("toastError", "ファイルサイズは5MB以内にしてください");
	        return "assignment/import";
	    }

	    try {
	        AssignmentService.ImportResult result = assignmentService.importCsv(file);
	        model.addAttribute("importResult", result);
	        if (result.errorCount > 0) {
	            model.addAttribute("toastError", "一部の行でエラーが発生しました");
	        } else {
	            model.addAttribute("toastMessage", result.successCount + "件のインポート処理が完了しました");
	        }
	        return "assignment/import";
	    } catch (MalformedInputException e) {
	        model.addAttribute("toastError", "UTF-8のCSVファイルを選択してください");
	        return "assignment/import";
	    } catch (Exception e) {
	        model.addAttribute("toastError", "ファイルの読み込みに失敗しました");
	        return "assignment/import";
	    }
	}

	/**
	 * インポート用のCSVテンプレートをダウンロードする。
	 *
	 * @return ダウンロード用のCSVファイルバイナリデータ
	 */
	@GetMapping("/import/template")
	public ResponseEntity<byte[]> downloadTemplate() {
		String csvContent = "アサインID,社員ID,社員姓,社員名,アサイン先企業名,作成日時,更新日時,契約開始日,契約終了日,契約単価,役割\n";
		byte[] csvBytes = csvContent.getBytes(StandardCharsets.UTF_8);
		byte[] bom = new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
		byte[] result = new byte[bom.length + csvBytes.length];
		System.arraycopy(bom, 0, result, 0, bom.length);
		System.arraycopy(csvBytes, 0, result, bom.length, csvBytes.length);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "attachment; filename=assignment_template.csv");
		headers.add("Content-Type", "text/csv; charset=UTF-8");
		return new ResponseEntity<>(result, headers, HttpStatus.OK);
	}

	/**
	 * 選択された複数のアサイン履歴情報を一括で論理削除する。
	 * 
	 * @param ids 削除対象のアサイン履歴IDのリスト
	 * @param attributes リダイレクト属性オブジェクト
	 * @return アサイン履歴情報の一覧画面にリダイレクト
	 */
	@PostMapping("/bulk-delete")
	public String bulkDelete(
			@RequestParam(name = "ids", required = false) List<Integer> ids,
			RedirectAttributes attributes) {
		if (ids == null || ids.isEmpty()) {
			attributes.addFlashAttribute("toastError", "削除する対象が選択されていません");
			return "redirect:/assignments";
		}
		assignmentService.deleteBulk(ids);
		attributes.addFlashAttribute("toastMessage", "選択したアサイン履歴情報を削除しました");
		return "redirect:/assignments";
	}

	/**
	 * アサイン履歴情報を1件論理削除する
	 * 
	 * @param id 削除対象のアサイン履歴ID
	 * @param attributes リダイレクト属性オブジェクト
	 * @return アサイン情報の一覧画面にリダイレクト
	 */
	@PostMapping("/{id}/delete")
	public String delete(@PathVariable("id") Integer id, RedirectAttributes attributes) {
		assignmentService.delete(id);
		attributes.addFlashAttribute("toastMessage", "アサイン履歴情報を削除しました");
		return "redirect:/assignments";
	}

	/**
	 * 選択されたアサイン履歴情報を一括でエクスポートする
	 * 
	 * @param ids エクスポート対象のアサインIDリスト
	 * @param attributes リダイレクト属性オブジェクト
	 * @return アサイン履歴情報の一覧画面にリダイレクト
	 */
	@PostMapping("/export")
	public String showExport(@RequestParam(name = "ids", required = false) List<Integer> ids,
			Model model, RedirectAttributes attributes) {
		if (ids == null || ids.isEmpty()) {
			attributes.addFlashAttribute("toastError", "エクスポートする対象が選択されていません");
			return "redirect:/assignments";
		}
		List<Assignment> assignments = assignmentService.findByIds(ids);
		model.addAttribute("count", assignments.size());
		model.addAttribute("assignments", assignments);
		model.addAttribute("ids", ids);
		return "assignment/export";
	}

	/**
	 * 検索条件に合致するアサイン履歴データをCSV形式でダウンロードする。
	 * 
	 * @param ids エクスポート対象のアサインIDリスト
	 * @return CSVファイルのバイナリデータを含むHTTPレスポンスエンティティ
	 */
	@PostMapping("/export/download")
	public ResponseEntity<byte[]> downloadCsv(
			@RequestParam(name = "ids", required = false) List<Integer> ids) {
		List<Assignment> assignments = assignmentService.findByIds(ids);
		StringBuilder csvBuilder = new StringBuilder("アサインID,社員ID,社員姓,社員名,アサイン先企業名,作成日時,更新日時,契約開始日,契約終了日,契約単価,役割\n");
		for (Assignment asn : assignments) {
			csvBuilder.append(asn.getAssignmentId()).append(",")
					.append(asn.getEmpId()).append(",")
					.append(asn.getEmployee().getLastName()).append(",")
					.append(asn.getEmployee().getFirstName()).append(",")
					.append(asn.getCompany().getCompanyName()).append(",")
					.append(asn.getCreatedAt()).append(",")
					.append(asn.getUpdatedAt()).append(",")
					.append(asn.getContractStartDate()).append(",")
					.append(asn.getContractEndDate() != null ? asn.getContractEndDate() : "ー").append(",")
					.append(asn.getUnitPrice()).append(",")
					.append(asn.getRole().getRole()).append("\n");
		}
		byte[] csvBytes = csvBuilder.toString().getBytes(StandardCharsets.UTF_8);
		byte[] bom = new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
		byte[] result = new byte[bom.length + csvBytes.length];
		System.arraycopy(bom, 0, result, 0, bom.length);
		System.arraycopy(csvBytes, 0, result, bom.length, csvBytes.length);

		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "attachment; filename=assignment.csv");
		headers.add("Content-Type", "text/csv; charset=UTF-8");
		return new ResponseEntity<>(result, headers, HttpStatus.OK);
	}

	/**
	 * フォームオブジェクトからエンティティオブジェクトへプロパティを詰め替える。
	 * 
	 * @param f 入力フォーム（コピー元）
	 * @param e エンティティ（コピー先）
	 */
	private void copyFormToEntity(AssignmentForm f, Assignment e) {
		e.setAssignmentId(f.getAssignmentId());
		e.setEmpId(f.getEmpId());
		e.setCompanyId(f.getCompanyId());
		e.setContractStartDate(f.getContractStartDate());
		e.setContractEndDate(f.getContractEndDate());
		e.setUnitPrice(f.getUnitPrice());
		e.setRoleId(f.getRoleId());
	}
}
