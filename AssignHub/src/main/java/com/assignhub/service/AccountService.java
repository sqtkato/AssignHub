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
import com.assignhub.mapper.AccountMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AccountService {

	private final AccountMapper accountMapper;
	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	public AccountService(AccountMapper accountMapper) {
		this.accountMapper = accountMapper;
	}

	public List<Account> findAll(String keyword, String sort, String order, Integer permission) {
		return accountMapper.findAll(keyword, sort, order, permission);
	}

	public List<Account> findByIds(List<Integer> ids) {
		return accountMapper.findByIds(ids);
	}

	public Account findById(Integer id) {
		return accountMapper.findById(id);
	}

	@Transactional
	public void save(Account account) {
		if (account.getAccountId() == null) {
			accountMapper.save(account);
		} else {
			accountMapper.update(account);
		}
	}

	@Transactional
	public void update(Account account) {
		accountMapper.update(account);
	}

	@Transactional
	public void delete(Integer id) {
		accountMapper.delete(id);
	}

	@Transactional
	public void deleteBulk(List<Integer> ids) {
		if (ids != null && !ids.isEmpty()) {
			accountMapper.deleteBulk(ids);
		}
	}

	public boolean existsByLoginId(String loginId) {
		return accountMapper.existsByLoginId(loginId);
	}

	public static class CsvRowError {
		public int rowNum;
		public String field;
		public String message;

		public CsvRowError(int rowNum, String field, String message) {
			this.rowNum = rowNum;
			this.field = field;
			this.message = message;
		}
	}

	public static class ImportResult {
		public int successCount = 0;
		public int errorCount = 0;
		public List<CsvRowError> errors = new ArrayList<>();
	}

	@Transactional(rollbackFor = Exception.class)
	public ImportResult importCsv(MultipartFile file) throws Exception {
		ImportResult result = new ImportResult();

		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
			String line;
			int rowNum = 1;
			boolean isFirstLine = true;

			while ((line = br.readLine()) != null) {
				if (isFirstLine) {
					isFirstLine = false;
					rowNum++;
					continue;
				}
				if (line.trim().isEmpty()) {
					rowNum++;
					continue;
				}

				line = line.replace("\uFEFF", "");
				String[] cols = line.split(",", -1);

				if (cols.length < 3) {
					result.errors.add(new CsvRowError(rowNum, "全体", "項目数が不足しています"));
					result.errorCount++;
					rowNum++;
					continue;
				}

				boolean hasError = false;
				Account account = new Account();

				String accountIdStr = cols[0].trim();
				String loginId = cols[1].trim();
				String rawPassword = cols[2].trim();

				if (!accountIdStr.isEmpty()) {
					try {
						Integer accountId = Integer.parseInt(accountIdStr);
						if (findById(accountId) == null) {
							result.errors.add(new CsvRowError(rowNum, "アカウントID",
									"指定されたアカウントIDが見つかりません。新規登録の場合はアカウントIDは空欄にしてください。"));
							hasError = true;
						} else {
							account.setAccountId(accountId);
						}
					} catch (Exception e) {
						result.errors.add(new CsvRowError(rowNum, "アカウントID",
								"指定されたアカウントIDが見つかりません。新規登録の場合はアカウントIDは空欄にしてください。"));
						hasError = true;
					}
				}

				if (loginId.isEmpty()) {
					result.errors.add(new CsvRowError(rowNum, "ログインID", "ログインIDは必須です"));
					hasError = true;
				} else {
					if (!loginId.matches("^[a-zA-Z0-9]+$")) {
						result.errors.add(new CsvRowError(rowNum, "ログインID", "ログインIDは半角英数字のみで入力してください"));
						hasError = true;
					}
					if (loginId.length() < 5 || loginId.length() > 12) {
						result.errors.add(new CsvRowError(rowNum, "ログインID", "ログインIDは5文字以上12文字以内で入力してください"));
						hasError = true;
					}
				}

				if (rawPassword.isEmpty()) {
					result.errors.add(new CsvRowError(rowNum, "パスワード", "パスワードは必須です"));
					hasError = true;
				} else {
					if (!rawPassword.matches("^[a-zA-Z0-9@_]+$")) {
						result.errors.add(new CsvRowError(rowNum, "パスワード",
								"パスワードは半角英数字または記号(\"@\",\"_\")のみで入力してください"));
						hasError = true;
					}
					if (rawPassword.length() < 8 || rawPassword.length() > 20) {
						result.errors.add(new CsvRowError(rowNum, "パスワード", "パスワードは8文字以上20文字以内で入力してください"));
						hasError = true;
					}
				}

				if (!hasError) {
					try {
						account.setLoginId(loginId);
						account.setPasswordHash(passwordEncoder.encode(rawPassword));
						account.setPermission(0);

						save(account);
						result.successCount++;
					} catch (Exception e) {
						log.error("CSVインポート中エラー（{}行目）: データの保存に失敗しました。", rowNum, e);
						result.errors.add(new CsvRowError(rowNum, "DB登録", "保存に失敗しました"));
						result.errorCount++;
					}
				} else {
					result.errorCount++;
				}
				rowNum++;
			}

			if (result.errorCount > 0) {
				org.springframework.transaction.interceptor.TransactionAspectSupport.currentTransactionStatus()
						.setRollbackOnly();
				result.successCount = 0;
			}
		}
		return result;
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