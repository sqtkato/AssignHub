package com.assignhub.form;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class AssignmentForm {
	/** アサインID */
    private Integer assignmentId;

    /** 社員名 */
    @NotNull(message = "社員名は必須です")
    private Integer empId;

    /** 企業名 */
    @NotNull(message = "企業名を選択してください")
    private Integer companyId;

    /** 契約開始日 */
    @NotNull(message = "契約開始日を選択してください")
    private LocalDate contractStartDate;

    /** 契約終了日 */
    private LocalDate contractEndDate;

    /** 契約単価 */
    @NotNull(message = "契約単価は必須です")
    @DecimalMin(value = "1", message = "この値は入力できません")
    @Digits(integer = 10, fraction = 0, message = "半角数字のみで入力してください")
    private BigDecimal unitPrice;

    /** 役割 */
    @NotNull(message = "役割を選択してください")
    private Integer roleId;
}
