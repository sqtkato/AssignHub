package com.assignhub.controller;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
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
import com.assignhub.form.AssignmentForm;
import com.assignhub.service.AssignmentService;

/**
 * アサイン情報のコントローラークラス
 * 
 * @author Team Excel
 * @version 1.00 2024/06/08
 */
@Controller
@RequestMapping("/assignments")
public class AssignmentController {
	
	private final AssignmentService assignmentService;

	public AssignmentController(AssignmentService assignmentService) {
		this.assignmentService = assignmentService;
	}
	
	/**
	 * アサイン情報の一覧を表示する	
	 * @param txtEmpName 社員名の検索キーワード
	 * @param txtAssignName アサイン先企業名の検索キーワード
	 * @param txtCompanyName 所属企業名の検索キーワード
	 * @param txtContractStartDate 契約開始日の検索キーワード
	 * @param txtContractEndDate 契約終了日の検索キーワード
	 * @param model モデルオブジェクト
	 * @return アサイン情報の一覧画面のテンプレートパス
	 */
	@GetMapping
	public String index(
	        @RequestParam(name = "txt_emp_name", required = false) String txtEmpName,
	        @RequestParam(name = "txt_assign_name", required = false) String txtAssignName,
	        @RequestParam(name = "txt_company_name", required = false) String txtCompanyName,
	        @RequestParam(name = "txt_contract_start_date", required = false) String txtContractStartDate,
	        @RequestParam(name = "txt_contract_end_date", required = false) String txtContractEndDate,
	        Model model) {

	    LocalDate startDate = null;
	    LocalDate endDate = null;
	    
	    if (txtEmpName != null && txtEmpName.length() > 100) {
	        model.addAttribute("toastError", "社員名は100文字以内で入力してください。");
	        return returnIndex(model, txtEmpName, txtAssignName, txtCompanyName,
	                txtContractStartDate, txtContractEndDate);
	    }
	    
	    if (txtAssignName != null && txtAssignName.length() > 50) {
	        model.addAttribute("toastError", "アサイン先企業名は50文字以内で入力してください。");
	        return returnIndex(model, txtEmpName, txtAssignName, txtCompanyName,
	                txtContractStartDate, txtContractEndDate);
	    }
	    
	    if (txtCompanyName != null && txtCompanyName.length() > 50) {
	        model.addAttribute("toastError", "所属企業は50文字以内で入力してください。");
	        return returnIndex(model, txtEmpName, txtAssignName, txtCompanyName,
	                txtContractStartDate, txtContractEndDate);
	    }
	    
	    try {
	        if (txtContractStartDate != null && !txtContractStartDate.isBlank()) {
	            startDate = LocalDate.parse(txtContractStartDate);
	        }
	    } catch (Exception e) {
	        model.addAttribute("toastError", "契約開始日は正しい日付を入力してください。");
	        return returnIndex(model, txtEmpName, txtAssignName, txtCompanyName,
	                txtContractStartDate, txtContractEndDate);
	    }
	    
	    try {
	        if (txtContractEndDate != null && !txtContractEndDate.isBlank()) {
	            endDate = LocalDate.parse(txtContractEndDate);
	        }
	    } catch (Exception e) {
	        model.addAttribute("toastError", "契約終了日は正しい日付を入力してください。");
	        return returnIndex(model, txtEmpName, txtAssignName, txtCompanyName,
	                txtContractStartDate, txtContractEndDate);
	    }
	    
	    
	    if (startDate != null && endDate != null
	            && startDate.isAfter(endDate)) {

	        model.addAttribute(
	                "toastError",
	                "契約開始日は契約終了日以前の日付を入力してください。");

	        return returnIndex(model, txtEmpName, txtAssignName, txtCompanyName,
	                txtContractStartDate, txtContractEndDate);
	    }

		List<Assignment> assignments = assignmentService.findAll(
	                    txtEmpName,
	                    txtAssignName,
	                    txtCompanyName,
	                    txtContractStartDate,
	                    txtContractEndDate);

	    model.addAttribute("assignments", assignments);
	    model.addAttribute("txt_emp_name", txtEmpName);
	    model.addAttribute("txt_assign_name", txtAssignName);
	    model.addAttribute("txt_company_name", txtCompanyName);
	    model.addAttribute("txt_contract_start_date", txtContractStartDate);
	    model.addAttribute("txt_contract_end_date", txtContractEndDate);

	    return "assignment/index";
	}
	
