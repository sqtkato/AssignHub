package com.assignhub.controller;

import java.nio.charset.StandardCharsets;
import java.util.List;

import jakarta.servlet.http.HttpSession;

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

import com.assignhub.entity.Account;
import com.assignhub.form.AccountForm;
import com.assignhub.service.AccountService;
import com.assignhub.service.EmployeeService;

/**
 * アカウント情報管理機能の画面遷移およびHTTPリクエストを処理するコントローラー。
 *
 * @version 1.03 2026/06/01
 * @author SQT）チームC
 */
@Controller
@RequestMapping("/accounts")
public class AccountController {

	private final AccountService accountService;
	private final EmployeeService employeeService;

	/**
	 * コンストラクタによる依存性の注入。
	 *
	 * @param companyService 企業サービス
	 */
	public AccountController(AccountService accountService, EmployeeService employeeService) {
		this.accountService = accountService;
		this.employeeService = employeeService;
	}

	/**
	 * アカウント一覧画面を表示する。検索・ソート条件に応じたデータを取得する。
	 *
	 * @param keywordEmpName 社員名検索キーワード（任意）
	 * @param model 画面描画用モデル
	 * @return 一覧画面のテンプレートパス
	 */
	@GetMapping
	public String index(@RequestParam(name = "empName", required = false) String empName,
			Model model,
			@RequestParam(name = "permission", required = false) Integer permission) {
		model.addAttribute("accounts", accountService.findAll(empName, permission));
		return "account/index";
	}

	/**
	 * アカウント情報の新規登録画面を表示する。
	 *
	 * @param model 画面描画用のモデル
	 * @return アカウント情報新規登録画面のテンプレートパス
	 */
	@GetMapping("/new")
	public String create(Model model, HttpSession session, RedirectAttributes attributes) {

		if (accountService.isMaxCount()) {
			attributes.addFlashAttribute(
		            "toastError",
		            "登録件数が上限(500件)に達しているため登録できません。");

			return "redirect:/accounts";
		}
		model.addAttribute("currentLoginId", session.getAttribute("loginId"));
		model.addAttribute("account", new AccountForm());
		return "account/create";
	}

	/**
	 * アカウント情報の新規登録処理を実行する。
	 *
	 * @param form 入力されたアカウント情報フォーム
	 * @param result       バリデーション結果
	 * @param attributes   リダイレクト時にメッセージを引き継ぐための属性
	 * @param model        画面描画用のモデル
	 * @return 成功時は一覧画面へのリダイレクト、失敗時は登録画面のテンプレートパス
	 */
	@PostMapping("/create")
	public String store(@Validated @ModelAttribute("account") AccountForm form,
			BindingResult result, Model model) {
		if (result.hasErrors()) {
			return "account/create";
		}
		if (accountService.isLoginIdDuplicate(form.getLoginId(), null)) {
			model.addAttribute("loginIdError", "このログインIDは既に使用されています");
			return "account/create";
		}
		Account account = new Account();
		copyFormToEntity(form, account);
		accountService.save(account);

		return "redirect:/accounts";
	}

	/**
	 * アカウント情報の編集画面を表示する。
	 *
	 * @param id    編集対象のアカウントID
	 * @param model 画面描画用のモデル
	 * @return アカウント情報編集画面のテンプレートパス
	 */
	@GetMapping("/{id}/edit")
	public String edit(@PathVariable("id") Integer id, Model model) {
		if (!model.containsAttribute("accountForm")) {
			Account acc = accountService.findById(id);
			AccountForm form = new AccountForm();
			form.setAccountId(acc.getAccountId());
			form.setLoginId(acc.getLoginId());
			form.setPasswordHash(acc.getPasswordHash());
			form.setPermission(acc.getPermission());
			model.addAttribute("accountForm", form);
		}
		return "account/edit";
	}

