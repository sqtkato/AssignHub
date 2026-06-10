package com.assignhub.controller;

<<<<<<< HEAD
import java.util.List;

=======
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
>>>>>>> branch '侃々諤々' of https://github.com/sqtkato/AssignHub.git
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.assignhub.entity.Company;
import com.assignhub.entity.DeletedAccount;
import com.assignhub.form.CompanyForm;
import com.assignhub.service.DeletedAccountService;

@Controller
@RequestMapping("/companies")
public class DeletedAccountController {
	private final DeletedAccountService deletedAccountService;

	/**
	 * コンストラクタによる依存性の注入。
	 *
	 * @param companyService 企業サービス
	 */
	public DeletedAccountController(DeletedAccountService deletedAccountService) {
		this.deletedAccountService = deletedAccountService;
	}

	/**
	 * 企業一覧画面を表示する。検索・ソート条件に応じたデータを取得する。
	 *
	 * @param keyword 検索キーワード（任意）
	 * @param sort ソート対象のカラム名（デフォルト: company_id）
	 * @param order ソート順（デフォルト: asc）
	 * @param model 画面描画用モデル
	 * @return 一覧画面のテンプレートパス
	 */
	@GetMapping
	public String index(@RequestParam(name = "keyword", required = false) String keyword,
			@RequestParam(name = "sort", defaultValue = "company_id") String sort,
			@RequestParam(name = "order", defaultValue = "asc") String order, Model model) {
		model.addAttribute("companies", deletedAccountService.findAll(keyword, sort, order));
		model.addAttribute("keyward", keyword);
		model.addAttribute("currentSort", sort);
		model.addAttribute("currentOrder", order);
		return "company/index";
	}

	/**
	 * 企業の新規登録画面を表示する。
	 *
	 * @param model 画面描画用モデル
	 * @return 新規登録画面のテンプレートパス
	 */
	@GetMapping("/new")
	public String create(Model model) {
		if (!model.containsAttribute("companyForm")) {
			model.addAttribute("companyForm", new CompanyForm());
		}
		return "company/create";
	}

	/**
	 * 入力された企業情報をデータベースに登録する。
	 *
	 * @param form 入力フォームデータ
	 * @param result バリデーション結果
	 * @param attributes リダイレクト先へ渡すフラッシュスコープ
	 * @return 成功時は一覧画面へリダイレクト、失敗時は登録画面へ戻る
	 */
	@PostMapping
	public String store(@Validated @ModelAttribute("companyForm") CompanyForm form, BindingResult result,
			RedirectAttributes attributes) {
		if (result.hasErrors()) {
			return "company/create";
		}
		Company company = new Company();
		copyFormToEntity(form, company);
		deletedaccountService.save(company);
		attributes.addFlashAttribute("toastMessage", "企業情報を登録しました");
		return "redirect:/companies";
	}

