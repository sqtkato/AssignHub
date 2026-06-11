package com.assignhub.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Assignment {
	/** アサインID（主キー） */
    private Integer assignmentId;

    /** 社員ID（外部キー） */
    private Integer empId;

    /** 企業ID（外部キー） */
    private Integer companyId;

    /** 契約開始日 */
    private LocalDate contractStartDate;

    /** 契約終了日 */
    private LocalDate contractEndDate;

    /** 契約単価 */
    private BigDecimal unitPrice;

    /** 役割ID（外部キー） */
    private Integer roleId;

    /** 削除フラグ */
    private Boolean deleteFlg;

    /** 作成日時 */
    private LocalDateTime createdAt;

    /** 更新日時 */
    private LocalDateTime updatedAt;
    
    /** 社員名（表示用） */
    private String empName;

    /** 社員名カナ（表示用） */
    private String empNameKana;

    /** 所属企業名（表示用） */
    private String companyName;

    /** 所属企業名カナ（表示用） */
    private String companyNameKana;

    /** アサイン先企業名（表示用） */
    private String assignCompanyName;

    /** エンジニアタイプ（表示用） */
    private String engineerType;

    /** 役割名（表示用） */
    private String role;
}
