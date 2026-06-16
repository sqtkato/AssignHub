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

import com.assignhub.entity.Assignment;
import com.assignhub.form.SearchForm;
import com.assignhub.service.DeletedAssignService;

@Controller
@RequestMapping("/deleted-assignments")
public class DeletedAssignController {
	private final DeletedAssignService deletedAssignService;

	public DeletedAssignController(DeletedAssignService deletedAssignService) {
		this.deletedAssignService = deletedAssignService;
	}

	@GetMapping
	public String index(@Validated @ModelAttribute("searchForm") SearchForm searchForm,
			BindingResult result,
			Model model) {

		java.time.LocalDate startDate = searchForm.getContractStartDate();
		java.time.LocalDate endDate = searchForm.getContractEndDate();

		String toastError = null;
		if (result.hasErrors()) {
			toastError = "契約開始日は契約終了日以前の日付を入力してください。";
		}

		if (toastError != null) {
			model.addAttribute("toastError", toastError);
			// エラー時は全件（または空）を取得
			model.addAttribute("assignments", deletedAssignService.findAll(null, null, null, null, null));
			return "deleted_assign/index";
		}

		// 検索結果を取得してモデルに格納
		model.addAttribute("assignments", deletedAssignService.findAll(
				searchForm.getEmpName(),
				searchForm.getAssignName(),
				searchForm.getCompanyName(),
				startDate == null ? null : startDate.toString(),
				endDate == null ? null : endDate.toString()));

		return "deleted_assign/index";
	}
	// ==========================================
	// 復元処理
	// ==========================================

	/* 単一復元 */
	@PostMapping("/{id}/restore")
	public String recover(@PathVariable("id") Integer id, RedirectAttributes attributes) {
		if (deletedAssignService.countcompanysdispatchsByAssignId(id)) {
			attributes.addFlashAttribute("toastError", "紐づく派遣先企業が削除状態のため、復元できません。先に該当する企業情報を復元してください。");
			return "redirect:/deleted-assignments";
		}

		if (deletedAssignService.countcompanyspartnerByAssignId(id)) {
			attributes.addFlashAttribute("toastError", "紐づく所属元企業が削除状態のため、復元できません。先に該当する企業情報を復元してください。");
			return "redirect:/deleted-assignments";
		}

		if (deletedAssignService.countEmployeesproperByAssignId(id)) {
			attributes.addFlashAttribute("toastError", "紐づく社員情報が削除状態のため、復元できません。先に該当する社員情報（プロパー）を復元してください。");
			return "redirect:/deleted-assignments";
		}

		if (deletedAssignService.countEmployeespartnerByAssignId(id)) {
			attributes.addFlashAttribute("toastError", "紐づく社員情報が削除状態のため、復元できません。先に該当する社員情報（パートナー）を復元してください。");
			return "redirect:/deleted-assignments";
		}
		deletedAssignService.restore(id);
		attributes.addFlashAttribute("toastMessage", "アカウント情報を復元しました");
		return "redirect:/deleted-assignments";
	}

	/* 一括復元 */
	@PostMapping("/restore-bulk")
	public String bulkRestore(@RequestParam(name = "ids", required = false) List<Integer> ids,
			RedirectAttributes attributes) {
		if (ids == null || ids.isEmpty()) {
			attributes.addFlashAttribute("toastError", "復元する対象が選択されていません");
			return "redirect:/deleted-assignments";
		}
		if (deletedAssignService.countcompanysdispatchsByAssignIds(ids)) {
			attributes.addFlashAttribute("toastError", "紐づく派遣先企業が削除状態のため、復元できません。先に該当する企業情報を復元してください。");
			return "redirect:/deleted-assignments";
		}

		if (deletedAssignService.countcompanyspartnerByAssignIds(ids)) {
			attributes.addFlashAttribute("toastError", "紐づく所属元企業が削除状態のため、復元できません。先に該当する企業情報を復元してください。");
			return "redirect:/deleted-assignments";
		}
		if (deletedAssignService.countEmployeesproperByAssignIds(ids)) {
			attributes.addFlashAttribute("toastError", "紐づく社員情報が削除状態のため、復元できません。先に該当する社員情報（プロパー）を復元してください。");
			return "redirect:/deleted-assignments";
		}
		if (deletedAssignService.countEmployeespartnersByAssignIds(ids)) {
			attributes.addFlashAttribute("toastError", "紐づく社員情報が削除状態のため、復元できません。先に該当する社員情報（パートナー）を復元してください。");
			return "redirect:/deleted-assignments";
		}
		deletedAssignService.restoreBulk(ids);
		attributes.addFlashAttribute("toastMessage", ids.size() + "件のアカウント情報を復元しました");
		return "redirect:/deleted-assignments";
	}

