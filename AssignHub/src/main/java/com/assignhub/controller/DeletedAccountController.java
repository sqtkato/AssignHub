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

/**
 * 論理削除されたアカウント情報の管理、復元、物理削除、エクスポートを処理するコントローラー。
 * @author C3S) 野本
 */
@Controller
@RequestMapping("/deleted-accounts")
public class DeletedAccountController {
	private final DeletedAccountService deletedAccountService;
	/**
	 * コンストラクタによる依存性の注入。
	 *
	 * @param DeletedAccountService 論理削除済みアカウントサービス
	 */
	public DeletedAccountController(DeletedAccountService deletedAccountService) {
		this.deletedAccountService = deletedAccountService;
	}
	
	/**
	 * 論理削除済みアカウント一覧画面を表示する。検索条件に応じたデータを取得する
	 * @param empName 社員名検索キーワード（任意）
	 * @param model   画面描画用モデル
	 * @return 一覧画面のテンプレートパス
	 */
	@GetMapping
	public String index(@RequestParam(name = "empName", required = false) String empName,
			Model model,
			@RequestParam(name = "permission", required = false) Integer permission) {
		model.addAttribute("accounts", deletedAccountService.findAll(empName, permission));
		model.addAttribute("empName", empName);
		model.addAttribute("permission", permission);
		return "deleted_account/index";
	}
    
	/**
	 * 論理削除済みアカウントを一件復元する。
	 *
	 * @param id 復元対象のアカウントID
	 * @return 一覧画面へのリダイレクトパス
	 */
    @PostMapping("/{id}/restore") 
    public String recover(@PathVariable("id") Integer id, RedirectAttributes attributes) {
    	if (deletedAccountService.isAccountLimitReachedAfterRestore(1)) {
			attributes.addFlashAttribute("toastError", "登録件数が上限（500件）に達するため、復元できません。");
			return "redirect:/deleted-accounts";
		}
    	if (deletedAccountService.isLoginIdDuplicate(id)) {
    		attributes.addFlashAttribute("toastError", "このログインIDは既に使用されています");
			return "redirect:/deleted-accounts";
		}
        deletedAccountService.restore(id);
        return "redirect:/deleted-accounts"; 
    }
    
    /**
	 * 選択された複数の論理削除済みアカウント情報を一括で復元する。
	 *
	 * @param ids        復元対象となるアカウントIDのリスト
	 * @param attributes リダイレクト時にメッセージを引き継ぐための属性
	 * @return 一覧画面へのリダイレクトパス
	 */
    @PostMapping("/bulk-restore")
    public String bulkRecover(@RequestParam(name = "ids", required = false) List<Integer> ids, RedirectAttributes attributes) {
        if (ids == null || ids.isEmpty()) {
            attributes.addFlashAttribute("toastError", "復元する対象が選択されていません");
            return "redirect:/deleted-accounts";
        }
        if (deletedAccountService.isAccountLimitReachedAfterRestore(ids.size())) {
			attributes.addFlashAttribute("toastError", "復元後の件数が上限に達しています。アカウント情報の登録上限は500件です。");
			return "redirect:/deleted-accounts";
		}
        if (deletedAccountService.isLoginIdDuplicate(ids)) {
			attributes.addFlashAttribute("toastError", "このログインIDは既に使用されています");
			return "redirect:/deleted-accounts";
		}
        deletedAccountService.restoreBulk(ids);
        return "redirect:/deleted-accounts";
    }
    
    /**
	 * 論理削除済みアカウントを一件物理削除する。
	 *
	 * @param id 削除対象のアカウントID
	 * @return 一覧画面へのリダイレクトパス
	 */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Integer id, RedirectAttributes attributes) {
        if (deletedAccountService.existEmployeesByAccountId(id)) {
            attributes.addFlashAttribute("toastError", "紐づく社員情報が存在するため、物理削除できません。先に社員情報を物理削除してください。");
            return "redirect:/deleted-accounts";
        }
        deletedAccountService.physicalDelete(id);
        return "redirect:/deleted-accounts";
    }
    
    /**
	 * 選択された複数の論理削除済みアカウント情報を一括で物理削除する。
	 *
	 * @param ids        削除対象となるアカウントIDのリスト
	 * @param attributes リダイレクト時にメッセージを引き継ぐための属性
	 * @return 一覧画面へのリダイレクトパス
	 */
    @PostMapping("/bulk-delete")
    public String bulkDelete(@RequestParam(name = "ids", required = false) List<Integer> ids, RedirectAttributes attributes) {
        if (ids == null || ids.isEmpty()) {
            attributes.addFlashAttribute("toastError", "削除対象が選択されていません");
            return "redirect:/deleted-accounts";
        }
        
        if (deletedAccountService.existEmployeesByAccountIds(ids)) {
            attributes.addFlashAttribute("toastError", "紐づく社員情報が存在するアカウントが含まれているため、物理削除できません。");
            return "redirect:/deleted-accounts";
        }
        
        deletedAccountService.physicalDeleteBulk(ids);
        return "redirect:/deleted-accounts";
    }
	
    /**
	 * 論理削除済みアカウント情報のエクスポート画面を表示する。
	 *
	 * @param ids    エクスポート対象となるアカウントIDのリスト 
	 * @param model  画面描画用のモデル
	 * @return エクスポート画面のテンプレートパス
	 */
	@PostMapping("/export")
	public String showExport(@RequestParam(name = "ids", required = false) List<Integer> ids, Model model, RedirectAttributes attributes) {		
	    if (ids == null || ids.isEmpty()) {
	        attributes.addFlashAttribute("toastError", "エクスポートする対象が選択されていません。");
	        return "redirect:/deleted-accounts";
	    }		
	    List<Account> accounts = deletedAccountService.findByIds(ids);
		model.addAttribute("count", accounts.size());
		model.addAttribute("accounts", accounts);
		model.addAttribute("ids", ids);
		return "deleted_account/export";
	}
	
	/**
	 * 検索条件に合致する論理削除済みアカウント情報をCSV形式でダウンロードする。
	 *
	 * @param ids    エクスポート対象となるアカウントIDのリスト 
	 * @return ダウンロード用のCSVファイルバイナリデータ
	 */
	@PostMapping("/export/download")
	public ResponseEntity<byte[]> downloadCsv(
			@RequestParam(name = "ids", required = false) List<Integer> ids) {
		List<Account> accounts = deletedAccountService.findByIds(ids);
		StringBuilder csvBuilder = new StringBuilder("アカウントID,ログインID,パスワード,権限\n");
		for (Account acc : accounts) {
			String Permission = "";
			if(acc.getPermission() == 0) {
				Permission = "一般";
			}else {
				Permission = "管理";
			}
			csvBuilder.append(acc.getAccountId()).append(",")
					.append(acc.getLoginId()).append(",")
					.append(",")
					.append(Permission).append("\n");
		}
		byte[] csvBytes = csvBuilder.toString().getBytes(StandardCharsets.UTF_8);
		byte[] bom = new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
		byte[] result = new byte[bom.length + csvBytes.length];
		System.arraycopy(bom, 0, result, 0, bom.length);
		System.arraycopy(csvBytes, 0, result, bom.length, csvBytes.length);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "attachment; filename=deleted-accounts.csv");
		headers.add("Content-Type", "text/csv; charset=UTF-8");
		return new ResponseEntity<>(result, headers, HttpStatus.OK);
	}
}
