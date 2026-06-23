package com.assignhub.form;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

@Data
public class SearchForm {
	
	private String empName;
    private String assignName;
    private String companyName;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate contractStartDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate contractEndDate;

    @AssertTrue(message = "契約開始日は契約終了日以前の日付を入力してください。")
    public boolean isContractPeriodValid() {
        if (contractStartDate == null || contractEndDate == null) return true;
        return !contractStartDate.isAfter(contractEndDate);
    }
}