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

@Controller
@RequestMapping("/deleted-companies")
public class DeletedCompanyController {
	private final DeletedCompanyService deletedCompanyService;

	public DeletedCompanyController(DeletedCompanyService deletedCompanyService) {
		this.deletedCompanyService = deletedCompanyService;
	}

	@GetMapping
	public String index(@RequestParam(name = "companyName", required = false) String companyName,
			@RequestParam(name = "companyTel", required = false) String companyTel,
			Model model) {
		model.addAttribute("companies", deletedCompanyService.findAll(companyName, companyTel));
		model.addAttribute("companyName", companyName);
		model.addAttribute("companyTel", companyTel);
		return "deleted_company/index";
	}

	// ==========================================
	// 復元処理
	// ==========================================

	@PostMapping("/{id}/restore")
	public String recover(@PathVariable("id") Integer id, RedirectAttributes attributes) {
		if (deletedCompanyService.isCompanyLimitReachedAfterRestore(1)) {
			attributes.addFlashAttribute("toastError", "登録件数が上限（500件）に達するため、復元できません。");
			return "redirect:/deleted-companies";
		}
		deletedCompanyService.restore(id);
		return "redirect:/deleted-companies";
	}

	@PostMapping("/bulk-restore")
	public String bulkRecover(@RequestParam(name = "ids", required = false) List<Integer> ids,
			RedirectAttributes attributes) {
		if (ids == null || ids.isEmpty()) {
			attributes.addFlashAttribute("toastError", "復元する対象が選択されていません");
			return "redirect:/deleted-companies";
		}
		
		if (deletedCompanyService.isCompanyLimitReachedAfterRestore(ids.size())) {
			attributes.addFlashAttribute("toastError", "復元後の件数が上限に達しています。企業情報の登録上限は500件です。");
			return "redirect:/deleted-companies";
		}
		deletedCompanyService.restoreBulk(ids);
		return "redirect:/deleted-companies";
	}

	// ==========================================
	// 物理削除処理
	// ==========================================

	@PostMapping("/{id}/delete")
	public String deleted(@PathVariable("id") Integer id, RedirectAttributes attributes) {
		if (deletedCompanyService.existEmployeesByCompanyId(id)) {
			attributes.addFlashAttribute("toastError", "紐づく社員情報が存在するため、物理削除できません。");
			return "redirect:/deleted-companies";
		}
		if (deletedCompanyService.existAssignmentsByCompanyId(id)) {
			attributes.addFlashAttribute("toastError", "紐づくアサイン履歴が存在するため、物理削除できません。");
			return "redirect:/deleted-companies";
		}

		deletedCompanyService.physicalDelete(id);
		return "redirect:/deleted-companies";
	}

	@PostMapping("/bulk-delete")
	public String bulkDeleted(@RequestParam(name = "ids", required = false) List<Integer> ids,
			RedirectAttributes attributes) {
		if (ids == null || ids.isEmpty()) {
			attributes.addFlashAttribute("toastError", "削除対象が選択されていません");
			return "redirect:/deleted-companies";
		}

		if (deletedCompanyService.existEmployeesByCompanyIds(ids)) {
			attributes.addFlashAttribute("toastError", "紐づく社員情報が存在する企業が含まれているため、一括削除できません。");
			return "redirect:/deleted-companies";
		}
		if (deletedCompanyService.existAssignmentsByCompanyIds(ids)) {
			attributes.addFlashAttribute("toastError", "紐づくアサイン履歴が存在する企業が含まれているため、一括削除できません。");
			return "redirect:/deleted-companies";
		}

		deletedCompanyService.physicalDeleteBulk(ids);
		return "redirect:/deleted-companies";
	}

	// ==========================================
	// エクスポート処理
	// ==========================================

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
		headers.add("Content-Disposition", "attachment; filename=deletedCompany.csv");
		headers.add("Content-Type", "text/csv; charset=UTF-8");
		return new ResponseEntity<>(result, headers, HttpStatus.OK);
	}

}
