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

import com.assignhub.entity.DeletedAccount;
import com.assignhub.service.DeletedAccountService;

@Controller
@RequestMapping("/deleted-accouts")
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
	
	
	
	@GetMapping
    public String index(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "sort", defaultValue = "account_id") String sort, // デフォルトをaccount_idに修正
            @RequestParam(name = "order", defaultValue = "asc") String order,
            @RequestParam(name = "permission", required = false) Integer permission, // permissionを追加
            Model model) {
        
        model.addAttribute("accounts", deletedAccountService.getDeletedAccounts(keyword, sort, order, permission));
        model.addAttribute("keyword", keyword); 
        model.addAttribute("permission", permission);
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentOrder", order);
        
        return "deleted_account/index";
    }

    // ==========================================
    // 復元処理
    // ==========================================
    
    /* 単一復元 */
    @PostMapping("/{id}/restore") 
    public String recover(@PathVariable("id") Integer id, RedirectAttributes attributes) {
        deletedAccountService.restoreAccount(id);
        attributes.addFlashAttribute("toastMessage", "アカウント情報を復元しました");
        return "redirect:/deleted-accounts"; 
    }

    /* 一括復元 */
    @PostMapping("/restore-bulk")
    public String bulkRecover(@RequestParam(name = "ids", required = false) List<Integer> ids, RedirectAttributes attributes) {
        if (ids == null || ids.isEmpty()) {
            attributes.addFlashAttribute("toastError", "復元する対象が選択されていません");
            return "redirect:/deleted-accounts";
        }
        deletedAccountService.restoreAccountsBulk(ids);
        attributes.addFlashAttribute("toastMessage", ids.size() + "件のアカウント情報を復元しました");
        return "redirect:/deleted-accounts";
    }
    
    // ==========================================
    // 物理削除処理
    // ==========================================

    /* 単一削除 */
    @PostMapping("/{id}/delete")
    public String deleted(@PathVariable("id") Integer id, RedirectAttributes attributes) {
        // Serviceの判定メソッドを使って不在条件をチェック
        if (deletedAccountService.hasAttachedEmployees(id)) {
            attributes.addFlashAttribute("toastError", "紐づく社員情報が存在するため、物理削除できません。先に社員情報を物理削除してください。");
            return "redirect:/deleted-accounts";
        }
        if (deletedAccountService.hasAttachedAssignments(id)) {
            attributes.addFlashAttribute("toastError", "紐づくアサイン履歴情報が存在するため、物理削除できません。先にアサイン履歴情報を物理削除してください。");
            return "redirect:/deleted-accounts";
        }
        
        deletedAccountService.physicalDeleteAccount(id);
        attributes.addFlashAttribute("toastMessage", "アカウント情報を完全に削除しました");
        return "redirect:/deleted-accounts";
    }

    /* 一括削除 */
    @PostMapping("/delete-bulk")
    public String bulkDeleted(@RequestParam(name = "ids", required = false) List<Integer> ids, RedirectAttributes attributes) {
        if (ids == null || ids.isEmpty()) {
            attributes.addFlashAttribute("toastError", "削除対象が選択されていません");
            return "redirect:/deleted-accounts";
        }
        
        // Serviceの判定メソッドを使って一括不在条件をチェック
        if (deletedAccountService.hasAttachedEmployeesBulk(ids)) {
            attributes.addFlashAttribute("toastError", "紐づく社員情報が存在するアカウントが含まれているため、物理削除できません。");
            return "redirect:/deleted-accounts";
        }
        if (deletedAccountService.hasAttachedAssignmentsBulk(ids)) {
            attributes.addFlashAttribute("toastError", "紐づくアサイン履歴情報が存在するアカウントが含まれているため、物理削除できません。");
            return "redirect:/deleted-accounts";
        }
        
        deletedAccountService.physicalDeleteAccountsBulk(ids);
        attributes.addFlashAttribute("toastMessage", ids.size() + "件のアカウント情報を完全に削除しました");
        return "redirect:/deleted-accounts";
    }
	
//	エクスポート画面へ遷移	
	@GetMapping("/export")
	public String showExport(@RequestParam(name = "ids", required = false) List<Integer> ids, Model model) {
		//引数内書き換え
		model.addAttribute("count", deletedAccountService.getExportData(ids));
		
		return "deleted_account/export";
	}
	
//エクスポートのダウンロード処理
	@GetMapping("/export/download")
	public ResponseEntity<byte[]> downloadCsv(@RequestParam(name = "ids", required = false) List<Integer> ids, 
			Model model) {
		List<DeletedAccount> delAccount = deletedAccountService.getExportData(ids);
		//引数名書き換え
		StringBuilder csvBuilder = new StringBuilder("アカウントID,ログインID,パスワード,権限,削除フラグ,作成日時,更新日時,社員名\n");
		//括弧内書き換え
		for (DeletedAccount delAcc : delAccount) {
			csvBuilder.append(delAcc.getAccountId()).append(",")
					.append(delAcc.getLoginId()).append(",")
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