	/**
	 * アサイン情報の新規登録画面を表示する
	 * 
	 * @param model モデルオブジェクト
	 * @return アサイン情報の新規登録画面のテンプレートパス
	 */
	@GetMapping("/new")
	public String create(Model model, RedirectAttributes attributes) {
		if (!model.containsAttribute("assignmentForm")) {
			model.addAttribute("assignmentForm", new AssignmentForm());
		}
		
		if (assignmentService.isMaxCount()) {
			attributes.addFlashAttribute(
					"toastError",
					"登録可能なアサイン情報は最大500件までです。");
			return "redirect:/assignments";
		}
		addComboBoxItems(model);
		return "assignment/create";
	}
	
	
	/**
	 * アサイン情報を新規登録する
	 * 
	 * @param form アサイン情報のフォームオブジェクト
	 * @param result バリデーション結果
	 * @param model モデルオブジェクト
	 * @param attributes リダイレクト属性オブジェクト
	 * @return 登録成功時はアサイン情報の一覧画面にリダイレクト、バリデーションエラー時は新規登録画面のテンプレートパス
	 */
	@PostMapping
	public String store(@Validated @ModelAttribute("assignmentForm") AssignmentForm form,
        BindingResult result,
        Model model,
        RedirectAttributes attributes) {

		if (result.hasErrors()) {
			addComboBoxItems(model);
			return "assignment/create";
		}
		
		Assignment assignment = new Assignment();
		copyFormToEntity(form, assignment);
		
		/**
		 * 契約開始日と契約終了日の順序をチェックする
		 * 契約終了日が入力されている場合、契約開始日より前の日付は入力できないようにする
		 */
		if (assignment.getContractEndDate() != null
        	&& assignment.getContractStartDate().isAfter(assignment.getContractEndDate())) {
			addComboBoxItems(model);
			result.rejectValue(
					"contractEndDate",
					"date.order",
					"契約開始日より前の日付は入力できません。");

    		return "assignment/create";
		}
		
		/**
		 * アサイン情報の重複をチェックする
		 */
		if (assignmentService.existsDuplicate(assignment)) {
			addComboBoxItems(model);
			result.reject(
					"duplicate",
					"すでに同じ内容が登録されています。");
        	return "assignment/create";
    	}
		
		assignmentService.save(assignment);
		attributes.addFlashAttribute("toastMessage", "アサイン情報を登録しました");
		return "redirect:/assignments";
	}
	
	@GetMapping("/{id}")
	public String detail(@PathVariable Integer id, Model model) {
	    Assignment assignment = assignmentService.findById(id);
	    model.addAttribute("assignment", assignment);
	    return "assignment/detail";
	}
	
	@GetMapping("/{id}/edit")
	public String edit(@PathVariable Integer id) {
	    return "assignment/edit";
	}
	
	@GetMapping("/import")
	public String importPage() {
	    return "assignment/import";
	}
	
	/**
	 * アサイン情報を一括で削除する
	 * 
	 * @param ids 削除対象のアサインIDリスト
	 * @param attributes リダイレクト属性オブジェクト
	 * @return アサイン情報の一覧画面にリダイレクト
	 */
	@PostMapping("/bulk-delete")
	public String bulkDelete(
	        @RequestParam(name = "ids", required = false) List<Integer> ids,
	        RedirectAttributes attributes) {

	    if (ids == null || ids.isEmpty()) {
	        attributes.addFlashAttribute("toastError", "削除対象は必須です");
	        return "redirect:/assignments";
	    }

	    assignmentService.deleteBulk(ids);
	    attributes.addFlashAttribute("toastMessage", "選択したアサイン情報を削除しました");
	    return "redirect:/assignments";
	}
	
	/**
	 * アサイン情報を削除する
	 * 
	 * @param id 削除対象のアサインID
	 * @param attributes リダイレクト属性オブジェクト
	 * @return アサイン情報の一覧画面にリダイレクト
	 */
	@PostMapping("/{id}/delete")
	public String delete(@PathVariable("id") Integer id, RedirectAttributes attributes) {
	    assignmentService.deleteById(id);
	    attributes.addFlashAttribute("toastMessage", "アサイン情報を削除しました");
	    return "redirect:/assignments";
	}
	
