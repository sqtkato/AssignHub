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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.assignhub.entity.Account;
import com.assignhub.form.AccountForm;
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
			Model model) {

        if (ids == null) {
            model.addAttribute("message", "対象が選択されていません");
            return "redirect:/accounts";
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

	@GetMapping("/{id}/edit")
	public String edit(@PathVariable("id") Integer id, Model model) {
		if (!model.containsAttribute("accountForm")) {
			Account acc = accountService. findById(id);;
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


		Account acc = new Account();
		acc.setAccountId(id);
		copyFormToEntity(accountForm, acc);
		accountService.update(acc);
		return "redirect:/accounts";
	}
	
	

	

	private void copyFormToEntity(AccountForm f, Account e) {
		e.setAccountId(f.getAccountId());
		e.setLoginId(f.getLoginId());
		e.setPasswordHash(f.getPasswordHash());
		e.setPermission(f.getPermission());
	}
}
