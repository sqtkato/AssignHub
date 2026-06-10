package com.assignhub.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.assignhub.entity.Account;
import com.assignhub.form.ImportError;
import com.assignhub.mapper.AccountMapper;

@Service
public class AccountService {

	private final AccountMapper accountMapper;
	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	public AccountService(AccountMapper accountMapper) {
		this.accountMapper = accountMapper;
	}

	/**
	 * 検索条件およびソート条件に合致する企業情報を全件取得する。
	 *
	 * @param keyword 検索キーワード（企業名の部分一致）
	 * @param sort    ソート対象のカラム名
	 * @param order   昇順（asc）または降順（desc）
	 * @return 企業エンティティのリスト
	 */
	public List<Account> findAll(String keyword, String sort, String order, Integer permission) {
		return accountMapper.findAll(keyword, sort, order, permission);
	}

	public List<Account> findByIds(List<Integer> ids) {
		return accountMapper.findByIds(ids);
	}
	
	
	public void save(Account account) {

	    account.setPasswordHash(
	        passwordEncoder.encode(account.getPasswordHash())
	    );

	    accountMapper.save(account);
	}
	// 追加：ログインIDの重複チェック
	public boolean existsByLoginId(String loginId) {
		return accountMapper.existsByLoginId(loginId);
	}

	// ===== ここから アカウント情報インポート機能 =====

	/**
	 * CSVファイルを1行ずつ検証し、エラーがあればリストで返す。
	 *
	 * @param file アップロードされたCSVファイル
	 * @return エラー情報のリスト（エラーがなければ空）
	 */
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

	/**
	 * CSVファイルのデータ行数（ヘッダー・空行を除く）を数える。
	 *
	 * @param file アップロードされたCSVファイル
	 * @return データ行数
	 */
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
	
	/**
	 * アカウントを一件論理削除。
	 *
	 * @param id　削除対象のアカウントID
	 */
	@Transactional
	public void delete(Integer id) {
		accountMapper.delete(id);
	}
}