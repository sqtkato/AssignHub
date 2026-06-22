package com.assignhub.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.multipart.MultipartFile;

import com.assignhub.entity.Assignment;
import com.assignhub.mapper.AssignmentMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * アサイン履歴情報管理に関するビジネスロジックを提供するサービスクラス。
 * 
 * @author Team Excel
 * @version 1.00 2026/06/16
 */
@Slf4j
@Service
public class AssignmentService {
	private final AssignmentMapper assignmentMapper;

	/**
	 * コンストラクタによる依存性の注入。
	 *
	 * @param assignmentMapper アサイン履歴情報に対するマッパー
	 */
	public AssignmentService(AssignmentMapper assignmentMapper) {
		this.assignmentMapper = assignmentMapper;
	}

	/**
	 * アサイン履歴情報の一覧を取得する。
	 *
	 * @param empName           社員名の部分一致検索キーワード
	 * @param assignName        アサイン先企業名の部分一致検索キーワード
	 * @param companyName       企業名の部分一致検索キーワード
	 * @param contractStartDate 契約開始日の範囲検索の開始日（yyyy/MM/dd形式）
	 * @param contractEndDate   契約終了日の範囲検索の終了日（yyyy/MM/dd形式）
	 * @return アサイン履歴エンティティのリスト
	 */
	public List<Assignment> findAll(String empName, String assignName, String companyName,
			String contractStartDate, String contractEndDate) {
		return assignmentMapper.findAll(empName, assignName, companyName, contractStartDate,
				contractEndDate);
	}

	/**
	 * アサイン履歴IDを指定して、アサイン履歴情報情報を1件取得する。
	 *
	 * @param id アサイン履歴情報のID
	 * @return 該当するアサイン履歴エンティティ（存在しない場合はnull）
	 */
	public Assignment findById(Integer id) {
		return assignmentMapper.findById(id);
	}
	
	/**
	 * チェックボックスでアサイン履歴IDを取得する。
	 * @param ids 選択されたチェックボックスの行に対応するアサイン履歴ID
	 * @return 該当するアサイン履歴エンティティのリスト
	 */
	public List<Assignment> findByIds(List<Integer> ids) {
		return assignmentMapper.findByIds(ids);
	}

	/**
	 * アサイン履歴情報を保存する。
	 * IDが存在しない場合（nullまたは0）は新規登録（INSERT）、存在する場合は更新（UPDATE）を行う。
	 *
	 * @param assignment 登録または更新するアサイン履歴エンティティ
	 */
	@Transactional
	public void save(Assignment assignment) {
		if (assignment.getAssignmentId() == null || assignment.getAssignmentId() == 0) {
			assignmentMapper.insert(assignment);
		} else {
			assignmentMapper.update(assignment);
		}
	}
	
	/**
	 * 指定されたアサイン履歴IDのデータを論理削除する。
	 *
	 * @param id 削除対象のアサイン履歴ID
	 */
	@Transactional
	public void delete(Integer id) {
		assignmentMapper.delete(id);
	}
	
	/**
	 * 指定された複数のアサイン履歴IDのデータを一括で論理削除する。
	 *
	 * @param ids 削除対象となるアサイン履歴IDのリスト
	 */
	@Transactional
	public void deleteBulk(List<Integer> ids) {
		assignmentMapper.deleteBulk(ids);
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
		public String limitMessage = null;
	}

	private static final int UNIT_PRICE_MAX_DIGITS = 10;

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

		Set<String> seenInCsv = new HashSet<>();

		int assignmentCount = assignmentMapper.countAll();
		int insertPlan = 0;

		CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
				.onMalformedInput(CodingErrorAction.REPORT)
				.onUnmappableCharacter(CodingErrorAction.REPORT);

		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(file.getInputStream(), decoder))) {
			String line;
			int rowNum = 1;
			boolean isFirstLine = true;

			while ((line = br.readLine()) != null) {
				if (isFirstLine) {
					line = stripBom(line);
					isFirstLine = false;
					rowNum++;
					continue;
				}
				if (line.trim().isEmpty()) {
					rowNum++;
					continue;
				}

				String[] cols = line.split(",", -1);
				if (cols.length < 12) {
					result.errors.add(new CsvRowError(rowNum, "全体", "項目数が不足しています"));
					result.errorCount++;
					rowNum++;
					continue;
				}

				boolean hasError = false;
				Assignment asm = new Assignment();
				LocalDate start = null;
				LocalDate end = null;

				String sId = cols[0].trim();
				if (!sId.isEmpty()) {
					Integer id = parseInteger(sId);
					if (id == null) {
						result.errors.add(new CsvRowError(rowNum, "アサインID", "数値以外の文字が含まれています"));
						hasError = true;
					} else {
						asm.setAssignmentId(id);
					}
				}

				String sEmpId = cols[1].trim();
				Integer empId = parseInteger(sEmpId);
				if (empId == null || sEmpId.length() > 5) {
					result.errors.add(new CsvRowError(rowNum, "社員ID", "社員IDの形式が正しくありません"));
					hasError = true;
				} else if (assignmentMapper.existsEmployee(empId) == 0) {
					result.errors.add(new CsvRowError(rowNum, "社員ID", "指定された社員IDは存在しません"));
					hasError = true;
				} else {
					asm.setEmpId(empId);
				}
				
				String sCompanyId = cols[2].trim();
				Integer companyId = parseInteger(sCompanyId);
				if (companyId == null || sCompanyId.length() > 5) {
					result.errors.add(new CsvRowError(rowNum, "企業ID", "企業IDの形式が正しくありません"));
					hasError = true;
				} else if (assignmentMapper.existsCompany(companyId) == 0) {
					result.errors.add(new CsvRowError(rowNum, "企業ID", "指定された企業IDは存在しません"));
					hasError = true;
				} else {
					asm.setCompanyId(companyId);
				}
				
				if (cols[3].trim().isEmpty()) {
					result.errors.add(new CsvRowError(rowNum, "社員姓", "社員姓は必須です"));
					hasError = true;
				}
				
				if (cols[4].trim().isEmpty()) {
					result.errors.add(new CsvRowError(rowNum, "社員名", "社員名は必須です"));
					hasError = true;
				}

				if (cols[5].trim().isEmpty()) {
					result.errors.add(new CsvRowError(rowNum, "企業名", "企業名は必須です"));
					hasError = true;
				}

				String sStart = cols[8].trim();
				if (sStart.isEmpty()) {
					result.errors.add(new CsvRowError(rowNum, "契約開始日", "契約開始日は必須です"));
					hasError = true;
				} else {
					start = parseDate(sStart);
					if (start == null) {
						result.errors.add(new CsvRowError(rowNum, "契約開始日/契約終了日", "日付の形式が正しくありません"));
						hasError = true;
					} else {
						asm.setContractStartDate(start);
					}
				}

				String sEnd = cols[9].trim();
				if (!sEnd.isEmpty() && !"ー".equals(sEnd) && !"-".equals(sEnd)) {
					end = parseDate(sEnd);
					if (end == null) {
						result.errors.add(new CsvRowError(rowNum, "契約開始日/契約終了日", "日付の形式が正しくありません"));
						hasError = true;
					} else {
						asm.setContractEndDate(end);
					}
				}

				if (start != null && end != null && start.isAfter(end)) {
					result.errors.add(new CsvRowError(rowNum, "契約終了日", "契約開始日より前の日付は入力できません"));
					hasError = true;
				}

				String sPrice = cols[10].trim();
				if (sPrice.isEmpty()) {
					result.errors.add(new CsvRowError(rowNum, "契約単価", "契約単価は必須です"));
					hasError = true;
				} else if (!sPrice.matches("\\d+")) {
					result.errors.add(new CsvRowError(rowNum, "契約単価", "半角数字のみで入力してください"));
					hasError = true;
				} else if (sPrice.length() > UNIT_PRICE_MAX_DIGITS) {
					result.errors.add(new CsvRowError(rowNum, "契約単価", "契約単価は10文字以内で入力してください"));
					hasError = true;
				} else {
					BigDecimal price = new BigDecimal(sPrice);
					if (price.compareTo(BigDecimal.ZERO) <= 0) {
						result.errors.add(new CsvRowError(rowNum, "契約単価", "この値は入力できません"));
						hasError = true;
					} else {
						asm.setUnitPrice(price);
					}
				}

				String role = cols[11].trim();
				if (role.isEmpty()) {
					result.errors.add(new CsvRowError(rowNum, "役割", "役割は必須です"));
					hasError = true;
				} else {
					Integer roleId = assignmentMapper.findRoleIdByName(role);
					if (roleId == null) {
						result.errors.add(new CsvRowError(rowNum, "役割", "指定された役割は存在しません"));
						hasError = true;
					} else {
						asm.setRoleId(roleId);
					}
				}

				if (!hasError) {
					String key = asm.getEmpId() + "|" + asm.getCompanyId() + "|"
							+ asm.getContractStartDate() + "|" + asm.getContractEndDate();
					if (!seenInCsv.add(key)) {
						result.errors.add(new CsvRowError(rowNum, "-", "CSV内で重複するアサイン履歴があります"));
						hasError = true;
					}
				}

				if (!hasError && existsDuplicate(asm)) {
					result.errors.add(new CsvRowError(rowNum, "-", "既に同じ内容が登録されています"));
					hasError = true;
				}

				if (!hasError && (asm.getAssignmentId() == null)) {
					if ((assignmentCount + insertPlan) >= 500) {
						String limitError = "登録後の件数が上限に達しています。アサインの登録上限は500件です";
						result.errors.add(new CsvRowError(rowNum, "上限", limitError));
						result.limitMessage = limitError;
						hasError = true;
					}
				}

				if (!hasError) {
					try {
						boolean isNew = (asm.getAssignmentId() == null);
						save(asm);
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
			
			if (result.errorCount > 0) {
				TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
				result.successCount = 0;
			}
		}
		return result;
	}
	
	private String stripBom(String s) {
		if (s != null && !s.isEmpty() && s.charAt(0) == '\uFEFF') {
			return s.substring(1);
		}
		return s;
	}
	
	private Integer parseInteger(String s) {
		if (s == null || !s.matches("\\d+")) {
			return null;
		}
		try {
			return Integer.valueOf(s);
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
	private LocalDate parseDate(String s) {
		for (String p : new String[] { "yyyy/M/d", "yyyy-M-d" }) {
			try {
				return LocalDate.parse(s, DateTimeFormatter.ofPattern(p));
			} catch (Exception e) {

			}
		}
		return null;
	}

	/**
	 * アサイン履歴情報の重複をチェックする。
	 * @param assignment チェック対象のアサイン履歴エンティティ
	 * @return 重複していればtrue
	 */
	public boolean existsDuplicate(Assignment assignment) {
	    int count = assignmentMapper.countDuplicate(
	            assignment.getAssignmentId(),
	            assignment.getEmpId(),
	            assignment.getCompanyId(),
	            assignment.getContractStartDate(),
	            assignment.getContractEndDate());
	    return count > 0;
	}
	
	/**
	 * アサイン履歴情報の件数が上限に達しているかを判定する。
	 * @return 上限に達していればtrue
	 */
	public boolean isMaxCount() {
		return assignmentMapper.countAll() >= 500;
	}
	
	/**
	 * 指定された社員IDのデータを論理削除する。
	 *
	 * @param id 削除対象の社員ID
	 */
	@Transactional
	public void deleteByEmpId(Integer id) {
		assignmentMapper.deleteByEmpId(id);
	}
	
	/**
	 * 指定された複数の社員IDのデータを一括で論理削除する。
	 *
	 * @param ids 削除対象となる社員IDのリスト
	 */
	@Transactional
	public void deleteBulkByEmpId(List<Integer> ids) {
		assignmentMapper.deleteBulkByEmpId(ids);
	}
	
	
}
