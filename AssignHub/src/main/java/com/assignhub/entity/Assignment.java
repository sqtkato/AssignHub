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

    /** 会社ID（外部キー） */
    private Integer companyId;

    /** 作成日時 */
    private LocalDateTime createdAt;

    /** 更新日時 */
    private LocalDateTime updatedAt;

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
    
    /** 社員名（表示用） */
    private String empName;
    
	/** 企業名（表示用） */
    private String companyName;
    
    /** 役割名（表示用） */
    private String roleName;
}
