package com.assignhub.controller;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
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
import com.assignhub.service.AssignmentService;
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
	private final AssignmentService assignmentService;

	/**
	 * コンストラクタによる依存性の注入。
	 *
	 * @param accountService    アカウントサービス
	 * @param employeeService   社員サービス
	 * @param assignmentService アサインサービス
	 */
	public AccountController(AccountService accountService, EmployeeService employeeService, AssignmentService assignmentService) {
		this.accountService = accountService;
		this.employeeService = employeeService;
		this.assignmentService = assignmentService;
	}
	
	/**
	 * エラーメッセージの切替を行う
	 *
	 * @param binder エラーメッセージの設定
	 */
	@InitBinder
	public void initBinder(WebDataBinder binder) {
	    binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
	}
	
	/**
	 * アカウント一覧画面を表示する。検索条件に応じたデータを取得する
	 * @param empName 社員名検索キーワード（任意）
	 * @param model   画面描画用モデル
	 * @return 一覧画面のテンプレートパス
	 */
	@GetMapping
	public String index(@RequestParam(name = "empName", required = false) String empName,
			Model model,
			@RequestParam(name = "permission", required = false) Integer permission) {
		model.addAttribute("accounts", accountService.findAll(empName, permission));
		model.addAttribute("empName", empName);
	    model.addAttribute("permission", permission);
		return "account/index";
	}

	/**
	 * アカウント情報の新規登録画面を表示する。
	 *
	 * @param model      画面描画用のモデル
	 * @param attributes リダイレクト時にメッセージを引き継ぐための属性
	 * @return アカウント情報新規登録画面のテンプレートパス
	 */
	@GetMapping("/new")
	public String create(Model model, RedirectAttributes attributes) {

		if (accountService.isMaxCount()) {
			attributes.addFlashAttribute(
		            "toastError",
		            "登録件数が上限(500件)に達しているため登録できません。");

			return "redirect:/accounts";
		}
		model.addAttribute("account", new AccountForm());
		return "account/create";
	}

	/**
	 * アカウント情報の新規登録処理を実行する。
	 *
	 * @param form       入力されたアカウント情報フォーム
	 * @param result     バリデーション結果
	 * @param attributes リダイレクト時にメッセージを引き継ぐための属性
	 * @param model      画面描画用のモデル
	 * @return 成功時は一覧画面へのリダイレクト、失敗時は登録画面のテンプレートパス
	 */
	@PostMapping("/create")
	public String store(@Validated @ModelAttribute("account") AccountForm form,
			BindingResult result,RedirectAttributes attributes, Model model) {
		Account account = new Account();
		copyFormToEntity(form, account);
		if (result.hasErrors()) {
			return "account/create";
		}
		if (accountService.isLoginIdDuplicate(form.getLoginId(), null)) {
			model.addAttribute("loginIdError", "このログインIDは既に使用されています");
			return "account/create";
		}
		
		accountService.save(account);
		attributes.addFlashAttribute("toastMessage", "アカウント情報を登録しました");

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
	 * @param accountForm  入力されたアカウント情報フォーム
	 * @param result       バリデーション結果
	 * @param attributes   リダイレクト時にメッセージを引き継ぐための属性
	 * @return 成功時は一覧画面へのリダイレクト、失敗時は編集画面のテンプレートパス
	 */
	@PostMapping("/{id}/edit")
	public String update(@PathVariable("id") Integer id,
			@Validated @ModelAttribute("accountForm") AccountForm accountForm,
			BindingResult result,RedirectAttributes attributes) {

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
		attributes.addFlashAttribute("toastMessage", "アカウント情報を更新しました");
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
		employeeService.deleteByAccountId(id);
		assignmentService.deleteByAccountId(id);
		attributes.addFlashAttribute("toastMessage", "アカウント情報を削除しました");
		return "redirect:/accounts";
	}

	/**
	 * 選択された複数のアカウント情報を一括で物理削除する。
	 *
	 * @param ids        削除対象となるアカウントIDのリスト
	 * @param attributes リダイレクト時にメッセージを引き継ぐための属性
	 * @return 一覧画面へのリダイレクトパス
	 */
	@PostMapping("/bulk-delete")
	public String bulkDelete(@RequestParam(name = "ids", required = false) List<Integer> ids,
			RedirectAttributes attributes) {
		if (ids == null || ids.isEmpty()) {
			attributes.addFlashAttribute("toastError", "削除する対象が選択されていません");
			return "redirect:/accounts";
		}
		accountService.deleteBulk(ids);
		employeeService.deleteBulkByAccountId(ids);
		assignmentService.deleteBulkByAccountId(ids);
		attributes.addFlashAttribute("toastMessage", ids.size() + "件のアカウント情報を削除しました");
		return "redirect:/accounts";
	}
	
	/**
	 * アカウント情報のインポート画面を表示する。
	 *
	 * @param model 画面描画用のモデル
	 * @return インポート画面のテンプレートパス
	 */
	@GetMapping("/import")
	public String showImport(Model model) {
		return "account/import";
	}

	/**
	 * CSVファイルを用いたアカウント情報の一括インポート処理を実行する。
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
			model.addAttribute("fileError", "UTF-8のCSVファイルを選択してください。");
			return "account/import";
		}

		try {
			AccountService.ImportResult result = accountService.importCsv(file);
			model.addAttribute("importResult", result);
			if (result.limitError != null) {
				model.addAttribute("globalError", result.limitError);
			}
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
		String csv = "アカウントID,ログインID,パスワード,権限\n";

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
	 * アカウント情報のエクスポート画面を表示する。
	 *
	 * @param ids    エクスポート対象となるアカウントIDのリスト 
	 * @param model  画面描画用のモデル
	 * @return エクスポート画面のテンプレートパス
	 */
	@PostMapping("/export")
	public String showExport(@RequestParam(name = "ids", required = false) List<Integer> ids,
			Model model, RedirectAttributes attributes) {
		if (ids == null || ids.isEmpty()) {
			attributes.addFlashAttribute("toastError", "エクスポートする対象が選択されていません");
			return "redirect:/accounts";
		}
		List<Account> accounts = accountService.findByIds(ids);
		model.addAttribute("count", accountService.findByIds(ids).size());
		model.addAttribute("accounts", accounts);
		model.addAttribute("ids", ids);
		return "account/export";
	}

	/**
	 * 検索条件に合致するアカウント情報をCSV形式でダウンロードする。
	 *
	 * @param ids    エクスポート対象となるアカウントIDのリスト 
	 * @return ダウンロード用のCSVファイルバイナリデータ
	 */
	@PostMapping("/export/download")
	public ResponseEntity<byte[]> downloadCsv(
			@RequestParam(name = "ids", required = false) List<Integer> ids) {
		List<Account> accounts = accountService.findByIds(ids);
		StringBuilder csvBuilder = new StringBuilder("アカウントID,ログインID,パスワード,権限\n");
		for (Account acc : accounts) {
			String Permission = "";
			if(acc.getPermission() == 0) {
				Permission = "一般";
			}
			else {
				Permission = "管理";
			}
			csvBuilder.append(acc.getAccountId()).append(",")
					.append(acc.getLoginId()).append(",")
					.append(",")
					.append(Permission).append("\n");
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

	/**
	 * フォームオブジェクトからエンティティオブジェクトへプロパティを詰め替える。
	 *
	 * @param f 入力フォーム（コピー元）
	 * @param e エンティティ（コピー先）
	 */
	private void copyFormToEntity(AccountForm f, Account e) {
		e.setAccountId(f.getAccountId());
		e.setLoginId(f.getLoginId());
		e.setPasswordHash(f.getPasswordHash());
		e.setPermission(f.getPermission());
	}
}