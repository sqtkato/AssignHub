package com.assignhub.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.multipart.MultipartFile;

import com.assignhub.entity.Account;
import com.assignhub.mapper.AccountMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * アカウント情報に関するビジネスロジックを提供するサービスクラス。
 *
 * @version 1.00 2026/06/12
 * @author ATO）黒木
 */
@Slf4j
@Service
public class AccountService {

	private final AccountMapper accountMapper;
	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	/**
	 * コンストラクタによる依存性の注入。
	 *
	 * @param accountMapper アカウント情報マスタに対するマッパー
	 */
	public AccountService(AccountMapper accountMapper) {
		this.accountMapper = accountMapper;
	}

	/**
	 * 検索条件およびソート条件に合致するアカウント情報を全件取得する。
	 *
	 * @param keyword    検索キーワード（社員名の部分一致）
	 * @param permission 絞り込み対象の権限
	 * @return アカウントエンティティのリスト
	 */
	public List<Account> findAll(String keyword, Integer permission) {
		return accountMapper.findAll(keyword, permission);
	}

	/**
	 * アカウントIDを指定して、アカウント情報を1件取得する。
	 *
	 * @param id 取得対象のアカウントID
	 * @return 該当するアカウントエンティティ（存在しない場合はnull）
	 */
	public Account findById(Integer id) {
		return accountMapper.findById(id);
	}

	/**
	 * アカウントIDを指定して、アカウント情報を複数件取得する。
	 *
	 * @param ids 取得対象のアカウントID
	 * @return 該当するアカウントエンティティ（存在しない場合はnull）
	 */
	public List<Account> findByIds(List<Integer> ids) {
		return accountMapper.findByIds(ids);
	}

	/**
	 * アカウント情報を保存する。
	 * IDが存在しない場合（nullまたは0）は新規登録（INSERT）、存在する場合は更新（UPDATE）を行う。
	 *
	 * @param accnount 登録または更新する社員アカウント
	 */
	@Transactional
	public void save(Account account) {
		account.setPasswordHash(passwordEncoder.encode(account.getPasswordHash()));
		if (account.getAccountId() == null) {
			accountMapper.insert(account);
		} else {
			accountMapper.update(account);
		}
	}

	/**
	 * 指定されたアカウントIDのデータを物理削除する。
	 *
	 * @param id 削除対象のアカウントID
	 */
	@Transactional
	public void delete(Integer id) {
		accountMapper.delete(id);
	}

	/**
	 * 指定された複数のアカウントIDのデータを一括で物理削除する。
	 *
	 * @param ids 削除対象となるアカウントIDのリスト
	 */
	@Transactional
	public void deleteBulk(List<Integer> ids) {
		if (ids != null && !ids.isEmpty()) {
			accountMapper.deleteBulk(ids);
		}
	}

	/**
	 * インポート時の各行のエラー内容を保持するクラス。
	 */
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

	/**
	 * インポート処理の全体結果（成功数、エラー数、エラー詳細リスト）を保持するクラス。
	 */
	public static class ImportResult {
		public int successCount = 0;
		public int errorCount = 0;
		public List<CsvRowError> errors = new ArrayList<>();
		public String limitError = null;
	}

	/**
	 * アップロードされたCSVファイルを解析し、バリデーションおよび一括登録・更新を行う。
	 * 1行ごとに保存処理を行うが、1件でもエラーがあれば全体をロールバックする。
	 *
	 * @return インポート処理の結果オブジェクト（成功・エラー件数および詳細）
	 * @throws Exception ファイル読み込み時やパース時に発生する例外
	 */
	@Transactional(rollbackFor = Exception.class)
	public ImportResult importCsv(MultipartFile file) throws Exception {
		ImportResult result = new ImportResult();
		Set<String> seenLoginIds = new HashSet<>();
		int accountCount = accountMapper.countAll();
		int insertPlan = 0;
		CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
		try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), decoder))) {
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
				if (cols.length < 4) {
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
				String permissionStr = cols[3].trim();

				int permission = 0;
				if (permissionStr.equals("一般")) {
					permission = 0;
				} else if (permissionStr.equals("管理")) {
					permission = 1;
				} else {
					result.errors.add(new CsvRowError(rowNum, "権限", "権限は一般または管理で入力してください。"));
					hasError = true;
				}

				if (!accountIdStr.isEmpty()) {
					try {
						Integer accountId = Integer.parseInt(accountIdStr);
						if (findById(accountId) == null) {
							result.errors.add(new CsvRowError(rowNum, "アカウントID", "指定されたアカウントIDが見つかりません。新規登録の場合はアカウントIDは空欄にしてください。"));
							hasError = true;
						} else {
							account.setAccountId(accountId);
						}
					} catch (Exception e) {
						result.errors.add(new CsvRowError(rowNum, "アカウントID", "指定されたアカウントIDが見つかりません。新規登録の場合はアカウントIDは空欄にしてください。"));
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
						result.errors.add(new CsvRowError(rowNum, "パスワード", "パスワードは半角英数字または記号(\"@\",\"_\")のみで入力してください"));
						hasError = true;
					}
					if (rawPassword.length() < 8 || rawPassword.length() > 20) {
						result.errors.add(new CsvRowError(rowNum, "パスワード", "パスワードは8文字以上20文字以内で入力してください"));
						hasError = true;
					}
				}

				if (!hasError) {
					if (!seenLoginIds.add(loginId)) {
						result.errors.add(new CsvRowError(rowNum, "ログインID", "CSV内でログインIDが重複しています"));
						hasError = true;
					} else if (isLoginIdDuplicate(loginId, account.getAccountId())) {
						result.errors.add(new CsvRowError(rowNum, "ログインID", "このログインIDは既に使用されています"));
						hasError = true;
					}
				}

				if (!hasError && account.getAccountId() == null && (accountCount + insertPlan) >= 500) {
					result.limitError = "登録後の件数が上限に達しています。アカウントの登録上限は500件です。";
					rowNum++;
					continue;
				}

				if (!hasError) {
					try {
						boolean isNew = (account.getAccountId() == null);
						account.setLoginId(loginId);
						account.setPasswordHash(rawPassword);
						account.setPermission(permission);
						save(account);
						if (isNew) {
							insertPlan++;
						}
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
			if (result.errorCount > 0 || result.limitError != null) {
				TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
				result.successCount = 0;
			}
		}
		return result;
	}

	/**
	 * ログインIDがすでに登録されているか（重複しているか）を判定する。
	 *
	 * @param loginId チェックするログインID
	 * @return 重複していればtrue
	 */
	public boolean isLoginIdDuplicate(String loginId, Integer excludeAccountId) {
		int count = accountMapper.countByLoginId(loginId, excludeAccountId);
		return count > 0;
	}

	/**
	 * アカウント登録数が上限（500件）に達しているかを判定する。
	 *
	 * @return 上限に達していればtrue
	 */
	public boolean isMaxCount() {
		return accountMapper.countAll() >= 500;
	}
	
	public Account findByLoginId(String loginId) {
	    return accountMapper.findByLoginId(loginId);
	}
	
	public List<Account> findLoginId() {
		return accountMapper.findLoginId();
	}
}