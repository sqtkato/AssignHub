package com.assignhub.entity;

import lombok.Data;

@Data
public class Role {
	/** 役割ID（主キー） */
	private Integer roleId;

	/** 役割 */
	private String role;
}