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
		model.addAttribute("account", new AccountForm());
		return "account/create";
	}

	@PostMapping("/create")
	public String create(@Validated @ModelAttribute("account") AccountForm form,
			BindingResult result, Model model) {

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

	@PostMapping("/export")
	public String showExport(@RequestParam(name = "ids", required = false) List<Integer> ids,
			Model model, RedirectAttributes attributes) {
		if (ids == null || ids.isEmpty()) {
			attributes.addFlashAttribute("toastError", "エクスポートする対象が選択されていません");
			return "redirect:/accounts";
		}
		model.addAttribute("count", accountService.findByIds(ids).size());
		List<Account> accounts = accountService.findByIds(ids);
		model.addAttribute("accounts", accounts);
		model.addAttribute("ids", ids);
		return "account/export";
	}

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

	@GetMapping("/import")
	public String showImport() {
		return "account/import";
	}

	@PostMapping("/import")
	public String importCsv(@RequestParam("file") MultipartFile file, Model model) {
		String filename = (file != null) ? file.getOriginalFilename() : null;
		if (file == null || file.isEmpty()
				|| filename == null || !filename.toLowerCase().endsWith(".csv")) {
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

	@GetMapping("/import/template")
	public ResponseEntity<byte[]> downloadTemplate() {
		String csvContent = "アカウントID(新規は空欄),ログインID,パスワード\n";
		byte[] csvBytes = csvContent.getBytes(StandardCharsets.UTF_8);
		byte[] bom = new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
		byte[] result = new byte[bom.length + csvBytes.length];
		System.arraycopy(bom, 0, result, 0, bom.length);
		System.arraycopy(csvBytes, 0, result, bom.length, csvBytes.length);

		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "attachment; filename=account_template.csv");
		headers.add("Content-Type", "text/csv; charset=UTF-8");
		return new ResponseEntity<>(result, headers, HttpStatus.OK);
	}

	@PostMapping("/{id}/delete")
	public String delete(@PathVariable("id") Integer id, RedirectAttributes attributes) {
		accountService.delete(id);
		return "redirect:/accounts";
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