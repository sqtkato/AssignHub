package com.assignhub.controller;

import java.nio.charset.StandardCharsets;
import java.util.List;

//import jakarta.servlet.http.HttpSession;

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

import com.assignhub.entity.Account;
import com.assignhub.service.DeletedAccountService;

@Controller
@RequestMapping("/deleted-accounts")
public class DeletedAccountController {
	private final DeletedAccountService deletedAccountService;

	/**
	 * コンストラクタによる依存性の注入。
	 *
	 * @param DeletedAccountService 企業サービス
	 */
	public DeletedAccountController(DeletedAccountService deletedAccountService) {
		this.deletedAccountService = deletedAccountService;
	}

	@GetMapping
	public String index(@RequestParam(name = "empName", required = false) String empName,
			Model model,
			@RequestParam(name = "permission", required = false) Integer permission) {
		model.addAttribute("accounts", deletedAccountService.findAll(empName, permission));

		return "deleted_account/index";
	}
    // ==========================================
    // 復元処理
    // ==========================================
    
    /* 単一復元 */
    @PostMapping("/{id}/restore") 
    public String recover(@PathVariable("id") Integer id, RedirectAttributes attributes) {
        deletedAccountService.restore(id);
        return "redirect:/deleted-accounts"; 
    }

    /* 一括復元 */
    @PostMapping("/restore-bulk")
    public String bulkRecover(@RequestParam(name = "ids", required = false) List<Integer> ids, RedirectAttributes attributes) {
        if (ids == null || ids.isEmpty()) {
            attributes.addFlashAttribute("toastError", "復元する対象が選択されていません");
            return "redirect:/deleted-accounts";
        }
        deletedAccountService.restoreBulk(ids);
        return "redirect:/deleted-accounts";
    }
    
    // ==========================================
    // 物理削除処理
    // ==========================================

    /* 単一削除 */
    @PostMapping("/{id}/delete")
    public String deleted(@PathVariable("id") Integer id, RedirectAttributes attributes) {
        // Serviceの判定メソッドを使って不在条件をチェック
        if (deletedAccountService.countEmployeesByAccountId(id)) {
            attributes.addFlashAttribute("toastError", "紐づく社員情報が存在するため、物理削除できません。先に社員情報を物理削除してください。");
            return "redirect:/deleted-accounts";
        }
        if (deletedAccountService.countAssignmentsByAccountId(id)) {
            attributes.addFlashAttribute("toastError", "紐づくアサイン履歴情報が存在するため、物理削除できません。先にアサイン履歴情報を物理削除してください。");
            return "redirect:/deleted-accounts";
        }
        
        deletedAccountService.physicalDelete(id);
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
        if (deletedAccountService.countEmployeesByAccountIds(ids)) {
            attributes.addFlashAttribute("toastError", "紐づく社員情報が存在するアカウントが含まれているため、物理削除できません。");
            return "redirect:/deleted-accounts";
        }
        if (deletedAccountService.countAssignmentsByAccountIds(ids)) {
            attributes.addFlashAttribute("toastError", "紐づくアサイン履歴情報が存在するアカウントが含まれているため、物理削除できません。");
            return "redirect:/deleted-accounts";
        }
        
        deletedAccountService.physicalDeleteBulk(ids);
        return "redirect:/deleted-accounts";
    }
	
//	エクスポート画面へ遷移	
	@GetMapping("/export")
	public String showExport(@RequestParam(name = "ids", required = false) List<Integer> ids, Model model, RedirectAttributes attributes) {		
		// チェックがなければ一覧へ戻す
	    if (ids == null || ids.isEmpty()) {
	        attributes.addFlashAttribute("toastError", "エクスポートする対象が選択されていません。");
	        return "redirect:/deleted-accounts";
	    }		
		model.addAttribute("count", deletedAccountService.findByIds(ids));
		
		return "deleted_account/export";
	}
	
//エクスポートのダウンロード処理
	@GetMapping("/export/download")
	public ResponseEntity<byte[]> downloadCsv(
			@RequestParam(name = "ids", required = false) List<Integer> ids) {
		List<Account> accounts = deletedAccountService.findByIds(ids);
		StringBuilder csvBuilder = new StringBuilder("アカウントID,ログインID,権限,社員名(姓),社員名(名)\n");
		for (Account acc : accounts) {
			csvBuilder.append(acc.getAccountId()).append(",")
					.append(acc.getLoginId()).append(",")
					.append(acc.getPermission()).append(",")
					.append(acc.getEmployee().getLastName()).append(",")
					.append(acc.getEmployee().getFirstName()).append("\n");
		}
		byte[] csvBytes = csvBuilder.toString().getBytes(StandardCharsets.UTF_8);
		byte[] bom = new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
		byte[] result = new byte[bom.length + csvBytes.length];
		System.arraycopy(bom, 0, result, 0, bom.length);
		System.arraycopy(csvBytes, 0, result, bom.length, csvBytes.length);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "attachment; filename=account.csv");
		headers.add("Content-Type", "text/csv; charset=UTF-8");
		return new ResponseEntity<>(result, headers, HttpStatus.OK);
	}

}