	/**
	 * アカウント情報の更新処理を実行する。
	 *
	 * @param id           更新対象のアカウントID
	 * @param accountForm 入力されたアカウント情報フォーム
	 * @param result       バリデーション結果
	 * @param attributes   リダイレクト時にメッセージを引き継ぐための属性
	 * @param model        画面描画用のモデル
	 * @return 成功時は一覧画面へのリダイレクト、失敗時は編集画面のテンプレートパス
	 */
	@PostMapping("/{id}/edit")
	public String update(@PathVariable("id") Integer id,
			@Validated @ModelAttribute("accountForm") AccountForm accountForm,
			BindingResult result, Model model) {

		if (result.hasErrors()) {
			return "account/edit";
		}

		if (accountService.isLoginIdDuplicate(accountForm.getLoginId(), id)) {
			result.rejectValue("loginId", "error.accountForm", "このログインIDは既に使用されています");
			return "account/edit";
		}

		Account acc = new Account();
		acc.setAccountId(id);
		copyFormToEntity(accountForm, acc);
		accountService.save(acc);
		return "redirect:/accounts";
	}

	/**
	 * アカウントを一件論理削除
	 *
	 * @param id 削除対象のアカウントID
	 * @return 一覧画面へのリダイレクトパス
	 */
	@PostMapping("/{id}/delete")
	public String delete(@PathVariable("id") Integer id, RedirectAttributes attributes) {
		accountService.delete(id);
		employeeService.delete(id);
		return "redirect:/accounts";
	}

	/**
	 * 選択された複数の社員情報を一括で物理削除する。
	 *
	 * @param ids        削除対象となるアカウントIDのリスト
	 * @param attributes リダイレクト時にメッセージを引き継ぐための属性
	 * @return 一覧画面へのリダイレクト
	 */
	@PostMapping("/bulk-delete")
	public String bulkDelete(@RequestParam(name = "ids", required = false) List<Integer> ids,
			RedirectAttributes attributes) {
		if (ids == null || ids.isEmpty()) {
			attributes.addFlashAttribute("toastError", "削除する対象が選択されていません");
			return "redirect:/accounts";
		}
		accountService.deleteBulk(ids);
		employeeService.deleteBulk(ids);
		attributes.addFlashAttribute("toastMessage", ids.size() + "件のアカウント情報を削除しました");
		return "redirect:/accounts";
	}

	/**
	 * アカウント情報インポート画面を表示する。
	 */
	@GetMapping("/import")
	public String showImport(Model model, RedirectAttributes attributes) {
		int currentCount = accountService.findAll("", null).size();
		if (currentCount >= 500) {
			attributes.addFlashAttribute("toastError", "アカウントの登録数が上限（500件）に達しているため、新規登録できません。");
			return "redirect:/accounts";
		}
		return "account/import";
	}

	/**
	 * CSVファイルを用いた社員データの一括インポート処理を実行する。
	 *
	 * @param file  アップロードされたCSVファイル
	 * @param model 画面描画用のモデル
	 * @return インポート画面のテンプレートパス
	 */
	@PostMapping("/import")
	public String importCsv(@RequestParam("file") MultipartFile file, Model model) {
		if (file == null || file.isEmpty()) {
			model.addAttribute("fileError", "ファイルを選択してください");
			return "account/import";
		}

		String filename = file.getOriginalFilename();
		if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
			model.addAttribute("fileError", "ファイル形式が正しくありません。.csvファイルを選択してください。");
			return "account/import";
		}

		if (file.getSize() > 5 * 1024 * 1024) {
			model.addAttribute("fileError", "ファイルサイズは5MB以内にしてください。");
			return "account/import";
		}

