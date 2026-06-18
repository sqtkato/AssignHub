package com.assignhub.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.assignhub.entity.Company;
import com.assignhub.mapper.CompanyMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * 企業情報（顧客・パートナー）管理に関するビジネスロジックを提供するサービスクラス。
 * * @version 1.00 2026/06/01
 * @author SQT）チームC
 */
@Slf4j
@Service
public class CompanyService {

	private final CompanyMapper companyMapper;

	/**
	 * コンストラクタによる依存性の注入。
	 *
	 * @param companyMapper 企業マスタに対するマッパー
	 */
	public CompanyService(CompanyMapper companyMapper) {
		this.companyMapper = companyMapper;
	}

	/**
	 * 検索条件およびソート条件に合致する企業情報を全件取得する。
	 *
	 * @param keyword 検索キーワード（企業名の部分一致）
	 * @param sort    ソート対象のカラム名
	 * @param order   昇順（asc）または降順（desc）
	 * @return 企業エンティティのリスト
	 */
	public List<Company> findAll(String companyName, String companyTel) {
		return companyMapper.findAll(companyName,companyTel);
	}
	
	public List<Company> findByIds(List<Integer> ids) {
		return companyMapper.findByIds(ids);
	}
	/**
	 * 企業IDを指定して、企業情報を1件取得する。
	 *
	 * @param id 取得対象の企業ID
	 * @return 該当する企業エンティティ（存在しない、または論理削除済みの場合はnull）
	 */
	public Company findById(Integer id) {
		return companyMapper.findById(id);
	}
	public boolean isMaxCount() {
		return companyMapper.countAll()>=500;
	}

	
	/**
	 * 企業情報を保存する。
	 * IDが存在しない場合（nullまたは0）は新規登録（INSERT）、存在する場合は更新（UPDATE）を行う。
	 *
	 * @param company 登録または更新する企業エンティティ
	 */
	@Transactional(rollbackFor = Exception.class)
	public void save(Company company) {
		if (company.getCompanyId() == null || company.getCompanyId() == 0) {
			companyMapper.insert(company);
		} else {
			companyMapper.update(company);
		}
	}
	
	/**
	 * 企業名がすでに登録されているか（重複しているか）を判定する。
	 *
	 * @param 企業名 チェックする企業名
	 * @param excludecompanyId 除外する企業ID（新規登録時はnullを渡す）
	 * @return 重複していればtrue
	 */
	public boolean isDuplicate(String companyName,String companyTel,String companyFax, Integer companyId) {
		return companyMapper.existsByCompanyNameAndTelAndFax(companyName, companyTel,companyFax,companyId);
	}

	public void delete(Integer id) {
		companyMapper.delete(id);

	}

