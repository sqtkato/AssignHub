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

import com.assignhub.entity.DeletedAssign;
import com.assignhub.service.DeletedAssignService;

@Controller
@RequestMapping("/deleted-assign")
public class DeletedAssignController {
	private final DeletedAssignService deletedAssignService;
	
	/**
	 * コンストラクタによる依存性の注入。
	 *
	 * @param companyService 企業サービス
	 */
	public DeletedAssignController(DeletedAssignService deletedAssignService) {
		this.deletedAssignService = deletedAssignService;	
	}
	
	
	
		@GetMapping
		public String index(
				@RequestParam("txtEmpName" ) String txtEmpName, 
	            @RequestParam("txtAssignName") String txtAssignName,
	            @RequestParam("txtCompanyName") String txtCompanyName,
	            @RequestParam("txtContractStartDate") String txtContractStartDate,
	            @RequestParam("txtContractEndDate") String txtContractEndDate,
	            Model model) {
	        
	        model.addAttribute("accounts", deletedAssignService.findAll(txtEmpName, txtAssignName, txtCompanyName, txtContractStartDate,txtContractEndDate));
	        model.addAttribute("txtEmpName", txtEmpName); 
	        model.addAttribute("txtAssignName", txtCompanyName);
	        model.addAttribute("txtContractStartDate",txtContractStartDate);
	        model.addAttribute("txtContractEndDate", txtContractEndDate);
	        
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
        	return "redirect:/deleted-assigns";
	    }
    
        if (deletedAssignService.countcompanyspartnerByAssignId(id)) {
        	attributes.addFlashAttribute("toastError", "紐づく所属元企業が削除状態のため、復元できません。先に該当する企業情報を復元してください。");
        	return "redirect:/deleted-assagns";
        }
        
	    if (deletedAssignService.countEmployeesproperByAssignId(id)) {
        	attributes.addFlashAttribute("toastError","紐づく社員情報が削除状態のため、復元できません。先に該当する社員情報（プロパー）を復元してください。"); 
        	return "redirect:/deleted-assagns";
	    }
	    
	    if (deletedAssignService.countEmployeespartnerByAssignId(id)) {
        	attributes.addFlashAttribute("toastError","紐づく社員情報が削除状態のため、復元できません。先に該当する社員情報（パートナー）を復元してください。"); 
        	return "redirect:/deleted-assaigns";
	    }
        	deletedAssignService.restore(id);
	        attributes.addFlashAttribute("toastMessage", "アカウント情報を復元しました");
	        return "redirect:/deleted-assigns"; 
	    }
	    
	    
	    
	   

	    /* 一括復元 */
	    @PostMapping("/restore-bulk")
	    public String bulkRestore(@RequestParam(name = "ids", required = false) List<Integer> ids, RedirectAttributes attributes) {
	        if (ids == null || ids.isEmpty()) {
	            attributes.addFlashAttribute("toastError", "復元する対象が選択されていません");
	            return "redirect:/deleted-assaigns";
	        }
	        if (deletedAssignService.countcompanysdispatchsByAssignIds(ids)) {
	    	    attributes.addFlashAttribute("toastError", "紐づく派遣先企業が削除状態のため、復元できません。先に該当する企業情報を復元してください。");
	        	return "redirect:/deleted-assaigns";
	        }
	    
	        if (deletedAssignService.countcompanyspartnerByAssignIds(ids)) {
	        	attributes.addFlashAttribute("toastError", "紐づく所属元企業が削除状態のため、復元できません。先に該当する企業情報を復元してください。");
	        	return "redirect:/deleted-assaigns";
	    }
		    if (deletedAssignService.countEmployeesproperByAssignIds(ids)) {
	        	attributes.addFlashAttribute("toastError","紐づく社員情報が削除状態のため、復元できません。先に該当する社員情報（プロパー）を復元してください。"); 
	        	return "redirect:/deleted-assaigns";
		    }
		    if (deletedAssignService.countEmployeespartnersByAssignIds(ids)) {
	        	attributes.addFlashAttribute("toastError","紐づく社員情報が削除状態のため、復元できません。先に該当する社員情報（パートナー）を復元してください。"); 
	        	return "redirect:/deleted-assaigns";
		    }		
	        deletedAssignService.restoreBulk(ids);
	        attributes.addFlashAttribute("toastMessage", ids.size() + "件のアカウント情報を復元しました");
	        return "redirect:/deleted-assaigns";
	    }
	    
	    
	    
	    // ==========================================
	    // 物理削除処理
	    // ==========================================

	    /* 単一削除 */
	    @PostMapping("/{id}/delete")
	    public String deleted(@PathVariable("id") Integer id, RedirectAttributes attributes) {
	        deletedAssignService.physicalDelete(id);
	        attributes.addFlashAttribute("toastMessage", "アカウント情報を完全に削除しました");
	        return "redirect:/deleted-assigns";
	    }

	    /* 一括削除 */
	    @PostMapping("/delete-bulk")
	    public String bulkDeleted(@RequestParam(name = "ids", required = false) List<Integer> ids, RedirectAttributes attributes) {
	        if (ids == null || ids.isEmpty()) {
	            attributes.addFlashAttribute("toastError", "削除対象が選択されていません");
	            return "redirect:/deleted-assigns";
	        }
	      
	        deletedAssignService.physicalDeleteBulk(ids);
	        attributes.addFlashAttribute("toastMessage", ids.size() + "件のアカウント情報を完全に削除しました");
	        return "redirect:/deleted-accounts";
	    }
		
//		エクスポート画面へ遷移	
		@GetMapping("/export")
		public String showExport(@RequestParam(name = "ids", required = false) List<Integer> ids, Model model) {
			//引数内書き換え
			model.addAttribute("count", deletedAssignService.findAllByIds(ids));
			
			return "deleted_account/export";
		}
		
	//エクスポートのダウンロード処理
		@GetMapping("/export/download")
		public ResponseEntity<byte[]> downloadCsv(@RequestParam(name = "ids", required = false) List<Integer> ids, 
				Model model) {
			List<DeletedAssign> delAccount = deletedAssignService.findAllByIds(ids);
			//引数名書き換え
			StringBuilder csvBuilder = new StringBuilder("アカウントID,ログインID,パスワード,権限,削除フラグ,作成日時,更新日時,社員名\n");
			//括弧内書き換え
			for (DeletedAssign delAcc : delAccount) {
				csvBuilder.append(delAcc.getAssignmentId()).append(",")
						.append(delAcc.getEmpId()).append(",")
						.append(delAcc.getPasswordHash()).append(",")
						.append(delAcc.getPermission()).append(",")
						.append(delAcc.getDeleteFlg()).append(",")
						.append(delAcc.getCreatedAt()).append(",")
						.append(delAcc.getUpdatedAt()).append(",")
						.append(delAcc.getEmpName()).append("\n");
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


	
		
			
		
			
			


