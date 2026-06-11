package com.assignhub.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.assignhub.entity.Company;
import com.assignhub.entity.Employee;
import com.assignhub.mapper.EmployeeMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * 社員管理に関するビジネスロジックを提供するサービスクラス。
 *
 * @version 1.01 2026/06/01
 * @author SQT）チームB
 */
@Slf4j
@Service
public class EmployeeService {

	private final EmployeeMapper employeeMapper;

	/**
	 * コンストラクタによる依存性の注入。
	 *
	 * @param employeeMapper 社員マスタに対するマッパー
	 */
	public EmployeeService(EmployeeMapper employeeMapper) {
		this.employeeMapper = employeeMapper;
	}

	/**
	 * 検索条件およびソート条件に合致する社員情報を全件取得する。
	 *
	 * @param keyword 検索キーワード（社員名の部分一致）
	 * @param sort    ソート対象のカラム名
	 * @param order   昇順（asc）または降順（desc）
	 * @return 社員エンティティのリスト
	 */
	public List<Employee> findAll(String txt_emp_name, String txt_emp_assign_company, String cmb_emp_engineer_type, String txt_emp_company) {
		return employeeMapper.findAll(txt_emp_name, txt_emp_assign_company, cmb_emp_engineer_type, txt_emp_company);
	}

	/**
	 * 社員IDを指定して、社員情報を1件取得する。
	 *
	 * @param id 取得対象の社員ID
	 * @return 該当する社員エンティティ（存在しない、または論理削除済みの場合はnull）
	 */
	public Employee findById(Integer id) {
		return employeeMapper.findById(id);
	}
	
	@Transactional
	public void delete(Integer id) {
		employeeMapper.delete(id);
	}
	
	@Transactional
	public void deleteBulk(List<Integer> ids) {
		if (ids != null && !ids.isEmpty()) {
			employeeMapper.deleteBulk(ids);
		}
	}

