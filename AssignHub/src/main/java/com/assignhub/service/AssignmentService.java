package com.assignhub.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.assignhub.entity.Assignment;
import com.assignhub.entity.SelectOption;
import com.assignhub.mapper.AssignmentMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * AssignmentServiceは、アサイン情報の管理に関するビジネスロジックを実装するサービスクラスです。
 * このクラスは、AssignmentMapperを介してデータベース操作を行い、アサイン情報の一覧取得、新規登録などの機能を提供します。
 * 
 * @author Team Excel
 * @version 1.00 2024/06/04
 */
@Slf4j
@Service
public class AssignmentService {
	private final AssignmentMapper assignmentMapper;
	
	/**
	 * コンストラクタによる依存性の注入。
	 *
	 * @param assignmentMapper アサイン情報に対するマッパー
	 */
    public AssignmentService(AssignmentMapper assignmentMapper) {
        this.assignmentMapper = assignmentMapper;
    }

    /**
	 * アサイン情報の一覧を取得する。
	 *
	 * @param txtEmpName 社員名の検索キーワード（nullまたは空文字の場合は全件取得）
	 * @param txtCompanyName 企業名の検索キーワード（nullまたは空文字の場合は全件取得）
	 * @return アサイン情報のリスト
	 */
    public List<Assignment> findAll(String txtEmpName, String txtAssignName, String txtCompanyName, String txtContractStartDate, String txtContractEndDate) {
        return assignmentMapper.findAll(txtEmpName, txtAssignName, txtCompanyName, txtContractStartDate, txtContractEndDate);
    }

    public List<SelectOption> findEmployeeOptions() {
        return assignmentMapper.findEmployeeOptions();
    }

    public List<SelectOption> findCompanyOptions() {
        return assignmentMapper.findCompanyOptions();
    }

    public List<SelectOption> findRoleOptions() {
        return assignmentMapper.findRoleOptions();
    }
    
    /**
	 * アサイン情報をIDで取得する。
	 *
	 * @param id アサイン情報のID
	 * @return IDに対応するアサイン情報、存在しない場合はnull
	 */
    public Assignment findById(Integer id) {
        return assignmentMapper.findById(id);
    }
    
    /**
	 * アサイン情報を保存する。
	 * IDが存在しない場合（nullまたは0）は新規登録（INSERT）、存在する場合は更新（UPDATE）を行う。
	 *
	 * @param assignment 登録または更新するアサインエンティティ
	 */
    @Transactional
    public void save(Assignment assignment) {
    	if (assignment.getAssignmentId() == null || assignment.getAssignmentId() == 0) {
    		assignmentMapper.insert(assignment);
		} else {
			assignmentMapper.update(assignment);
		}
    }
    
    public void deleteById(Integer id) {
		assignmentMapper.delete(id);
	}
    
    public void deleteBulk(List<Integer> ids) {
    	assignmentMapper.deleteBulk(ids);
    }
    