	// ==========================================
	// 物理削除処理
	// ==========================================

	/* 単一削除 */
	@PostMapping("/{id}/delete")
	public String deleted(@PathVariable("id") Integer id, RedirectAttributes attributes) {
		deletedAssignService.physicalDelete(id);
		attributes.addFlashAttribute("toastMessage", "アカウント情報を完全に削除しました");
		return "redirect:/deleted-assignments";
	}

	/* 一括削除 */
	@PostMapping("/delete-bulk")
	public String bulkDeleted(@RequestParam(name = "ids", required = false) List<Integer> ids,
			RedirectAttributes attributes) {
		if (ids == null || ids.isEmpty()) {
			attributes.addFlashAttribute("toastError", "削除対象が選択されていません");
			return "redirect:/deleted-assignments";
		}

		deletedAssignService.physicalDeleteBulk(ids);
		attributes.addFlashAttribute("toastMessage", ids.size() + "件のアカウント情報を完全に削除しました");
		return "redirect:/deleted-assignments";
	}

	//エクスポート
	@PostMapping("/export")
	public String showExport(@RequestParam(name = "ids", required = false) List<Integer> ids,
			Model model, RedirectAttributes attributes) {
		if (ids == null || ids.isEmpty()) {
			attributes.addFlashAttribute("toastError", "エクスポートする対象が選択されていません");
			return "redirect:/deleted-assignments";
		}
		List<Assignment> assignments = deletedAssignService.findAllByIds(ids);
		model.addAttribute("count", assignments.size());
		model.addAttribute("assignments", assignments);
		model.addAttribute("ids", ids);
		return "deleted_assign/export";
	}

	@PostMapping("/export/download")
	public ResponseEntity<byte[]> downloadCsv(
			@RequestParam(name = "ids", required = false) List<Integer> ids) {
		List<Assignment> assignments = deletedAssignService.findAllByIds(ids);
		StringBuilder csvBuilder = new StringBuilder("アサインID,社員ID,社員姓,社員名,アサイン先企業名,作成日時,更新日時,契約開始日,契約終了日,契約単価,役割\n");
		for (Assignment asn : assignments) {
			csvBuilder.append(asn.getAssignmentId()).append(",")
					.append(asn.getEmpId()).append(",")
					.append(asn.getEmployee().getLastName()).append(",")
					.append(asn.getEmployee().getFirstName()).append(",")
					.append(asn.getCompany().getCompanyName()).append(",")
					.append(asn.getCreatedAt()).append(",")
					.append(asn.getUpdatedAt()).append(",")
					.append(asn.getContractStartDate()).append(",")
					.append(asn.getContractEndDate() != null ? asn.getContractEndDate() : "ー").append(",")
					.append(asn.getUnitPrice()).append(",")
					.append(asn.getRole().getRole()).append("\n");
		}
		byte[] csvBytes = csvBuilder.toString().getBytes(StandardCharsets.UTF_8);
		byte[] bom = new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
		byte[] result = new byte[bom.length + csvBytes.length];
		System.arraycopy(bom, 0, result, 0, bom.length);
		System.arraycopy(csvBytes, 0, result, bom.length, csvBytes.length);

		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "attachment; filename=assignment.csv");
		headers.add("Content-Type", "text/csv; charset=UTF-8");
		return new ResponseEntity<>(result, headers, HttpStatus.OK);
	}
}