	/**
	 * 社員情報を保存する。
	 * IDが存在しない場合（nullまたは0）は新規登録（INSERT）、存在する場合は更新（UPDATE）を行う。
	 *
	 * @param employee 登録または更新する社員エンティティ
	 */
	@Transactional(rollbackFor = Exception.class)
	public void save(Employee employee) {
		if (employee.getEmpId() == null || employee.getEmpId() == 0) {
			employeeMapper.insert(employee);
		} else {
			employeeMapper.update(employee);
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
	}
	/**
	 * アップロードされたCSVファイルを解析し、バリデーションおよび一括登録・更新を行う。
	 * 1行ごとに保存処理を行うが、1件でもエラーがあれば全体をロールバックする。
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

				String[] cols = line.split(",", -1);
				if (cols.length < 7) {
					result.errors.add(new CsvRowError(rowNum, "全体", "項目数が不足しています（7項目必要）"));
					result.errorCount++;
					rowNum++;
					continue;
				}

				boolean hasError = false;
				Employee emp = new Employee();
				Integer parsedId = null;

				if (!cols[0].trim().isEmpty()) {
					try {
						parsedId = Integer.parseInt(cols[0].trim());
						if (findById(parsedId) == null) {
							result.errors.add(new CsvRowError(rowNum, "社員ID",
									"指定された社員ID（" + parsedId + "）は存在しません（新規登録の場合は空欄にしてください）"));
							hasError = true;
						} else {
							emp.setEmpId(parsedId);
						}
					} catch (Exception e) {
						result.errors.add(new CsvRowError(rowNum, "社員ID", "数値以外の文字が含まれています"));
						hasError = true;
					}
				}

				String lastName = cols[1].trim();
				if (lastName.isEmpty()) {
				    result.errors.add(new CsvRowError(rowNum, "社員姓", "社員姓は必須です"));
				    hasError = true;
				} else if (lastName.length() > 50) {
				    result.errors.add(new CsvRowError(rowNum, "社員姓", "社員姓は50文字以内で入力してください"));
				    hasError = true;
				} else {
				    emp.setLastName(lastName);
				}

				String firstName = cols[2].trim();
				if (firstName.isEmpty()) {
				    result.errors.add(new CsvRowError(rowNum, "社員名", "社員名は必須です"));
				    hasError = true;
				} else if (firstName.length() > 50) {
				    result.errors.add(new CsvRowError(rowNum, "社員名", "社員名は50文字以内で入力してください"));
				    hasError = true;
				} else {
				    emp.setFirstName(firstName);
				}
				
				String lastNameKana = cols[3].trim();
				if (lastNameKana.isEmpty()) {
				    result.errors.add(new CsvRowError(rowNum, "社員姓カナ", "社員姓カナは必須です"));
				    hasError = true;
				} else if (lastNameKana.length() > 50) {
				    result.errors.add(new CsvRowError(rowNum, "社員姓カナ", "社員姓カナは50文字以内で入力してください"));
				    hasError = true;
				} else {
				    emp.setLastNameKana(lastNameKana);
				}

				String firstNameKana = cols[4].trim();
				if (firstNameKana.isEmpty()) {
				    result.errors.add(new CsvRowError(rowNum, "社員名カナ", "社員名カナは必須です"));
				    hasError = true;
				} else if (firstNameKana.length() > 50) {
				    result.errors.add(new CsvRowError(rowNum, "社員名カナ", "社員名カナは50文字以内で入力してください"));
				    hasError = true;
				} else {
				    emp.setFirstNameKana(firstNameKana);
				}
				
				String hireDate = cols[5].trim();
				if (!hireDate.isEmpty()) {
				    try {
				        LocalDate hireDate = LocalDate.parse(hireDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
				        emp.setHireDate(hireDate);
				    } catch (DateTimeParseException e) {
				        result.errors.add(new CsvRowError(rowNum, "入社年月日", "日付の形式が正しくありません\nYYYY-MM-DD 形式で入力してください"));
				        hasError = true;
				    }
				}
				
				String yearsOfServiceStr = cols[6].trim();
				if (!yearsOfServiceStr.isEmpty()) {
				    try {
				        int yearsOfService = Integer.parseInt(yearsOfServiceStr);
				        if (yearsOfService > 999) {
				            result.errors.add(new CsvRowError(rowNum, "勤続年数", "勤続年数は3桁以内で入力してください"));
				            hasError = true;
				        } else {
				            emp.setYearsOfService(yearsOfService);
				        }
				    } catch (NumberFormatException e) {
				        result.errors.add(new CsvRowError(rowNum, "勤続年数", "勤続年数は数値で入力してください"));
				        hasError = true;
				    }
				}
				
				String birthDateStr = cols[7].trim();
				if (!birthDateStr.isEmpty()) {
				    try {
				        LocalDate birthDate = LocalDate.parse(birthDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
				if (birthDate.isAfter(LocalDate.now())) {
				            result.errors.add(new CsvRowError(rowNum, "生年月日", "入力された年月日は正しくありません"));
				            hasError = true;
				} else {
				            emp.setBirthDate(birthDate);
				}
				} catch (DateTimeParseException e) {
				        result.errors.add(new CsvRowError(rowNum, "生年月日", "日付の形式が正しくありません\nYYYY-MM-DD 形式で入力してください"));
				        hasError = true;
				    }
				}
				
				String zipCode = cols[8].trim();
				if (zipCode.isEmpty()) {
				    result.errors.add(new CsvRowError(rowNum, "郵便番号", "郵便番号は必須です"));
				    hasError = true;
				} else if (zipCode.length() > 7) {
				    result.errors.add(new CsvRowError(rowNum, "郵便番号", "郵便番号は7桁以内で入力してください"));
				    hasError = true;
				} else if (zipCode.contains("-")) {
				    result.errors.add(new CsvRowError(rowNum, "郵便番号", "郵便番号の形式が正しくありません\nハイフンなしで入力してください"));
				    hasError = true;
				} else {
				    emp.setZipCode(zipCode);
				}
				
				String address1 = cols[9].trim();
				if (address1.isEmpty()) {
				    result.errors.add(new CsvRowError(rowNum, "住所1", "住所1は必須です"));
				    hasError = true;
				} else if (address1.length() > 100) {
				    result.errors.add(new CsvRowError(rowNum, "住所1", "住所1は100文字以内で入力してください"));
				    hasError = true;
				} else {
				    emp.setAddress1(address1);
				}
				
				String address2 = cols[10].trim();
				if (address2.length() > 100) {
				    result.errors.add(new CsvRowError(rowNum, "住所2", "住所2は100文字以内で入力してください"));
				    hasError = true;
				} else {
				    emp.setAddress2(address2);
				}
				
				String engineerType = cols[11].trim();
				if (engineerType.isEmpty()) {
				    result.errors.add(new CsvRowError(rowNum, "エンジニアタイプ", "エンジニアタイプは必須です"));
				    hasError = true;
				} else if (!engineerType.equals("プロパー") && !engineerType.equals("パートナー")) {
				    result.errors.add(new CsvRowError(rowNum, "エンジニアタイプ", "エンジニアタイプは\"プロパー\"または\"パートナー\"を入力してください"));
				    hasError = true;
				} else {
				    emp.setEngineerType(engineerType);
				}
				
				String roginId = cols[12].trim();
				if (roginId.isEmpty()) {
				    result.errors.add(new CsvRowError(rowNum, "ログインID", "ログインIDは必須です"));
				    hasError = true;
				} else {
				    Account account = accountService.findByName(roginId);
				    
				if (account == null) {
				        result.errors.add(new CsvRowError(rowNum, "ログインID", "指定されたログインIDは存在しません"));
				        hasError = true;
				} else {
					emp.setAccountId(account.getAccountId());
					}
				}
				
				String companyName = cols[13].trim();
				if (companyName.isEmpty()) {
				    result.errors.add(new CsvRowError(rowNum, "所属企業", "所属企業は必須です"));
				    hasError = true;
				} else {
				    Company company = companyService.findByName(companyName);
				    if (company == null) {
				        result.errors.add(new CsvRowError(rowNum, "所属企業", "指定された所属企業は存在しません"));
				        hasError = true;
				    } else {
				        emp.setCompanyId(company.getCompanyId());
				    }
				}
				
				String department = cols[14].trim();
				if (department.length() > 100) {
				    result.errors.add(new CsvRowError(rowNum, "所属部署", "所属部署は100文字以内で入力してください"));
				    hasError = true;
				} else {
				    emp.setDepartment(department);
				}
				
				String jobTitle = cols[15].trim();
				if (jobTitle.length() > 100) {
				    result.errors.add(new CsvRowError(rowNum, "役職", "役職は100文字以内で入力してください"));
				    hasError = true;
				} else {
				    emp.setJobTitle(jobTitle);
				}
				
				String empTel = cols[16].trim();
				if (empTel.isEmpty()) {
				    result.errors.add(new CsvRowError(rowNum, "電話番号", "電話番号は必須です"));
				    hasError = true;
				} else if (empTel.contains("-")) {
				    result.errors.add(new CsvRowError(rowNum, "電話番号", "電話番号の形式が正しくありません\nハイフンなしで入力してください"));
				    hasError = true;
				} else if (empTel.length() != 10 && empTel.length() != 11) {
				    result.errors.add(new CsvRowError(rowNum, "電話番号", "電話番号は10桁または11桁で入力してください"));
				    hasError = true;
				} else {
				    emp.setEmpTel(empTel);
				}
				
				String email = cols[17].trim();
				if (email.isEmpty()) {
				    result.errors.add(new CsvRowError(rowNum, "メールアドレス", "メールアドレスは必須です"));
				    hasError = true;
				} else if (email.length() > 255) {
				    result.errors.add(new CsvRowError(rowNum, "メールアドレス", "メールアドレスは255文字以内で入力してください"));
				    hasError = true;
				} else if (!email.contains("@")) {
				    result.errors.add(new CsvRowError(rowNum, "メールアドレス", "メールアドレスの形式が正しくありません\n@を含めて入力してください"));
				    hasError = true;
				} else {
				    emp.setEmail(email);
				}

				if (!hasError && isEmailDuplicate(email, parsedId)) {
				    result.errors.add(new CsvRowError(rowNum, "メールアドレス", "このメールアドレスは既に使用されています"));
				    hasError = true;
				}

				if (!hasError) {
					try {
						save(emp);
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

			// エラーが1件でも発生した場合はトランザクションをロールバックする
			if (result.errorCount > 0) {
				org.springframework.transaction.interceptor.TransactionAspectSupport.currentTransactionStatus()
						.setRollbackOnly();
				// ロールバックされたため、成功件数を0に戻して画面表示を正す
				result.successCount = 0;
			}
		}
		return result;
	}

	/**
	 * メールアドレスがすでに登録されているか（重複しているか）を判定する。
	 *
	 * @param email        チェックするメールアドレス
	 * @param excludeEmpId 除外する社員ID（新規登録時はnullを渡す）
	 * @return 重複していればtrue
	 */
	public boolean isEmailDuplicate(String email, Integer excludeEmpId) {
		int count = employeeMapper.countByEmail(email, excludeEmpId);
		return count > 0;
	}
}

