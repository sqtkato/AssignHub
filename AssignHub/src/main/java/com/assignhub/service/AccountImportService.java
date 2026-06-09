package com.assignhub.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.assignhub.form.ImportError;

@Service
public class AccountImportService {

    public List<ImportError> validate(MultipartFile file) throws Exception {
        List<ImportError> errors = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            int rowNum = 0;
            while ((line = br.readLine()) != null) {
                rowNum++;
                if (rowNum == 1) continue;
                if (line.trim().isEmpty()) continue;

                String label = rowNum + "行目";
                line = line.replace("\uFEFF", "");
                String[] cols = line.split(",", -1);

                if (cols.length < 3) {
                    errors.add(new ImportError(label, "全体", "項目数が不足しています"));
                    continue;
                }

                String loginId  = cols[1].trim();
                String password = cols[2].trim();

                if (loginId.isEmpty()) {
                    errors.add(new ImportError(label, "ログインID", "ログインIDは必須です"));
                }
                if (password.isEmpty()) {
                    errors.add(new ImportError(label, "パスワード", "パスワードは必須です"));
                }
                if (!loginId.isEmpty() && !loginId.matches("^[a-zA-Z0-9]+$")) {
                    errors.add(new ImportError(label, "ログインID", "ログインIDは半角英数字のみ入力してください"));
                }
                if (!password.isEmpty() && !password.matches("^[a-zA-Z0-9@_]+$")) {
                    errors.add(new ImportError(label, "パスワード", "パスワードは半角英数字または記号(\"@\"\"_\")のみで入力してください"));
                }
            }
        }
        return errors;
    }

    public int countDataRows(MultipartFile file) throws Exception {
        int count = 0;
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int rowNum = 0;
            while ((line = br.readLine()) != null) {
                rowNum++;
                if (rowNum == 1) continue;
                if (line.trim().isEmpty()) continue;
                count++;
            }
        }
        return count;
    }
}