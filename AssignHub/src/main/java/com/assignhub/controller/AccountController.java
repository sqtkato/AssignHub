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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
			@RequestParam(name = "permission", required = false) Integer permission) {
		model.addAttribute("accounts", accountService.findAll(keyword, sort, order, permission));
		model.addAttribute("keyward", keyword);
		model.addAttribute("currentSort", sort);
		model.addAttribute("currentOrder", order);
		return "account/index";
	}

	@GetMapping("/new")
	public String newAccount(Model model) {
		// 【重要】ここで「account」という名前で空のオブジェクトを渡す！
		model.addAttribute("account", new Account());
		return "account/new"; // ここがHTMLのファイル名と一致しているか
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

	@PostMapping("/export")
	public String showExport(@RequestParam(name = "ids", required = false) List<Integer> ids,
			Model model, RedirectAttributes attributes) {
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
		headers.add("Content-Disposition", "attachment; filename=employees.csv");
		headers.add("Content-Type", "text/csv; charset=UTF-8");
		return new ResponseEntity<>(result, headers, HttpStatus.OK);
	}

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

		if (file == null || file.isEmpty()) {
			model.addAttribute("fileError", "ファイルを選択してください");
			return "account/import";
		}

		try {
			int total = accountService.countDataRows(file);

			if (total > 500) {
				model.addAttribute("globalError", "登録件数が上限（500件）に達しています");
				model.addAttribute("successCount", 0);
				model.addAttribute("errorCount", total);
				return "account/import";
			}

			List<ImportError> errors = accountService.validate(file);

			if (!errors.isEmpty()) {
				model.addAttribute("successCount", 0);
				model.addAttribute("errorCount", errors.size());
				model.addAttribute("errors", errors);
			} else {
				model.addAttribute("successCount", total);
				model.addAttribute("errorCount", 0);
			}
		} catch (Exception e) {
			model.addAttribute("fileError", "ファイルの読み込みに失敗しました");
		}
		return "account/import";
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
		attributes.addFlashAttribute("toastMessage", ids.size() + "件のアカウント情報を削除しました");
		return "redirect:/accounts";
	}

}