    /** インポート時の各行のエラー内容を保持するクラス。 */
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
    /** インポート処理の全体結果を保持するクラス。 */
    public static class ImportResult {
        public int successCount = 0;
        public int errorCount = 0;
        public List<CsvRowError> errors = new ArrayList<>();
    }
    /** 契約単価の最大桁数 */
    private static final int UNIT_PRICE_MAX_DIGITS = 10;
    /** CSV列数（アサインID,社員ID,社員名,企業名,作成日時,更新日時,開始日,終了日,単価,役割） */
    private static final int CSV_COLUMN_COUNT = 10;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    /**
     * アップロードされたCSVを解析し、バリデーションおよび一括登録・更新を行う。
     * 1件でもエラーがあれば全体をロールバックする（all-or-nothing）。
     *
     * CSV列: [0]アサインID [1]社員ID [2]社員名 [3]アサイン先企業名 [4]作成日時 [5]更新日時
     *        [6]契約開始日 [7]契約終了日 [8]契約単価 [9]役割
     * ※[4][5]はインポートでは使用せず、DB側でNOW()を設定する。
     */
    @Transactional(rollbackFor = Exception.class)
    public ImportResult importCsv(MultipartFile file) throws Exception {
        ImportResult result = new ImportResult();
        // ファイルサイズ
        if (file.getSize() > 5L * 1024 * 1024) {
            result.errors.add(new CsvRowError(0, "全体", "ファイルサイズは5MB以内にしてください"));
            result.errorCount++;
            return result;
        }
        Set<String> seenInCsv = new HashSet<>();
        int insertPlan = 0;
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
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
                // 項目数不足（No.3）
                if (cols.length < CSV_COLUMN_COUNT) {
                    result.errors.add(new CsvRowError(rowNum, "全体", "項目数が不足しています"));
                    result.errorCount++;
                    rowNum++;
                    continue;
                }
                boolean hasError = false;
                Assignment asm = new Assignment();
                LocalDate start = null;
                LocalDate end = null;
                // [0] アサインID（空=新規 / 値あり=更新。数値であること）
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
                // [2] 社員名 必須（No.3）
                if (cols[2].trim().isEmpty()) {
                    result.errors.add(new CsvRowError(rowNum, "社員名", "社員名は必須です"));
                    hasError = true;
                }
                // [1] 社員ID 形式（No.8）＋存在（No.9）
                String sEmpId = cols[1].trim();
                Integer empId = parseInteger(sEmpId);
                if (empId == null || sEmpId.length() > 5) {
                    result.errors.add(new CsvRowError(rowNum, "社員ID/企業ID", "社員ID／企業IDの形式が正しくありません"));
                    hasError = true;
                } else if (assignmentMapper.existsEmployee(empId) == 0) {
                    result.errors.add(new CsvRowError(rowNum, "社員ID/企業ID", "指定された社員ID／企業IDは存在しません"));
                    hasError = true;
                } else {
                    asm.setEmpId(empId);
                }
                // [3] 企業名 必須（No.4）＋企業名→ID（UNIQUE）
                String companyName = cols[3].trim();
                if (companyName.isEmpty()) {
                    result.errors.add(new CsvRowError(rowNum, "企業名", "企業名は必須です"));
                    hasError = true;
                } else {
                    Integer companyId = assignmentMapper.findCompanyIdByName(companyName);
                    if (companyId == null) {
                        result.errors.add(new CsvRowError(rowNum, "企業名", "指定された企業は存在しません"));
                        hasError = true;
                    } else {
                        asm.setCompanyId(companyId);
                    }
                }
                // [6] 契約開始日 必須（No.5）＋形式（No.11）
                String sStart = cols[6].trim();
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
                // [7] 契約終了日 任意＋形式（No.11）
                String sEnd = cols[7].trim();
                if (!sEnd.isEmpty() && !"ー".equals(sEnd) && !"-".equals(sEnd)) {
                    end = parseDate(sEnd);
                    if (end == null) {
                        result.errors.add(new CsvRowError(rowNum, "契約開始日/契約終了日", "日付の形式が正しくありません"));
                        hasError = true;
                    } else {
                        asm.setContractEndDate(end);
                    }
                }
                // 開始＞終了（No.10）
                if (start != null && end != null && start.isAfter(end)) {
                    result.errors.add(new CsvRowError(rowNum, "契約終了日", "契約開始日より前の日付は入力できません"));
                    hasError = true;
                }
                // [8] 契約単価 必須（No.6）/半角数字（No.13）/桁数（No.13）/>0（No.12）
                String sPrice = cols[8].trim();
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
                // [9] 役割 必須（No.7）＋役割名→ID
                String role = cols[9].trim();
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
                // CSV内重複（No.14）
                if (!hasError) {
                    String key = asm.getEmpId() + "|" + asm.getCompanyId() + "|"
                            + asm.getContractStartDate() + "|" + asm.getContractEndDate();
                    if (!seenInCsv.add(key)) {
                        result.errors.add(new CsvRowError(rowNum, "-", "CSV内で重複するアサイン履歴があります"));
                        hasError = true;
                    }
                }
                // DB重複（No.15）
                if (!hasError && existsDuplicate(asm)) {
                    result.errors.add(new CsvRowError(rowNum, "-", "既に同じ内容が登録されています"));
                    hasError = true;
                }
                // 保存（ID無し→INSERT / ID有り→UPDATE）
                if (!hasError) {
                    try {
                        if (asm.getAssignmentId() == null || asm.getAssignmentId() == 0) {
                            insertPlan++;
                        }
                        save(asm);
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
            // 500件上限（No.16）: 現在件数 + 新規INSERT分
            if (result.errorCount == 0 && assignmentMapper.countActive() + insertPlan > 500) {
                result.errors.add(new CsvRowError(0, "-", "登録後の件数が上限に達しています。アサインの登録上限は500件です"));
                result.errorCount++;
            }
            // エラーが1件でもあればロールバック（お手本と同方式）
            if (result.errorCount > 0) {
                org.springframework.transaction.interceptor.TransactionAspectSupport.currentTransactionStatus()
                        .setRollbackOnly();
                result.successCount = 0;
            }
        }
        return result;
    }
    /** 先頭のUTF-8 BOM（\uFEFF）を除去する。 */
    private String stripBom(String s) {
        if (s != null && !s.isEmpty() && s.charAt(0) == '\uFEFF') {
            return s.substring(1);
        }
        return s;
    }
    /** 数値文字列をIntegerに変換。失敗時null。 */
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
    /** yyyy/MM/dd をLocalDateに変換。失敗時null。 */
    private LocalDate parseDate(String s) {
        try {
            return LocalDate.parse(s, DATE_FMT);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * アサイン情報の重複をチェックする。
     * @param assignment チェック対象のアサインエンティティ
     * @return
     */
	public boolean existsDuplicate(Assignment assignment) {
    	Assignment duplicate = assignmentMapper.findDuplicate(
            assignment.getAssignmentId(),
            assignment.getEmpId(),
            assignment.getCompanyId(),
            assignment.getContractStartDate(),
            assignment.getContractEndDate()
    	);

    	return duplicate != null;
	}

	public boolean isMaxCount() {
	    return assignmentMapper.countActive() >= 500;
	}
}
