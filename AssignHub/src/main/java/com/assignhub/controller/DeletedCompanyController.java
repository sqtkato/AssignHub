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

import com.assignhub.entity.DeletedCompany;
import com.assignhub.service.DeletedCompanyService;

@Controller
@RequestMapping("/deleted-companies")
public class DeletedCompanyController {
	private final DeletedCompanyService deletedCompanyService;

	public DeletedCompanyController(DeletedCompanyService deletedCompanyService) {
		this.deletedCompanyService = deletedCompanyService;
	}

	@GetMapping
	public String index(
			@RequestParam(name = "keyword", required = false) String keyword,
			@RequestParam(name = "tel", required = false) String tel,
			@RequestParam(name = "sort", defaultValue = "company_id") String sort,
			@RequestParam(name = "order", defaultValue = "asc") String order,
			Model model) {

		model.addAttribute("companies", deletedCompanyService.deletedfindAll(keyword, tel, sort, order));
		model.addAttribute("keyword", keyword);
		model.addAttribute("tel", tel);
		model.addAttribute("currentSort", sort);
		model.addAttribute("currentOrder", order);

		return "deleted_company/index";
	}

	// ==========================================
	// 復元処理
	// ==========================================

	@PostMapping("/{id}/restore")
	public String recover(@PathVariable("id") Integer id, RedirectAttributes attributes) {
		deletedCompanyService.restore(id);
		attributes.addFlashAttribute("toastMessage", "企業情報を復元しました");
		return "redirect:/deleted-companies";
	}

	@PostMapping("/restore-bulk")
	public String bulkRecover(@RequestParam(name = "ids", required = false) List<Integer> ids,
			RedirectAttributes attributes) {
		if (ids == null || ids.isEmpty()) {
			attributes.addFlashAttribute("toastError", "復元する対象が選択されていません");
			return "redirect:/deleted-companies";
		}
		deletedCompanyService.restoreBulk(ids);
		attributes.addFlashAttribute("toastMessage", ids.size() + "件の企業情報を復元しました");
		return "redirect:/deleted-companies";
	}

	// ==========================================
	// 物理削除処理
	// ==========================================

	@PostMapping("/{id}/delete")
	public String deleted(@PathVariable("id") Integer id, RedirectAttributes attributes) {
		if (deletedCompanyService.countEmployeesByCompanyId(id)) {
			attributes.addFlashAttribute("toastError", "紐づく社員情報が存在するため、物理削除できません。");
			return "redirect:/deleted-companies";
		}
		if (deletedCompanyService.countAssignmentsByCompanyId(id)) {
			attributes.addFlashAttribute("toastError", "紐づくアサイン履歴が存在するため、物理削除できません。");
			return "redirect:/deleted-companies";
		}

		deletedCompanyService.physicalDelete(id);
		attributes.addFlashAttribute("toastMessage", "企業情報を完全に削除しました");
		return "redirect:/deleted-companies";
	}

	@PostMapping("/delete-bulk")
	public String bulkDeleted(@RequestParam(name = "ids", required = false) List<Integer> ids,
			RedirectAttributes attributes) {
		if (ids == null || ids.isEmpty()) {
			attributes.addFlashAttribute("toastError", "削除対象が選択されていません");
			return "redirect:/deleted-companies";
		}

		if (deletedCompanyService.countEmployeesByCompanyIds(ids)) {
			attributes.addFlashAttribute("toastError", "紐づく社員情報が存在する企業が含まれているため、一括削除できません。");
			return "redirect:/deleted-companies";
		}
		if (deletedCompanyService.countAssignmentsByCompanyIds(ids)) {
			attributes.addFlashAttribute("toastError", "紐づくアサイン履歴が存在する企業が含まれているため、一括削除できません。");
			return "redirect:/deleted-companies";
		}

		deletedCompanyService.physicalDeleteBulk(ids);
		attributes.addFlashAttribute("toastMessage", ids.size() + "件の企業情報を完全に削除しました");
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

		List<DeletedCompany> targetCompanies = deletedCompanyService.deletedfindByIds(ids);

		model.addAttribute("count", targetCompanies.size());
		model.addAttribute("companies", targetCompanies);
		model.addAttribute("ids", ids);

		return "deleted_company/export";
	}

	@PostMapping("/export/download")
	public ResponseEntity<byte[]> downloadCsv(@RequestParam(name = "ids", required = false) List<Integer> ids) {
		if (ids == null || ids.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		List<DeletedCompany> delCompanies = deletedCompanyService.deletedfindByIds(ids);
		StringBuilder csvBuilder = new StringBuilder("企業ID,企業名,電話番号,住所,削除フラグ,作成日時,更新日時\n");

		for (DeletedCompany comp : delCompanies) {
			csvBuilder.append(comp.getCompId()).append(",")
					.append(comp.getCompName() != null ? comp.getCompName() : "").append(",")
					.append(comp.getCompTel() != null ? comp.getCompTel() : "").append(",")
					.append(comp.getCompAddress1() != null ? comp.getCompAddress1() : "").append(",")
					.append(comp.getDeleteFlag()).append(",")
					.append(comp.getCreatedAt() != null ? comp.getCreatedAt() : "").append(",")
					.append(comp.getUpdatedAt() != null ? comp.getUpdatedAt() : "").append("\n");
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
