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

	/**
	 * コンストラクタによる依存性の注入。
	 *
	 * @param companyService 企業サービス
	 */
	public AccountController(AccountService accountService) {
		this.accountService = accountService;
	}

	/**
	 * アカウント一覧画面を表示する。検索・ソート条件に応じたデータを取得する。
	 *
	 * @param keyword 検索キーワード（任意）
	 * @param sort ソート対象のカラム名（デフォルト: company_id）
	 * @param order ソート順（デフォルト: asc）
	 * @param model 画面描画用モデル
	 * @return 一覧画面のテンプレートパス
	 */
	@GetMapping
	public String index(@RequestParam(name = "keyword", required = false) String keyword,
			@RequestParam(name = "sort", defaultValue = "login_id") String sort,
			@RequestParam(name = "order", defaultValue = "asc") String order, Model model,
			@RequestParam(name = "permission", required = false) Integer permission,HttpSession session) {
		model.addAttribute("accounts", accountService.findAll(keyword, sort, order, permission));
		model.addAttribute("keyward", keyword);
		model.addAttribute("currentSort", sort);
		model.addAttribute("currentOrder", order);
        model.addAttribute("currentLoginId",session.getAttribute("loginId"));

		return "account/index";
	}
	
	@GetMapping("/new")
	public String newAccount(Model model, HttpSession session) {
        model.addAttribute("currentLoginId",session.getAttribute("loginId"));
		model.addAttribute("account", new AccountForm());
		return "account/create";
	}

	@PostMapping("/create")
	public String create(@Validated @ModelAttribute("account") AccountForm form,
			BindingResult result, Model model, HttpSession session) {
        model.addAttribute("currentLoginId",session.getAttribute("loginId"));
		if (result.hasErrors()) {
			return "account/create";
		}
		if (accountService.existsByLoginId(form.getLoginId())) {
			model.addAttribute("loginIdError", "このログインIDは既に使用されています");
			return "account/create";
		}
		Account account = new Account();
		copyFormToEntity(form, account);
		accountService.save(account);

		return "redirect:/accounts";

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
			Model model, HttpSession session, RedirectAttributes attributes) {
		
        model.addAttribute("currentLoginId",session.getAttribute("loginId"));
		// ★【最優先】まず最初にnullチェックを行う
		if (ids == null || ids.isEmpty()) {
			attributes.addFlashAttribute("toastError", "エクスポートする対象が選択されていません");
			return "redirect:/accounts"; // 元の一覧画面に戻す
		}
		if (ids.size() == 0) {
			return "account/index";

		}
		model.addAttribute("count", accountService.findByIds(ids).size());
		List<Account> accounts = accountService.findByIds(ids);
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
		headers.add("Content-Disposition", "attachment; filename=account.csv");
		headers.add("Content-Type", "text/csv; charset=UTF-8");
		return new ResponseEntity<>(result, headers, HttpStatus.OK);
	}

	// ===== ここから アカウント情報インポート機能 =====

	/**
	 * アカウント情報インポート画面を表示する。
	 */
	@GetMapping("/import")
	public String importPage(HttpSession session,Model model) {
        model.addAttribute("currentLoginId",session.getAttribute("loginId"));
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

		// No.4 文字コードチェック（UTF-8で読めるか試す）
				try {
					java.nio.charset.CharsetDecoder decoder =
							java.nio.charset.StandardCharsets.UTF_8.newDecoder();
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
	 * アカウントを一件論理削除
	 *
	 * @param id 削除対象のアカウントID
	 * @return 一覧画面へのリダイレクトパス
	 */
	@PostMapping("/{id}/delete")
	public String delete(@PathVariable("id") Integer id, RedirectAttributes attributes) {
		accountService.delete(id);
		return "redirect:/accounts";
	
	/**
	 * 選択された複数の社員情報を一括で物理削除する。
	 *
	 * @param ids        削除対象となるアカウントIDのリスト
	 * @param attributes リダイレクト時にメッセージを引き継ぐための属性
	 * @return 一覧画面へのリダイレクト
	 */
	}

	@PostMapping("/bulk-delete")
	public String bulkDelete(@RequestParam(name = "ids", required = false) List<Integer> ids,
			RedirectAttributes attributes) {
		if (ids == null || ids.isEmpty()) {
			attributes.addFlashAttribute("toastError", "削除する対象が選択されていません");
			return "redirect:/accounts";
		}
		accountService.deleteBulk(ids);
		attributes.addFlashAttribute("toastMessage", ids.size() + "件のアカウント情報を削除しました");
		return "redirect:/accounts";
	}

	@GetMapping("/{id}/edit")
	public String edit(@PathVariable("id") Integer id, HttpSession session, Model model) {
		if (!model.containsAttribute("accountForm")) {
	        model.addAttribute("currentLoginId",session.getAttribute("loginId"));
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

	@PostMapping("/{id}/edit")
	public String update(@PathVariable("id") Integer id,
			@Validated @ModelAttribute("accountForm") AccountForm accountForm,
			BindingResult result, RedirectAttributes attributes, Model model) {

		if (result.hasErrors()) {
			return "account/edit";
		}
		
		if (accountService.existsByLoginIdUpdate(accountForm.getLoginId(), id)) {
			model.addAttribute("loginId", "このログインIDは既に使用されています");
			return "account/edit";
		}
	
		Account acc = new Account();
		// :bulb: 画面から届いたデータを、DBに送るオブジェクトにしっかりセットする！
	    acc.setLoginId(accountForm.getLoginId());
	    acc.setPermission(accountForm.getPermission());
	    // パスワードの入力がある場合のみハッシュ化してセット（空なら変更しない等の制御は必要に応じて）
	    acc.setPasswordHash(accountForm.getPasswordHash());
	    acc.setAccountId(id);
		accountService.save(acc);
		return "redirect:/accounts";
	}

	private void copyFormToEntity(AccountForm f, Account e) {
		e.setAccountId(f.getAccountId());
		e.setLoginId(f.getLoginId());
		e.setPasswordHash(f.getPasswordHash());
		e.setPermission(f.getPermission());
	}
}