	/**
	 * 指定された複数の企業IDのデータを一括で物理削除する。
	 *
	 * @param ids 削除対象となる社員IDのリスト
	 */
	@Transactional
	public void deleteBulk(List<Integer> ids) {
		if (ids != null && !ids.isEmpty()) {
			companyMapper.deleteBulk(ids);
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
		
		int companyCount = companyMapper.countAll();
		int insertPlan = 0;
		
		java.nio.charset.CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
		        .onMalformedInput(java.nio.charset.CodingErrorAction.REPORT)
		        .onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPORT);
		try (BufferedReader br = new BufferedReader(
		        new InputStreamReader(file.getInputStream(), decoder))) {
			String line;
			int rowNum = 1;
			boolean isFirstLine = true;
			
			 int currentCount = companyMapper.countAll();
			
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
				if (cols.length < 14) {
					result.errors.add(new CsvRowError(rowNum, "全体", "項目数が不足しています（14項目必要）"));
					result.errorCount++;
					rowNum++;
					continue;
				}

				boolean hasError = false;
				Company company = new Company();
				Integer parsedId = null;
				
				
				//企業ID

				if (!cols[0].trim().isEmpty()) {
					try {
						parsedId = Integer.parseInt(cols[0].trim());
						if (findById(parsedId) == null) {
							result.errors.add(new CsvRowError(rowNum, "企業ID",
									"指定された企業ID（" + parsedId + "）は存在しません"));
							hasError = true;
						} else {
							company.setCompanyId(parsedId);
						}
					} catch (Exception e) {
						result.errors.add(new CsvRowError(rowNum, "企業ID", "企業形式が正しくありません"));
						hasError = true;
					}
				}
				
				
				//企業名
				String companyName = cols[1].trim();
				if (companyName.isEmpty()) {
					result.errors.add(new CsvRowError(rowNum, "企業名", "企業名は必須です"));
					hasError = true;
				} else if (companyName.length() > 50) {
	                result.errors.add(new CsvRowError(rowNum, "企業名", "企業名は50文字以内で入力してください"));
	                hasError = true;
				} else {
					company.setCompanyName(companyName);
				}
				
				//企業名カナ
				String companyNameKana = cols[2].trim();
				if (companyNameKana.isEmpty()) {
					result.errors.add(new CsvRowError(rowNum, "企業名カナ", "企業名カナは必須です"));
					hasError = true;
				} else if (companyNameKana.length() > 100) {
	                result.errors.add(new CsvRowError(rowNum, "企業名カナ", "企業名カナは100文字以内で入力してください"));
	                hasError = true;
				} else {
					company.setCompanyNameKana(companyNameKana);
				}
				
				
				//設立年度
				String foundedYear = cols[3].trim();
				 if (!foundedYear.isEmpty()) {
		                if (foundedYear.length() > 4) {
		                    result.errors.add(new CsvRowError(rowNum, "設立年度", "設立年度は4文字以内で入力してください"));
		                    hasError = true;
		                } else if (Integer.parseInt(foundedYear) > java.time.Year.now().getValue()) {
		                    result.errors.add(new CsvRowError(rowNum, "設立年度", "設立年度の形式が不正です"));
		                    hasError = true;
		                }
				 } else {
				        int year = Integer.parseInt(foundedYear);
				        int currentYear = java.time.Year.now().getValue();
				        if (year > currentYear) {
				            result.errors.add(new CsvRowError(rowNum, "設立年度", "設立年度は" + currentYear + "年以前で入力してください"));
				            hasError = true;
				        }
		                else {
		                    company.setFoundedYear(Integer.parseInt(foundedYear));
		                }
		            }
				 
				 //社員数
				 String employeeCount = cols[4].trim();
		            if (!employeeCount.isEmpty()) {
		                if (employeeCount.length() > 5) {
		                    result.errors.add(new CsvRowError(rowNum, "社員数", "社員数は5文字以内で入力してください"));
		                    hasError = true;
		                } else {
		                	company.setEmployeeCount(Integer.parseInt(employeeCount));
		                }
		            }
		            // 郵便番号
		            String companyZipCode = cols[5].trim();
		            if (companyZipCode.isEmpty()) {
		                result.errors.add(new CsvRowError(rowNum, "郵便番号", "郵便番号は必須です"));
		                hasError = true;
		            }
		            else if (!companyZipCode.matches("^[0-9]*$")) {
		                result.errors.add(new CsvRowError(rowNum, "郵便番号", "郵便番号の形式が正しくありません ハイフンなしで入力してください"));
		                hasError = true;
		            }else if (companyZipCode.length() != 7) {
		                result.errors.add(new CsvRowError(rowNum, "郵便番号", "郵便番号は7桁以内で入力してください"));
		                hasError = true;
		            }else {
		                company.setCompanyZipCode(companyZipCode);
		            }

		            // 住所1
		            String companyAddress1 = cols[6].trim();
		            if (companyAddress1.isEmpty()) {
		                result.errors.add(new CsvRowError(rowNum, "住所1", "住所1は必須です"));
		                hasError = true;
		            } else if (companyAddress1.length() > 100) {
		                result.errors.add(new CsvRowError(rowNum, "住所1", "住所1は100文字以内で入力してください"));
		                hasError = true;
		            } else {
		                company.setCompanyAddress1(companyAddress1);
		            }

		            // 住所2
		            String companyAddress2 = cols[7].trim();
		            if (companyAddress2.length() > 100) {
		                result.errors.add(new CsvRowError(rowNum, "住所2", "住所2は100文字以内で入力してください"));
		                hasError = true;
		            } else {
		                company.setCompanyAddress2(companyAddress2);
		            }

		            // 電話番号
		            String companyTel = cols[8].trim();
		            if (companyTel.isEmpty()) {
		                result.errors.add(new CsvRowError(rowNum, "電話番号", "電話番号は必須です"));
		                hasError = true;
		            }  if ((companyTel.length() < 10) || (companyTel.length() > 11)) {
		                result.errors.add(new CsvRowError(rowNum, "電話番号", "電話番号10桁または11桁で入力してください"));
		                hasError = true;
		            }   if (!companyTel.matches("^[0-9]*$")) {
		                result.errors.add(new CsvRowError(rowNum, "電話番号", "電話番号の形式が正しくありません ハイフンなしで入力してください"));
		                hasError = true;
		            } else {
		                company.setCompanyTel(companyTel);
		            }

		            // FAX
		            String companyFax = cols[9].trim();
		            if (!companyFax.isEmpty()) {
		                if (companyFax.length() > 20) {
		                    result.errors.add(new CsvRowError(rowNum, "FAX", "FAXは20桁以内で入力してください"));
		                    hasError = true;
		                } else if (!companyFax.matches("\\d+")) {
		                    result.errors.add(new CsvRowError(rowNum, "FAX", "FAX番号の形式が正しくありません ハイフンなしで入力してください"));
		                    hasError = true;
		                } else {
		                    company.setCompanyFax(companyFax);
		                }
		            }
		            
		         // 重複チェック（企業名＋TEL＋FAXの組み合わせ）
		            if (!hasError) {
		                String faxForCheck = companyFax.isEmpty() ? null : companyFax;
		                if (isDuplicate(companyName, companyTel, faxForCheck, parsedId)) {
		                    result.errors.add(new CsvRowError(rowNum, "企業名/TEL/FAX",
		                        "この企業名は既に使用されています"));
		                    hasError = true;
		                }
		            }

		            // 代表者姓
		            String repLast = cols[10].trim();
		            if (repLast.length() > 50) {
		                result.errors.add(new CsvRowError(rowNum, "代表者姓", "代表者姓は50文字以内で入力してください"));
		                hasError = true;
		            } else {
		                company.setRepLastName(repLast);
		            }

		            // 代表者名
		            String repFirst = cols[11].trim();
		            if (repFirst.length() > 50) {
		                result.errors.add(new CsvRowError(rowNum, "代表者名", "代表者名は50文字以内で入力してください"));
		                hasError = true;
		            } else {
		                company.setRepFirstName(repFirst);
		            }

		            // 代表者姓カナ
		            String repLastKana = cols[12].trim();
		            if (repLastKana.length() > 100) {
		                result.errors.add(new CsvRowError(rowNum, "代表者姓カナ", "代表者姓カナは100文字以内で入力してください"));
		                hasError = true;
		            } else {
		                company.setRepLastNameKana(repLastKana);
		            }

		            // 代表者名カナ
		            String repFirstKana = cols[13].trim();
		            if (repFirstKana.length() > 100) {
		                result.errors.add(new CsvRowError(rowNum, "代表者名カナ", "代表者名カナは100文字以内で入力してください"));
		                hasError = true;
		            } else {
		                company.setRepFirstNameKana(repFirstKana);
		            }

		           
					// 登録上限チェック
		            if (!hasError && (parsedId == null)) {
		        		    if((companyCount + insertPlan) >= 500) {
		        	        result.errors.add(new CsvRowError(rowNum, "上限",
		        	        "登録後の件数が上限に達しています。企業情報の登録上限は500件です。"));
		        	     hasError = true;
		        	     }
		        	 }
		        				if (!hasError) {
		        					try {
		        						save(company);
		        						result.successCount++;
		        						if (parsedId == null) insertPlan++;
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

		
}