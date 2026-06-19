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

/**
 * 論理削除されたアサイン履歴情報の管理、復元、物理削除、エクスポートを処理するコントローラー。
 * @author SQT) 石田
 */
@Controller
@RequestMapping("/deleted-assignments")
public class DeletedAssignController {
	private final DeletedAssignService deletedAssignService;

	/**
	 * コンストラクタによる依存性の注入。
	 *
	 * @param DeletedAssignService アサイン履歴サービス
	 */
	public DeletedAssignController(DeletedAssignService deletedAssignService) {
		this.deletedAssignService = deletedAssignService;
	}

	/**
	 * 論理削除済みアサイン履歴情報一覧画面を表示する。検索条件に応じたデータを取得する
	 * @param model   画面描画用モデル
	 * @return 一覧画面のテンプレートパス
	 */
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
			model.addAttribute("assignments", deletedAssignService.findAll(null, null, null, null, null));
			return "deleted_assign/index";
		}

		model.addAttribute("assignments", deletedAssignService.findAll(
				searchForm.getEmpName(),
				searchForm.getAssignName(),
				searchForm.getCompanyName(),
				startDate == null ? null : startDate.toString(),
				endDate == null ? null : endDate.toString()));

		return "deleted_assign/index";
	}

	/**
	 * 論理削除済みアサイン履歴を一件復元する。
	 *
	 * @param id 復元対象のアサインID
	 * @return 一覧画面へのリダイレクトパス
	 */
	/* 単一復元 */
	@PostMapping("/{id}/restore")
	public String recover(@PathVariable("id") Integer id, RedirectAttributes attributes) {

		if (deletedAssignService.isAssginLimitReachedAfterRestore(1)) {
			attributes.addFlashAttribute("toastError", "登録件数が上限（500件）に達するため、復元できません。");
			return "redirect:/deleted-assignments";
		}

		if (deletedAssignService.existCompanyDispatchsByAssignId(id)) {
			attributes.addFlashAttribute("toastError", "紐づくアサイン先企業が削除状態のため、復元できません。先に該当する企業情報を復元してください。");
			return "redirect:/deleted-assignments";
		}

		if (deletedAssignService.existEmployeeByAssignId(id)) {
			attributes.addFlashAttribute("toastError", "紐づく社員情報が削除状態のため、復元できません。先に該当する社員情報を復元してください。");
			return "redirect:/deleted-assignments";
		}

		if (deletedAssignService.isAssignIdDuplicate(id)) {
			attributes.addFlashAttribute("toastError", "社員ID、企業ID、アサイン開始日、アサイン終了日が重複している履歴があります。");
			return "redirect:/deleted-assignments";
		}
		deletedAssignService.restore(id);
		return "redirect:/deleted-assignments";
	}

	/**
	 * 選択された複数の論理削除済みアサイン履歴情報を一括で復元する。
	 *
	 * @param ids        復元対象となるアサインIDのリスト
	 * @param attributes リダイレクト時にメッセージを引き継ぐための属性
	 * @return 一覧画面へのリダイレクトパス
	 */
	/* 一括復元 */
	@PostMapping("/bulk-restore")
	public String bulkRecover(@RequestParam(name = "ids", required = false) List<Integer> ids,
			RedirectAttributes attributes) {
		if (ids == null || ids.isEmpty()) {
			attributes.addFlashAttribute("toastError", "復元する対象が選択されていません");
			return "redirect:/deleted-assignments";
		}

		if (deletedAssignService.isAssginLimitReachedAfterRestore(ids.size())) {
			attributes.addFlashAttribute("toastError", "復元後の件数が上限に達しています。アサイン履歴情報の登録上限は500件です。");
			return "redirect:/deleted-assignments";
		}

		if (deletedAssignService.existCompanyDispatchsByAssignIds(ids)) {
			attributes.addFlashAttribute("toastError", "紐づくアサイン先企業が削除状態のため、復元できません。先に該当する企業情報を復元してください。");
			return "redirect:/deleted-assignments";
		}

		if (deletedAssignService.existEmployeeByAssignIds(ids)) {
			attributes.addFlashAttribute("toastError", "紐づく社員情報が削除状態のため、復元できません。先に該当する社員情報を復元してください。");
			return "redirect:/deleted-assignments";
		}
		if (deletedAssignService.isAssignIdsDuplicate(ids)) {
			attributes.addFlashAttribute("toastError", "社員ID、企業ID、アサイン開始日、アサイン終了日が重複している履歴があります。");
			return "redirect:/deleted-assignments";
		}
		deletedAssignService.restoreBulk(ids);
		return "redirect:/deleted-assignments";
	}

	/**
	 * 論理削除済みアサイン履歴情報を一件物理削除する。
	 *
	 * @param id 削除対象のアサインID
	 * @return 一覧画面へのリダイレクトパス
	 */
	/* 単一削除 */
	@PostMapping("/{id}/delete")
	public String delete(@PathVariable("id") Integer id, RedirectAttributes attributes) {
		deletedAssignService.physicalDelete(id);
		return "redirect:/deleted-assignments";
	}

	/**
	 * 選択された複数の論理削除済みアサイン履歴情報を一括で物理削除する。
	 *
	 * @param ids        削除対象となるアサインIDのリスト
	 * @param attributes リダイレクト時にメッセージを引き継ぐための属性
	 * @return 一覧画面へのリダイレクトパス
	 */
	/* 一括削除 */
	@PostMapping("/bulk-delete")
	public String bulkDelete(@RequestParam(name = "ids", required = false) List<Integer> ids,
			RedirectAttributes attributes) {
		if (ids == null || ids.isEmpty()) {
			attributes.addFlashAttribute("toastError", "削除対象が選択されていません");
			return "redirect:/deleted-assignments";
		}

		deletedAssignService.physicalDeleteBulk(ids);
		return "redirect:/deleted-assignments";
	}

	/**
	 * 検索条件に合致する論理削除済みアサイン履歴情報をCSV形式でダウンロードする。
	 *
	 * @param ids    エクスポート対象となるアカウントIDのリスト 
	 * @return ダウンロード用のCSVファイルバイナリデータ
	 */
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
		StringBuilder csvBuilder = new StringBuilder(
				"アサインID,社員ID,企業ID,社員姓,社員名,アサイン先企業名,作成日時,更新日時,契約開始日,契約終了日,契約単価,役割\n");
		for (Assignment asn : assignments) {
			csvBuilder.append(asn.getAssignmentId()).append(",")
					.append(asn.getEmpId()).append(",")
					.append(asn.getCompanyId()).append(",")
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
		headers.add("Content-Disposition", "attachment; filename=deleted-assignments.csv");
		headers.add("Content-Type", "text/csv; charset=UTF-8");
		return new ResponseEntity<>(result, headers, HttpStatus.OK);
	}
}
