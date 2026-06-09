package com.assignhub.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.assignhub.form.ImportError;
import com.assignhub.service.AccountImportService;

@Controller
public class AccountImportController {

    @Autowired
    private AccountImportService importService;

    @GetMapping("/accounts/import")
    public String importPage() {
        return "account/account_import";
    }

    @PostMapping("/accounts/import")
    public String doImport(@RequestParam("file") MultipartFile file, Model model) {
        model.addAttribute("done", true);

        if (file == null || file.isEmpty()) {
            model.addAttribute("fileError", "ファイルを選択してください");
            return "account/account_import";
        }

        try {
            int total = importService.countDataRows(file);

            if (total > 500) {
                model.addAttribute("globalError", "登録件数が上限（500件）に達しています");
                model.addAttribute("successCount", 0);
                model.addAttribute("errorCount", total);
                return "account/account_import";
            }

            List<ImportError> errors = importService.validate(file);

            if (!errors.isEmpty()) {
                model.addAttribute("successCount", 0);
                model.addAttribute("errorCount", errors.size());
                model.addAttribute("errors", errors);
            } else {
                model.addAttribute("successCount", total);
                model.addAttribute("errorCount", 0);
            }
        } catch (Exception e) {
            model.addAttribute("fileError", "ファイルの読み込みに失敗しました");
        }
        return "account/account_import";
    }
}