	/**
	 * フォームオブジェクトからエンティティオブジェクトへ値の詰め替えを行う。
	 *
	 * @param f 入力フォーム
	 * @param e 更新対象のエンティティ
	 */
	private void copyFormToEntity(CompanyForm f, Company e) {
		e.setCompanyName(" ");
	}
	
	
	
//一覧
	@GetMapping
	public String index(@RequestParam(name = "keyword", required = false) String keyword,
			@RequestParam(name = "sort", defaultValue = "company_id") String sort,
			@RequestParam(name = "order", defaultValue = "asc") String order, Model model) {
		model.addAttribute("companies", deletedAccountService.findAll(keyword, sort, order));
		model.addAttribute("keyward", keyword);
		model.addAttribute("currentSort", sort);
		model.addAttribute("currentOrder", order);
		return "deletedAccount/index";
	}

	
//一つ復元
	@GetMapping
	public String recover(@RequestParam(name = "ids", required = false) List<Integer> ids,
				RedirectAttributes attributes) {
			if (ids == null || ids.isEmpty()) {
				attributes.addFlashAttribute("toastError", "");
				return "deletedAccount/recover";
			}
			DeletedAccountService.deleteBulk(ids);
			attributes.addFlashAttribute("toastMessage", ids.size() + "");
			return "deletedAccount/recover";}

//	一つ削除
	@GetMapping
	public String deleted(@RequestParam(name = "ids", required = false) List<Integer> ids,
				RedirectAttributes attributes) {
			if (ids == null || ids.isEmpty()) {
				attributes.addFlashAttribute("toastError", "");
				return "deletedAccount/deleted";
			}
	
			DeletedAccountService.deleteBulk(ids);
			attributes.addFlashAttribute("toastMessage", ids.size() + "");
			return "deletedAccount/deleted";	
		}
		

//	一括復元
	@GetMapping
	public String bulkRecover(@RequestParam(name = "ids", required = false) List<Integer> ids,
				RedirectAttributes attributes) {
			if (ids == null || ids.isEmpty()) {
				attributes.addFlashAttribute("toastError", "復元する対象が選択されていません");
				return "deletedAccount/bulkRecover";
			}
			DeletedAccountService.deleteBulk(ids);
			attributes.addFlashAttribute("toastMessage", ids.size() + "");
			return "deletedAccount/bulkRecover";	
		}
	
//一括削除
	@GetMapping
	public String bulkDeleted(@RequestParam(name = "ids", required = false) List<Integer> ids,
				RedirectAttributes attributes) {
			if (ids == null || ids.isEmpty()) {
				attributes.addFlashAttribute("toastError", "削除対象が選択されていません");
				return "deletedAccount/bulkDeleted";
			}
			DeletedAccountService.deleteBulk(ids);
			attributes.addFlashAttribute("toastMessage", ids.size() + "");
			return "deletedAccount/bulkDeleted";
			}
	
//	エクスポート画面へ遷移
	@PostMapping
	public String exportConfirm() {
		return "deletedAccount/export";
	}
	
	@GetMapping("/export")
	public String showExport(@RequestParam(name = "keyword", required = false) String keyword, 
			@RequestParam(name = "deptId", required = false) Integer deptId, Model model) {
		//引数内書き換え
		model.addAttribute("count", DeletedAccountService.findAll(keyword, deptId, "rooky_id", "asc").size());
		model.addAttribute("keyword", keyword);
		model.addAttribute("deptId", deptId);
		return "deletedAccount/export";
	}
	
//　エクスポートのダウンロード処理
	@GetMapping("/export/download")
	public ResponseEntity<byte[]> downloadCsv(
			@RequestParam(name = "keyword", required = false) String keyword, 
			@RequestParam(name = "deptId", required = false) Integer deptId) {
		List<DeletedAccount> delAccount = deletedAccountService.findAll(keyword,deptId, "rooky_id", "asc");
		//引数名書き換え
		StringBuilder csvBuilder = new StringBuilder("ID,社員名,部署ID,電話番号,メールアドレス\n");
		//括弧内書き換え
		for (DeletedAccount delAcc : delAccount) {
			csvBuilder.append(delAcc.getRookyId()).append(",")
					.append(delAcc.getRookyName()).append(",")
					.append(delAcc.getDeptId()).append(",")
					.append(delAcc.getTelNumber() != null ? rook.getTelNumber() : "").append(",")
					.append(delAcc.getEmailAddress()).append("\n");
		}
		byte[] csvBytes = csvBuilder.toString().getBytes(StandardCharsets.UTF_8);
		byte[] bom = new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
		byte[] result = new byte[bom.length + csvBytes.length];
		System.arraycopy(bom, 0, result, 0, bom.length);
		System.arraycopy(csvBytes, 0, result, bom.length, csvBytes.length);

		HttpHeaders headers = new HttpHeaders();
		//filename変更必要
		headers.add("Content-Disposition", "attachment; filename=deletedAccount.csv");
		headers.add("Content-Type", "text/csv; charset=UTF-8");
		return new ResponseEntity<>(result, headers, HttpStatus.OK);
	}

}