		try {
			java.nio.charset.CharsetDecoder decoder = java.nio.charset.StandardCharsets.UTF_8.newDecoder();
			decoder.onMalformedInput(java.nio.charset.CodingErrorAction.REPORT);
			decoder.onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPORT);
			decoder.decode(java.nio.ByteBuffer.wrap(file.getBytes()));
		} catch (Exception e) {
			// UTF-8として読めない → 文字コードが違う
			model.addAttribute("fileError", "UTF-8のCSVファイルを選択してください。");
			return "account/import";
		}

		try {
			int total = accountService.countDataRows(file);

			if (total > 500) {
				model.addAttribute("globalError", "登録後の件数が上限に達しています。アカウント登録条件は500件です。");
				return "account/import";
			}

			AccountService.ImportResult result = accountService.importCsv(file);
			model.addAttribute("importResult", result);
			return "account/import";
		} catch (Exception e) {
			model.addAttribute("fileError", "ファイルの読み込みに失敗しました");
			return "account/import";
		}
	}

	/**
	 * CSVテンプレートをダウンロードする。
	 */
	@GetMapping("/import/template")
	public ResponseEntity<byte[]> downloadTemplate() {
		String csv = "アカウントID,ログインID,パスワード\n"
				+ ",user001,pass1234\n";

		byte[] csvBytes = csv.getBytes(StandardCharsets.UTF_8);
		byte[] bom = new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
		byte[] result = new byte[bom.length + csvBytes.length];
		System.arraycopy(bom, 0, result, 0, bom.length);
		System.arraycopy(csvBytes, 0, result, bom.length, csvBytes.length);

		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "attachment; filename=account_template.csv");
		headers.add("Content-Type", "text/csv; charset=UTF-8");

		return new ResponseEntity<>(result, headers, HttpStatus.OK);
	}

	/**
	 * 社員データのエクスポート画面を表示する。
	 *
	 * @param keyword     現在の検索キーワード（状態保持用）
	 * @param deptId 現在の絞り込み部署ID
	 * @param model  画面描画用のモデル
	 * @return エクスポート画面のテンプレートパス
	 */
	@PostMapping("/export")
	public String showExport(@RequestParam(name = "ids", required = false) List<Integer> ids,
			Model model, RedirectAttributes attributes) {

		// ★【最優先】まず最初にnullチェックを行う
		if (ids == null || ids.isEmpty()) {
			attributes.addFlashAttribute("toastError", "エクスポートする対象が選択されていません");
			return "redirect:/accounts"; // 元の一覧画面に戻す
		}
		List<Account> accounts = accountService.findByIds(ids);
		model.addAttribute("count", accountService.findByIds(ids).size());
		model.addAttribute("accounts", accounts);
		model.addAttribute("ids", ids);
		return "account/export";
	}

	/**
	 * 検索条件に合致する社員データをCSV形式でダウンロードする。
	 *
	 * @param keyword 検索キーワード
	 * @param deptId  絞り込み部署ID
	 * @return ダウンロード用のCSVファイルバイナリデータ
	 */
	@PostMapping("/export/download")
	public ResponseEntity<byte[]> downloadCsv(
			@RequestParam(name = "ids", required = false) List<Integer> ids) {
		List<Account> accounts = accountService.findByIds(ids);
		StringBuilder csvBuilder = new StringBuilder("アカウントID,ログインID,権限,社員名(姓),社員名(名)\n");
		for (Account acc : accounts) {
			String lastName = "-";
			String firstName = "-";
			if (acc.getEmployee() != null) {
				lastName = acc.getEmployee().getLastName();
				firstName = acc.getEmployee().getFirstName();
			}

			csvBuilder.append(acc.getAccountId()).append(",")
					.append(acc.getLoginId()).append(",")
					.append(acc.getPermission()).append(",")
					.append(lastName).append(",") 
					.append(firstName).append("\n");
		}
		byte[] csvBytes = csvBuilder.toString().getBytes(StandardCharsets.UTF_8);
		byte[] bom = new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
		byte[] result = new byte[bom.length + csvBytes.length];
		System.arraycopy(bom, 0, result, 0, bom.length);
		System.arraycopy(csvBytes, 0, result, bom.length, csvBytes.length);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "attachment; filename=account.csv");
		headers.add("Content-Type", "text/csv; charset=UTF-8");
		return new ResponseEntity<>(result, headers, HttpStatus.OK);
	}

	private void copyFormToEntity(AccountForm f, Account e) {
		e.setAccountId(f.getAccountId());
		e.setLoginId(f.getLoginId());
		e.setPasswordHash(f.getPasswordHash());
		e.setPermission(f.getPermission());
	}
}