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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.assignhub.entity.Company;
import com.assignhub.form.CompanyForm;
import com.assignhub.service.AssignmentService;
import com.assignhub.service.CompanyService;
import com.assignhub.service.EmployeeService;

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
	private final EmployeeService employeeService;
	private final AssignmentService assignmentService;

	/**
	 * コンストラクタによる依存性の注入。
	 *
	 * @param companyService 企業サービス
	 */
	public CompanyController(CompanyService companyService, EmployeeService employeeService,
			AssignmentService assignmentService) {
		this.companyService = companyService;
		this.employeeService = employeeService;
		this.assignmentService = assignmentService;
	}

	/**
	 * 企業一覧画面を表示する。検索条件に応じたデータを取得する。
	 *
	 * @param companyNameSearch 企業名検索キーワード（任意）
	 * @param companyTelSearch  TEL検索キーワード（任意）
	 * @param model             画面描画用モデル
	 * @return 一覧画面のテンプレートパス
	 */
	@GetMapping
	public String index(@RequestParam(name = "companyName", required = false) String companyName,
			@RequestParam(name = "companyTel", required = false) String companyTel, Model model) {
		model.addAttribute("companies", companyService.findAll(companyName, companyTel));
		model.addAttribute("companyName", companyName);
		model.addAttribute("companyTel", companyTel);
		return "company/index";
	}

	/**
	 * 企業の新規登録画面を表示する。
	 *
	 * @param model 画面描画用モデル
	 * @return 新規登録画面のテンプレートパス
	 */
	@GetMapping("/new")
	public String create(Model model, RedirectAttributes attributes) {
		if (!model.containsAttribute("companyForm")) {
			model.addAttribute("companyForm", new CompanyForm());
		}
		if (companyService.isMaxCount()) {
			attributes.addFlashAttribute(
					"toastError",
					"登録件数が上限（500件）に達しています");
			return "redirect:/companies";
		}
		return "company/create";
	}

	/**
	 * 入力された企業情報をデータベースに登録する。
	 *
	 * @param companyForm 入力フォームデータ
	 * @param result      バリデーション結果
	 * @param attributes  リダイレクト先へ渡すフラッシュスコープ
	 * @return 成功時は一覧画面へリダイレクト、失敗時は登録画面へ戻る
	 */
	@PostMapping
	public String store(
			@Validated @ModelAttribute("companyForm") CompanyForm companyForm,
			BindingResult result,
			RedirectAttributes attributes) {

		// 企業名重複チェック
		if (companyService.isCompanyNameDuplicate(companyForm.getCompanyName(), null)) {
			result.rejectValue("companyName", "error.companyForm", "この企業名は既に使用されています");
		}
		// 設立年度チェック
		if (companyForm.getFoundedYear() != null) {
			int currentYear = java.time.Year.now().getValue();
			if (companyForm.getFoundedYear() > currentYear) {
				result.rejectValue("foundedYear", "error", "設立年度は現在年度以前を入力してください");
			}
		}
		// TEL重複チェック
		if (companyService.isCompanyTelDuplicate(companyForm.getCompanyTel(), null)) {
			result.rejectValue("companyTel", "error.companyForm", "この電話番号は既に使用されています");
		}
		// FAX重複チェック
		if (companyService.isCompanyFaxDuplicate(companyForm.getCompanyFax(), null)) {
			result.rejectValue("companyFax", "error.companyForm", "このFAX番号は既に使用されています");
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
	 * 企業情報の編集画面を表示する。
	 *
	 * @param companyId 編集対象の企業ID
	 * @param from      遷移元画面
	 * @param model     画面描画用モデル
	 * @return 編集画面のテンプレートパス
	 */
	@GetMapping("/{id}/edit")
	public String edit(
			@PathVariable("id") Integer companyId,
			@RequestParam(value = "from", required = false) String from,
			Model model) {
		if (!model.containsAttribute("companyForm")) {
			Company comp = companyService.findById(companyId);
			CompanyForm form = new CompanyForm();
			form.setCompanyId(comp.getCompanyId());
			form.setCompanyName(comp.getCompanyName());
			form.setCompanyNameKana(comp.getCompanyNameKana());
			form.setCompanyZipCode(comp.getCompanyZipCode());
			form.setCompanyAddress1(comp.getCompanyAddress1());
			form.setCompanyAddress2(comp.getCompanyAddress2());
			form.setCompanyTel(comp.getCompanyTel());
			form.setCompanyFax(comp.getCompanyFax());
			form.setFoundedYear(comp.getFoundedYear());
			form.setEmployeeCount(comp.getEmployeeCount());
			form.setRepLastName(comp.getRepLastName());
			form.setRepLastNameKana(comp.getRepLastNameKana());
			form.setRepFirstName(comp.getRepFirstName());
			form.setRepFirstNameKana(comp.getRepFirstNameKana());
			model.addAttribute("companyForm", form);
		}
		model.addAttribute("fromPage", from);
		return "company/edit";
	}

	/**
	 * 企業情報の更新処理を実行する。
	 *
	 * @param companyId   更新対象の企業ID
	 * @param companyForm 入力フォームデータ
	 * @param result      バリデーション結果
	 * @param attributes  リダイレクト時にメッセージを引き継ぐための属性
	 * @param fromPage    遷移元画面
	 * @param model       画面描画用モデル
	 * @return 成功時はリダイレクト、失敗時は編集画面へ戻る
	 */
	@PostMapping("/{id}/edit")
	public String update(
			@PathVariable("id") Integer companyId,
			@Validated @ModelAttribute("companyForm") CompanyForm companyForm,
			BindingResult result,
			RedirectAttributes attributes,
			@RequestParam(value = "fromPage", required = false) String fromPage,
			Model model) {

		// 企業名重複チェック
		if (companyService.isCompanyNameDuplicate(companyForm.getCompanyName(), companyId)) {
			result.rejectValue("companyName", "error.companyForm", "この企業名はすでに使用されています");
		}

		// TEL重複チェック
		if (companyService.isCompanyTelDuplicate(companyForm.getCompanyTel(), companyId)) {
			result.rejectValue("companyTel", "error.companyForm", "この電話番号は既に使用されています");
		}

		// FAX重複チェック
		if (companyService.isCompanyFaxDuplicate(companyForm.getCompanyFax(), companyId)) {
			result.rejectValue("companyFax", "error.companyForm", "このFAX番号は既に使用されています");
		}

		if (result.hasErrors()) {
			companyForm.setCompanyId(companyId);
			model.addAttribute("companies", companyService.findAll(null, null));
			model.addAttribute("fromPage", fromPage);
			return "company/edit";
		}

		Company comp = new Company();
		comp.setCompanyId(companyId);
		copyFormToEntity(companyForm, comp);
		companyService.save(comp);

		attributes.addFlashAttribute("toastMessage", "企業情報を更新しました");
		if ("detail".equals(fromPage)) {
			return "redirect:/companies/" + comp.getCompanyId() + "/detail";
		} else if ("index".equals(fromPage)) {
			return "redirect:/companies";
		}
		return "redirect:/companies";
	}

	/**
	 * 企業情報の詳細画面を表示する。
	 *
	 * @param id    表示対象の企業ID
	 * @param from  遷移元画面
	 * @param model 画面描画用モデル
	 * @return 詳細画面のテンプレートパス
	 */
	@GetMapping("/{id}/detail")
	public String detail(
			@PathVariable("id") Integer id,
			@RequestParam(value = "from", required = false) String from,
			Model model) {
		Company company = companyService.findById(id);
		model.addAttribute("company", company);
		model.addAttribute("fromPage", from);
		return "company/detail";
	}

	/**
	 * 企業情報を1件論理削除する。
	 *
	 * @param id         削除対象の企業ID
	 * @param attributes リダイレクト時にメッセージを引き継ぐための属性
	 * @return 一覧画面へのリダイレクト
	 */
	@PostMapping("/{id}/delete")
	public String delete(
			@PathVariable("id") Integer id,
			RedirectAttributes attributes) {
		companyService.delete(id);
		employeeService.deleteByCompanyId(id);
		assignmentService.deleteByCompanyId(id);
		attributes.addFlashAttribute("toastMessage", "企業情報を削除しました");
		return "redirect:/companies";
	}

	/**
	 * 複数の企業情報を一括で論理削除する。
	 *
	 * @param ids        削除対象IDリスト
	 * @param attributes リダイレクト時にメッセージを引き継ぐための属性
	 * @return 一覧画面へのリダイレクト
	 */
	@PostMapping("/bulk-delete")
	public String bulkDelete(
			@RequestParam(name = "ids", required = false) List<Integer> ids,
			RedirectAttributes attributes) {
		if (ids == null || ids.isEmpty()) {
			attributes.addFlashAttribute("toastError", "削除対象が選択されていません");
			return "redirect:/companies";
		}
		companyService.deleteBulk(ids);
		employeeService.deleteBulkByCompanyId(ids);
		assignmentService.deleteBulkByCompanyId(ids);
		attributes.addFlashAttribute("toastMessage", ids.size() + "件の企業情報を削除しました");
		return "redirect:/companies";
	}

	/**
	 * インポート画面を表示する。
	 *
	 * @return インポート画面のテンプレートパス
	 */
	@GetMapping("/import")
	public String showImport() {
		return "company/import";
	}

	/**
	 * CSVファイルを用いた企業データの一括インポート処理を実行する。
	 *
	 * @param file  アップロードされたCSVファイル
	 * @param model 画面描画用のモデル
	 * @return インポート画面のテンプレートパス
	 */
	@PostMapping("/import")
	public String importCsv(
			@RequestParam("file") MultipartFile file,
			Model model) throws Exception {
		if (file.isEmpty()) {
			model.addAttribute("toastError", "ファイルを選択してください");
			return "company/import";
		}
		String filename = file.getOriginalFilename();
		if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
			model.addAttribute("errorMessage", "ファイル形式が正しくありません。CSVファイルを選択してください。");
			return "company/import";
		}
		if (file.getSize() > 5 * 1024 * 1024) {
			model.addAttribute("errorMessage", "ファイルサイズは5MB以内にしてください");
			return "company/import";
		}
		try {
			CompanyService.ImportResult result = companyService.importCsv(file);
			model.addAttribute("importResult", result);
			if (result.errorCount > 0) {
				model.addAttribute("toastError", "一部の行でエラーが発生しました");
			} else {
				model.addAttribute("toastMessage", result.successCount + "件のインポート処理が完了しました");
			}
			return "company/import";
		} catch (java.nio.charset.MalformedInputException e) {
			model.addAttribute("errorMessage", "UTF-8のCSVファイルを選択してください");
			return "company/import";
		}
	}

	/**
	 * インポート用のCSVテンプレートをダウンロードする。
	 *
	 * @return ダウンロード用のCSVファイルバイナリデータ
	 */
	@GetMapping("/import/template")
	public ResponseEntity<byte[]> downloadTemplate() {
		String csvContent = "企業ID,企業名,企業名カナ,設立年度,社員数,郵便番号,住所1,住所2,TEL,FAX,代表者姓,代表者名,代表者姓カナ,代表者名カナ\n";
		byte[] csvBytes = csvContent.getBytes(StandardCharsets.UTF_8);
		byte[] bom = new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
		byte[] result = new byte[bom.length + csvBytes.length];
		System.arraycopy(bom, 0, result, 0, bom.length);
		System.arraycopy(csvBytes, 0, result, bom.length, csvBytes.length);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "attachment; filename=company_template.csv");
		headers.add("Content-Type", "text/csv; charset=UTF-8");
		return new ResponseEntity<>(result, headers, HttpStatus.OK);
	}

	@PostMapping("/export")
	public String showExport(@RequestParam(name = "ids", required = false) List<Integer> ids, Model model,
			RedirectAttributes attributes) {
		if (ids == null || ids.isEmpty()) {
			attributes.addFlashAttribute("toastError", "エクスポートする対象が選択されていません");
			return "redirect:/companies";
		}

		List<Company> Companies = companyService.findByIds(ids);

		model.addAttribute("count", Companies.size());
		model.addAttribute("companies", Companies);
		model.addAttribute("ids", ids);

		return "company/export";
	}

	/**
	 * 選択された企業情報をCSVでエクスポートする。
	 *
	 * @param ids 選択された企業IDリスト（nullの場合全件）
	 * @return ダウンロード用のCSVファイルバイナリデータ
	 */
	@PostMapping("/export/download")
	public ResponseEntity<byte[]> downloadCsv(
			@RequestParam(name = "ids", required = false) List<Integer> ids) {
		List<Company> companies = companyService.findByIds(ids);
		StringBuilder csvBuilder = new StringBuilder(
				"作成日時,更新日時,企業ID,企業名,企業名カナ,設立年度,社員数,郵便番号,住所1,住所2,TEL,FAX,代表者 姓,代表者 名,代表者 姓：カナ,代表者 名：カナ\n");
		for (Company comp : companies) {
			csvBuilder.append(comp.getCreatedAt() != null ? comp.getCreatedAt() : "").append(",")
					.append(comp.getUpdatedAt() != null ? comp.getUpdatedAt() : "").append(",")
					.append(comp.getCompanyId()).append(",")
					.append(comp.getCompanyName()).append(",")
					.append(comp.getCompanyNameKana()).append(",")
					.append(comp.getFoundedYear() != null ? comp.getFoundedYear() : "").append(",")
					.append(comp.getEmployeeCount() != null ? comp.getEmployeeCount() : "").append(",")
					.append(comp.getCompanyZipCode() != null ? comp.getCompanyZipCode() : "").append(",")
					.append(comp.getCompanyAddress1()).append(",")
					.append(comp.getCompanyAddress2() != null ? comp.getCompanyAddress2() : "").append(",")
					.append(comp.getCompanyTel()).append(",")
					.append(comp.getCompanyFax() != null ? comp.getCompanyFax() : "").append(",")
					.append(comp.getRepLastName() != null ? comp.getRepLastName() : "").append(",")
					.append(comp.getRepFirstName() != null ? comp.getRepFirstName() : "").append(",")
					.append(comp.getRepLastNameKana() != null ? comp.getRepLastNameKana() : "").append(",")
					.append(comp.getRepFirstNameKana() != null ? comp.getRepFirstNameKana() : "").append("\n");
		}
		byte[] csvBytes = csvBuilder.toString().getBytes(StandardCharsets.UTF_8);
		byte[] bom = new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
		byte[] result = new byte[bom.length + csvBytes.length];
		System.arraycopy(bom, 0, result, 0, bom.length);
		System.arraycopy(csvBytes, 0, result, bom.length, csvBytes.length);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "attachment; filename=Company.csv");
		headers.add("Content-Type", "text/csv; charset=UTF-8");
		return new ResponseEntity<>(result, headers, HttpStatus.OK);
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
		e.setCompanyFax(f.getCompanyFax());
		e.setFoundedYear(f.getFoundedYear());
		e.setEmployeeCount(f.getEmployeeCount());
		e.setRepFirstName(f.getRepFirstName());
		e.setRepLastName(f.getRepLastName());
		e.setRepFirstNameKana(f.getRepFirstNameKana());
		e.setRepLastNameKana(f.getRepLastNameKana());
	}
}