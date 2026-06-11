package com.assignhub.form;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class SearchForm {
	
	private String empName;
    private String assignName;
    private String companyName;
    
	/** 契約期間（チェック対象） */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate contractStartDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate contractEndDate;
}