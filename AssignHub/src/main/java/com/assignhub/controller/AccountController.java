package com.assignhub.controller;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.assignhub.entity.Account;
import com.assignhub.form.ImportError;
import com.assignhub.service.AccountService;

/**
 * 企業管理機能の画面遷移およびHTTPリクエストを処理するコントローラー。
 *
 * @version 1.03 2026/06/01
 * @author SQT）チームC
 */
@Controller
@RequestMapping("/accounts")
public class AccountController {

	private final AccountService accountService;

	public AccountController(AccountService accountService) {
		this.accountService = accountService;
	}

	@GetMapping
	public String index(@RequestParam(name = "keyword", required = false) String keyword,
			@RequestParam(name = "sort", defaultValue = "login_id") String sort,
			@RequestParam(name = "order", defaultValue = "asc") String order, Model model,
			@RequestParam(name = "permission", required = false) Integer permission) {
		model.addAttribute("accounts", accountService.findAll(keyword, sort, order, permission));
		model.addAttribute("keyward", keyword);
		model.addAttribute("currentSort", sort);
		model.addAttribute("currentOrder", order);
		return "account/index";
	}

	@GetMapping("/new")
	public String newAccount(Model model) {
		model.addAttribute("account", new Account());
		return "account/new";
	}

	@PostMapping("/create")
	public String create(@ModelAttribute Account account, Model model) {
		if (accountService.existsByLoginId(account.getLoginId())) {
			model.addAttribute("loginIdError", "このログインIDは既に使用されています");
			return "account/new";
		}
		accountService.save(account);
		return "redirect:/accounts";
	}

	/**
	 * 社員データのエクスポート画面を表示する。
	 */
	@GetMapping("/export")
	public String showExport(@RequestParam(name = "ids", required = false) List<Integer> ids,
			Model model) {
		System.out.println(ids);
		model.addAttribute("count", accountService.findByIds(ids).size());
		model.addAttribute("ids", ids);
		return "account/export";
	}

	/**
	 * 検索条件に合致する社員データをCSV形式でダウンロードする。
	 */
	@GetMapping("/export/download")
	public ResponseEntity<byte[]> downloadCsv(
			@RequestParam(name = "ids", required = false) List<Integer> ids) {
		List<Account> accounts = accountService.findByIds(ids);
		StringBuilder csvBuilder = new StringBuilder("アカウントID,ログインID,権限,社員名\n");
		for (Account acc : accounts) {
			csvBuilder.append(acc.getAccountId()).append(",")
					.append(acc.getLoginId()).append(",")
					.append(acc.getPermission()).append(",")
					.append(acc.getEmpName()).append("\n");
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

	// ===== ここから アカウント情報インポート機能 =====

	/**
	 * アカウント情報インポート画面を表示する。
	 */
	@GetMapping("/import")
	public String importPage() {
		return "account/import";
	}

	/**
	 * CSVファイルをアップロードしてアカウント情報を一括登録・更新する。
	 */
	@PostMapping("/import")
	public String doImport(@RequestParam("file") MultipartFile file, Model model) {
		model.addAttribute("done", true);

		// 没选文件
		if (file == null || file.isEmpty()) {
			model.addAttribute("fileError", "ファイルを選択してください");
			return "account/import";
		}

		// No.2 非CSV文件
		String filename = file.getOriginalFilename();
		if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
			model.addAttribute("fileError", "ファイル形式が正しくありません。.csvファイルを選択してください。");
			return "account/import";
		}

		// No.3 超过5MB
		if (file.getSize() > 5 * 1024 * 1024) {
			model.addAttribute("fileError", "ファイルサイズは5MB以内にしてください。");
			return "account/import";
		}

		try {
			int total = accountService.countDataRows(file);

			// No.5 超过500件
			if (total > 500) {
				model.addAttribute("globalError", "登録後の件数が上限に達しています。アカウント登録条件は500件です。");
				model.addAttribute("successCount", 0);
				model.addAttribute("errorCount", total);
				return "account/import";
			}

			// ① 先校验（No.6〜13、No.7存在チェック、権限チェック）
			List<ImportError> errors = accountService.validate(file);

			if (!errors.isEmpty()) {
				// 有错 → 全部取消，不写DB
				model.addAttribute("successCount", 0);
				model.addAttribute("errorCount", errors.size());
				model.addAttribute("errors", errors);
				return "account/import";
			}

			// ② 校验全通过 → 写入DB（INSERT/UPDATE）
			List<ImportError> dbErrors = accountService.importData(file);

			if (!dbErrors.isEmpty()) {
				// No.14 写入有失败
				model.addAttribute("successCount", total - dbErrors.size());
				model.addAttribute("errorCount", dbErrors.size());
				model.addAttribute("errors", dbErrors);
			} else {
				// 全部成功
				model.addAttribute("successCount", total);
				model.addAttribute("errorCount", 0);
			}
		} catch (Exception e) {
			model.addAttribute("fileError", "ファイルの読み込みに失敗しました");
		}
		return "account/import";
	}

	/**
	 * CSVテンプレート（見本）をダウンロードする。
	 */
	@GetMapping("/import/template")
	public ResponseEntity<byte[]> downloadTemplate() {
		String csv = "アカウントID,ログインID,パスワード,権限\n"
				   + ",user001,pass1234,0\n";

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

}