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

import lombok.extern.slf4j.Slf4j;

/**
 * アカウント管理に関するビジネスロジックを提供するサービスクラス。
 *
 * @version 1.00 2026/06/01
 * @author SQT）チームC
 */
@Slf4j
@Service
public class AccountService {

	private final AccountMapper accountMapper;
	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	/**
	 * コンストラクタによる依存性の注入。
	 *
	 * @param accountMapper アカウントマスタに対するマッパー
	 */
	public AccountService(AccountMapper accountMapper) {
		this.accountMapper = accountMapper;
	}

	/**
	 * 検索条件およびソート条件に合致するアカウント情報を全件取得する。
	 *
	 * @param keyword 検索キーワード（企業名の部分一致）
	 * @param sort    ソート対象のカラム名
	 * @param order   昇順（asc）または降順（desc）
	 * @return 企業エンティティのリスト
	 */
	public List<Account> findAll(String keyword, String sort, String order, Integer permission) {
		return accountMapper.findAll(keyword, sort, order, permission);
	}

	public List<Account> findByIds(List<Integer> ids){
		return accountMapper.findByIds(ids);
	}

    public Account findByLoginId(String loginId) {
        return accountMapper.findByLoginId(loginId);
    }
    public Account login(String loginId, String password) {

        // ① loginIdでユーザー取得
        Account account = accountMapper.findByLoginId(loginId);
        // ② ユーザー存在チェック
        if (account == null) {
            return null;
        }
        // ③ パスワードチェック（ハッシュ照合）
        if (passwordEncoder.matches(password, account.getPasswordHash())) {
            return account;
        }
        return null;
    }

	/**
	 * アカウント情報を保存する。
	 * IDが存在しない場合は新規登録（INSERT）、存在する場合は更新（UPDATE）を行う。
	 *
	 * @param account 登録または更新するアカウントエンティティ
	 */
	@Transactional
	public void save(Account account) {
		account.setPasswordHash(passwordEncoder.encode(account.getPasswordHash()));
		if (account.getAccountId() == null) {
			accountMapper.save(account);
		} else {
			accountMapper.update(account);
		}

	    account.setPasswordHash(
	        passwordEncoder.encode(account.getPasswordHash())
	    );

		accountMapper.save(account);
		
	}
	
	public boolean existsByLoginIdUpdate(String loginId, Integer currentAccountId) {
		
		return accountMapper.existsByLoginIdUpdate(loginId, currentAccountId);
	}
	
	public boolean existsByLoginId(String loginId) {
		return accountMapper.existsByLoginId(loginId);
	}


	// ===== ここから アカウント情報インポート機能 =====


	/**
	 * インポート時の各行のエラー内容を保持するクラス。
	 */
	public static class CsvRowError {
		public int rowNum;
		public String field;
		public String message;


				String label = rowNum + "行目";
				line = line.replace("\uFEFF", "");
				String[] cols = line.split(",", -1);

				// No.6 项目数不足（4列必要）
				if (cols.length < 3) {
					errors.add(new ImportError(label, "全体", "項目数が不足しています"));
					continue;
				}
				String accountIdStr = cols[0].trim();   // 第1列：账户ID
				String loginId       = cols[1].trim();   // 第2列：登录ID
				String password      = cols[2].trim();   // 第3列：密码
				String permission    = "0";   // 第4列：权限

				// No.7 账户ID填了但DB不存在
				if (!accountIdStr.isEmpty()) {
					try {
						Integer accountId = Integer.valueOf(accountIdStr);
						if (accountMapper.findById(accountId) == null) {
							errors.add(new ImportError(label, "アカウントID",
									"指定されたアカウントIDが見つかりません。新規登録の場合はアカウントIDは空欄にしてください。"));
						}
					} catch (NumberFormatException e) {
						errors.add(new ImportError(label, "アカウントID",
								"指定されたアカウントIDが見つかりません。新規登録の場合はアカウントIDは空欄にしてください。"));
					}
				}

				// ===== ログインID =====
				if (loginId.isEmpty()) {
					errors.add(new ImportError(label, "ログインID", "ログインIDは必須です"));            // No.8
				} else {
					if (!loginId.matches("^[a-zA-Z0-9]+$")) {                                            // No.12
						errors.add(new ImportError(label, "ログインID", "ログインIDは半角英数字のみで入力してください"));
					}
					if (loginId.length() < 5 || loginId.length() > 12) {                                 // No.10
						errors.add(new ImportError(label, "ログインID", "ログインIDは5文字以上12文字以内で入力してください"));
					}
				}

				// ===== パスワード =====
				if (password.isEmpty()) {
					errors.add(new ImportError(label, "パスワード", "パスワードは必須です"));            // No.9
				} else {
					if (!password.matches("^[a-zA-Z0-9@_]+$")) {                                         // No.13
						errors.add(new ImportError(label, "パスワード", "パスワードは半角英数字または記号(\"@\",\"_\")のみで入力してください"));
					}
					if (password.length() < 8 || password.length() > 20) {                               // No.11
						errors.add(new ImportError(label, "パスワード", "パスワードは8文字以上20文字以内で入力してください"));
					}
				}

			}
		}
	}

	/**
	 * インポート処理の全体結果（成功数、エラー数、エラー詳細リスト）を保持するクラス。
	 */
	public static class ImportResult {
		public int successCount = 0;
		public int errorCount = 0;
		public List<CsvRowError> errors = new ArrayList<>();
	}

	/**
	 * アップロードされたCSVファイルを解析し、バリデーションおよび一括登録・更新を行う。
	 * 1行ごとに保存処理を行うが、1件でもエラーがあれば全体をロールバックする。
	 * CSVは3列（アカウントID、ログインID、パスワード）。権限は一律「一般(0)」で登録する。
	 *
	 * @param file アップロードされたマルチパート形式のCSVファイル
	 * @return インポート処理の結果オブジェクト（成功・エラー件数および詳細）
	 * @throws Exception ファイル読み込み時やパース時に発生する例外
	 */
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

				String accountIdStr = cols[0].trim();
				String loginId       = cols[1].trim();
				String rawPassword   = cols[2].trim();
				String permission    = "0";

				try {
					// 密码 BCrypt 加密
					String hashed = passwordEncoder.encode(rawPassword);

					Account account = new Account();
					account.setLoginId(loginId);
					account.setPasswordHash(hashed);
					// 权限：空欄なら "0"(一般)、それ以外は入力値（0 または 1）
					account.setPermission(permission.isEmpty() ? 0 : Integer.parseInt(permission));

					if (accountIdStr.isEmpty()) {
						// 账户ID空 → 新增
						accountMapper.save(account);
					} else {
						// 账户ID有值 → 更新
						account.setAccountId(Integer.valueOf(accountIdStr));
						accountMapper.update(account);
					}
				} catch (Exception e) {
					// No.14 写入失败
					errors.add(new ImportError(label, "DB登録", "保存に失敗しました"));
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

	/**
	 * CSVファイルのデータ行数（ヘッダー・空行を除く）を数える。
	 *
	 * @param file アップロードされたCSVファイル
	 * @return データ行数
	 * @throws Exception ファイル読み込み時の例外
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


	public Account findById(Integer id) {
		return accountMapper.findById(id);
	}


	// 指定された複数の社員IDのデータを一括で物理削除する。
	@Transactional
	public void deleteBulk(List<Integer> ids) {
		if (ids != null && !ids.isEmpty()) {
			accountMapper.deleteBulk(ids);
		}

	}


		
	

	

	}
	
