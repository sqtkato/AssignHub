package com.assignhub.service;

import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.assignhub.entity.Account;
import com.assignhub.mapper.AccountMapper;

@Service
public class AccountService {

	private final AccountMapper accountMapper;
	// 密码加密器（和 login 一致）
	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	public AccountService(AccountMapper accountMapper) {
		this.accountMapper = accountMapper;
	}


	/**
	 * 検索条件およびソート条件に合致する企業情報を全件取得する。
	 *
	 * @param keyword 検索キーワード（各カラム部分一致）
	 * @param sort    ソート対象のカラム名
	 * @param order   昇順（asc）または降順（desc）
	 * @return エンティティのリスト
	 */
	public List<Account> findAll(String keyword, String sort, String order, Integer permission) {
		return accountMapper.findAll(keyword, sort, order, permission);

	}

	public List<Account> findByIds(List<Integer> ids) {
		return accountMapper.findByIds(ids);
	}

	
	

	public void save(Account account) {
		if (account.getAccountId() == null) {
			accountMapper.save(account); 
		} else {
			accountMapper.update(account);
		}

	}
	// 追加：ログインIDの重複チェック
	public boolean existsByLoginId(String loginId) {
		return accountMapper.existsByLoginId(loginId);
	}

	// ===== ここから アカウント情報インポート機能 =====

	/**
	 * CSVファイルを1行ずつ検証し、エラーがあればリストで返す。
	 * （DB登録前のチェック。No.6〜No.13 + 権限チェック）
	 */
	public List<ImportError> validate(MultipartFile file) throws Exception {
		List<ImportError> errors = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
			String line;
			int rowNum = 0;
			while ((line = br.readLine()) != null) {
				rowNum++;
				if (rowNum == 1) continue;           // 表头
				if (line.trim().isEmpty()) continue;  // 空行

				String label = rowNum + "行目";
				line = line.replace("\uFEFF", "");
				String[] cols = line.split(",", -1);

				// No.6 项目数不足（4列必要）
				if (cols.length < 4) {
					errors.add(new ImportError(label, "全体", "項目数が不足しています"));
					continue;
				}

				String accountIdStr = cols[0].trim();   // 第1列：账户ID
				String loginId       = cols[1].trim();   // 第2列：登录ID
				String password      = cols[2].trim();   // 第3列：密码
				String permission    = cols[3].trim();   // 第4列：权限

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

				// ===== 権限（空欄OK、空欄なら0扱い。0/1以外はエラー）=====
				if (!permission.isEmpty() && !permission.equals("0") && !permission.equals("1")) {
					errors.add(new ImportError(label, "権限", "権限は0(一般)または1(管理)で入力してください"));
				}
			}
		}
		return errors;
	}

	/**
	 * CSVのデータをDBに登録/更新する。（バリデーション通過後に呼ぶ）
	 * アカウントID空→新規登録(INSERT)、IDあり→更新(UPDATE)。
	 * 失敗した行はエラーリストで返す（No.14）。
	 */
	public List<ImportError> importData(MultipartFile file) throws Exception {
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

				String accountIdStr = cols[0].trim();
				String loginId       = cols[1].trim();
				String rawPassword   = cols[2].trim();
				String permission    = cols[3].trim();

				try {
					// 密码 BCrypt 加密
					String hashed = passwordEncoder.encode(rawPassword);

					Account account = new Account();
					account.setLoginId(loginId);
					account.setPasswordHash(hashed);
					// 权限：空欄なら "0"(一般)、それ以外は入力値（0 または 1）
					account.setPermission(permission.isEmpty() ? "0" : permission);

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
			}
		}
		return errors;
	}

	/**
	 * CSVファイルのデータ行数（ヘッダー・空行を除く）を数える。
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

	//	public void update(Integer id) {
//		 accountMapper.update(id);}
	

	public void update(Account account) {
		accountMapper.update(account);
		
	// 指定された複数の社員IDのデータを一括で物理削除する。
	@Transactional
	public void deleteBulk(List<Integer> ids) {
		if (ids != null && !ids.isEmpty()) {
			accountMapper.deleteBulk(ids);
		}
	}

}