	/**
	 * アサイン情報を一括でエクスポートする
	 * 
	 * @param ids エクスポート対象のアサインIDリスト
	 * @param attributes リダイレクト属性オブジェクト
	 * @return アサイン情報の一覧画面にリダイレクト
	 */
	@PostMapping("/export")
	public String export(@RequestParam(name = "txt_emp_name", required = false) String txtEmpName,
			@RequestParam(name = "txt_assign_name", required = false) String txtAssignName,
			@RequestParam(name = "txt_company_name", required = false) String txtCompanyName,
			@RequestParam(name = "txt_contract_start_date", required = false) String txtContractStartDate,
			@RequestParam(name = "txt_contract_end_date", required = false) String txtContractEndDate,
			@RequestParam(name = "ids", required = false) List<Integer> ids,
			Model model,
			RedirectAttributes redirectAttributes) {
		if (ids == null || ids.isEmpty()) {
			redirectAttributes.addFlashAttribute("toastError", "エクスポートする対象が選択されていません");
			return "redirect:/assignments";
		}
		List<Assignment> assignments = ids.stream().map(assignmentService::findById).toList();
		model.addAttribute("assignments", assignments);
		model.addAttribute("count", assignments.size());
		model.addAttribute("ids", ids);
		return "assignment/export";

	    }

	@GetMapping("/export/download")
	public ResponseEntity<byte[]> downloadCsv(
			@RequestParam(name = "ids") List<Integer> ids) {
		List<Assignment> assignments = ids.stream().map(assignmentService::findById).toList();
		StringBuilder csvBuilder = new StringBuilder("アサインID,社員ID,社員名,アサイン先企業名,作成日時,更新日時,契約開始日,契約終了日,契約単価,役割\n");
		for (Assignment asn : assignments) {
			csvBuilder.append(asn.getAssignmentId()).append(",")
					.append(asn.getEmpId()).append(",")
					.append(asn.getEmpName()).append(",")
					.append(asn.getCompanyName()).append(",")
					.append(asn.getCreatedAt()).append(",")
					.append(asn.getUpdatedAt()).append(",")
					.append(asn.getContractStartDate()).append(",")
					.append(asn.getContractEndDate() != null ? asn.getContractEndDate() : "ー").append(",")
					.append(asn.getUnitPrice()).append(",")
					.append(asn.getRole()).append("\n");
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
	
	/**
	 * アサイン情報のフォームオブジェクトからエンティティオブジェクトに値をコピーする
	 * 
	 * @param f アサイン情報のフォームオブジェクト
	 * @param e アサイン情報のエンティティオブジェクト
	 */
	private void copyFormToEntity(AssignmentForm f, Assignment e) {
	    e.setEmpId(f.getEmpId());
	    e.setCompanyId(f.getCompanyId());
	    e.setContractStartDate(f.getContractStartDate());
	    e.setContractEndDate(f.getContractEndDate());
	    e.setUnitPrice(f.getUnitPrice());
	    e.setRoleId(f.getRoleId());
	}

	private void addComboBoxItems(Model model) {
	    model.addAttribute("employeeOptions", assignmentService.findEmployeeOptions());
	    model.addAttribute("companyOptions", assignmentService.findCompanyOptions());
	    model.addAttribute("roleOptions", assignmentService.findRoleOptions());
	}
	
	private String returnIndex(
	        Model model,
	        String txtEmpName,
	        String txtAssignName,
	        String txtCompanyName,
	        String txtContractStartDate,
	        String txtContractEndDate) {

	    model.addAttribute("txt_emp_name", txtEmpName);
	    model.addAttribute("txt_assign_name", txtAssignName);
	    model.addAttribute("txt_company_name", txtCompanyName);
	    model.addAttribute("txt_contract_start_date", txtContractStartDate);
	    model.addAttribute("txt_contract_end_date", txtContractEndDate);

	    model.addAttribute(
	            "assignments",
	            assignmentService.findAll(null, null, null, null, null));

	    return "assignment/index";
	}
}
