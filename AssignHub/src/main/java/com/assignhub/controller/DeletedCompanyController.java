package com.assignhub.controller;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.assignhub.entity.Company;
import com.assignhub.service.DeletedCompanyService;

/**
 * 論理削除された企業情報の管理、復元、物理削除、エクスポートを処理するコントローラー。
 * @author USYS) 北田
 */
@Controller
@RequestMapping("/deleted-companies")
public class DeletedCompanyController {
	private final DeletedCompanyService deletedCompanyService;
	/**
	 * コンストラクタによる依存性の注入。
	 *
	 * @param deletedCompanyService 論理削除済み企業管理サービス
	 */
	public DeletedCompanyController(DeletedCompanyService deletedCompanyService) {
		this.deletedCompanyService = deletedCompanyService;
	}
	
	/**
	 * 論理削除済み企業一覧画面を表示する。検索条件に応じたデータを取得する
	 * @param companyName 企業名検索キーワード（任意）
	 * @param companyTel 電話番号検索キーワード（任意）
	 * @param model   画面描画用モデル
	 * @return 一覧画面のテンプレートパス
	 */
	@GetMapping
	public String index(@RequestParam(name = "companyName", required = false) String companyName,
			@RequestParam(name = "companyTel", required = false) String companyTel,
			Model model) {
		model.addAttribute("companies", deletedCompanyService.findAll(companyName, companyTel));
		model.addAttribute("companyName", companyName);
		model.addAttribute("companyTel", companyTel);
		return "deleted_company/index";
	}
	/**
	 * 論理削除済み企業を一件復元する。
	 *
	 * @param id 復元対象の企業ID
	 * @return 一覧画面へのリダイレクトパス
	 */
	@PostMapping("/{id}/restore")
	public String recover(@PathVariable("id") Integer id, RedirectAttributes attributes) {
		if (deletedCompanyService.isCompanyLimitReachedAfterRestore(1)) {
			attributes.addFlashAttribute("toastError", "登録件数が上限（500件）に達するため、復元できません。");
			return "redirect:/deleted-companies";
		}
		if (deletedCompanyService.isCompanyIdDuplicate(id)) {
			attributes.addFlashAttribute("toastError", "企業名、電話番号、FAX番号は既に使用されています。");
			return "redirect:/deleted-companies";
		}
		deletedCompanyService.restore(id);
		return "redirect:/deleted-companies";
	}
	/**
	 * 選択された複数の論理削除済み企業情報を一括で復元する。
	 *
	 * @param ids        復元対象となる企業IDのリスト
	 * @param attributes リダイレクト時にメッセージを引き継ぐための属性
	 * @return 一覧画面へのリダイレクトパス
	 */
	@PostMapping("/bulk-restore")
	public String bulkRecover(@RequestParam(name = "ids", required = false) List<Integer> ids,
			 RedirectAttributes attributes) {
		if (ids == null || ids.isEmpty()) {
			attributes.addFlashAttribute("toastError", "復元対象が選択されていません");
			return "redirect:/deleted-companies";
		}

		if (deletedCompanyService.isCompanyLimitReachedAfterRestore(ids.size())) {
			attributes.addFlashAttribute("toastError", "復元後の件数が上限に達しています。企業情報の登録上限は500件です。");
			return "redirect:/deleted-companies";
		}
		if (deletedCompanyService.isCompanyIdDuplicate(ids)) {
			attributes.addFlashAttribute("toastError", "企業名、電話番号、FAX番号は既に使用されています。");
			return "redirect:/deleted-companies";
		}

		deletedCompanyService.restoreBulk(ids);
		return "redirect:/deleted-companies";
	}
	/**
	 * 論理削除済み企業を一件物理削除する。
	 *
	 * @param id 削除対象の企業ID
	 * @return 一覧画面へのリダイレクトパス
	 */
	@PostMapping("/{id}/delete")
	public String delete(@PathVariable("id") Integer id, RedirectAttributes attributes) {
		if (deletedCompanyService.existEmployeesByCompanyId(id)) {
			attributes.addFlashAttribute("toastError", "紐づく社員情報（パートナー）が存在するため、削除できません。先に社員情報を削除してください。");
			return "redirect:/deleted-companies";
		}
		if (deletedCompanyService.existAssignmentsByCompanyId(id)) {
			attributes.addFlashAttribute("toastError", "紐づくアサイン履歴情報が存在するため、削除できません。先にアサイン履歴情報を削除してください。");
			return "redirect:/deleted-companies";
		}

		deletedCompanyService.physicalDelete(id);
		return "redirect:/deleted-companies";
	}	
	/**
	 * 選択された複数の論理削除済み企業情報を一括で物理削除する。
	 *
	 * @param ids        削除対象となる企業IDのリスト
	 * @param attributes リダイレクト時にメッセージを引き継ぐための属性
	 * @return 一覧画面へのリダイレクトパス
	 */
	@PostMapping("/bulk-delete")
	public String bulkDelete(@RequestParam(name = "ids", required = false) List<Integer> ids,
			RedirectAttributes attributes) {
		if (ids == null || ids.isEmpty()) {
			attributes.addFlashAttribute("toastError", "削除対象が選択されていません");
			return "redirect:/deleted-companies";
		}

		if (deletedCompanyService.existEmployeesByCompanyIds(ids)) {
			attributes.addFlashAttribute("toastError", "紐づく社員情報（パートナー）が存在するため、削除できません。先に社員情報を削除してください。");
			return "redirect:/deleted-companies";
		}
		if (deletedCompanyService.existAssignmentsByCompanyIds(ids)) {
			attributes.addFlashAttribute("toastError", "紐づくアサイン履歴情報が存在するため、削除できません。先にアサイン履歴情報を削除してください。");
			return "redirect:/deleted-companies";
		}

		deletedCompanyService.physicalDeleteBulk(ids);
		return "redirect:/deleted-companies";
	}
	 /**
	 * 論理削除済み企業情報のエクスポート画面を表示する。
	 *
	 * @param ids    エクスポート対象となる企業IDのリスト 
	 * @param model  画面描画用のモデル
	 * @return エクスポート画面のテンプレートパス
	 */
	@PostMapping("/export")
	public String showExport(@RequestParam(name = "ids", required = false) List<Integer> ids, Model model,
			RedirectAttributes attributes) {
		if (ids == null || ids.isEmpty()) {
			attributes.addFlashAttribute("toastError", "エクスポートする対象が選択されていません");
			return "redirect:/deleted-companies";
		}

		List<Company> Companies = deletedCompanyService.findByIds(ids);

		model.addAttribute("count", Companies.size());
		model.addAttribute("companies", Companies);
		model.addAttribute("ids", ids);

		return "deleted_company/export";
	}
	/**
	 * 検索条件に合致する論理削除済み企業情報をCSV形式でダウンロードする。
	 *
	 * @param ids    エクスポート対象となる企業IDのリスト 
	 * @return ダウンロード用のCSVファイルバイナリデータ
	 */
	@PostMapping("/export/download")
	public ResponseEntity<byte[]> downloadCsv(@RequestParam(name = "ids", required = false) List<Integer> ids) {

		List<Company> delCompanies = deletedCompanyService.findByIds(ids);
		StringBuilder csvBuilder = new StringBuilder(
				"作成日時,更新日時,企業ID,企業名,企業名カナ,設立年度,社員数,郵便番号,住所1,住所2,TEL,FAX,代表者 姓,代表者 名,代表者 姓：カナ,代表者 名：カナ\n");

		for (Company comp : delCompanies) {
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
		headers.add("Content-Disposition", "attachment; filename=deleted-companies.csv");
		headers.add("Content-Type", "text/csv; charset=UTF-8");
		return new ResponseEntity<>(result, headers, HttpStatus.OK);
	}

}
