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
    private Integer deleteFlg;

    /** 作成日時 */
    private LocalDateTime createdAt;

    /** 更新日時 */
    private LocalDateTime updatedAt;
    
    /** 結合先の社員情報 */
	private Employee employee;
	
	/** 結合先のアサイン先企業情報 */
	private Company company;
	
	/** 結合先の役割情報 */
	private Role role;
}

