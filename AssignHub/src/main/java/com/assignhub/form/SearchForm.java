package com.assignhub.form;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class SearchForm {
	
	private String txtEmpName;
    private String txtAssignName;
    private String txtCompanyName;
    
	/** 契約期間（チェック対象） */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate txtContractStartDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate txtContractEndDate;
}