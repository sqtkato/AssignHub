package com.assignhub.service;

import java.util.List;

import com.assignhub.entity.Role;
import com.assignhub.mapper.RoleMapper;

public class RoleService {
	private final RoleMapper roleMapper;

	public RoleService(RoleMapper roleMapper) {
		this.roleMapper = roleMapper;
	}

	public List<Role> findAll() {
		return roleMapper.findAll();
	}

}
