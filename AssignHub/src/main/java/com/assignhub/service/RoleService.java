package com.assignhub.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.assignhub.entity.Role;
import com.assignhub.mapper.RoleMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RoleService {
	private final RoleMapper roleMapper;

	public RoleService(RoleMapper roleMapper) {
		this.roleMapper = roleMapper;
	}

	public List<Role> findAll() {
		return roleMapper.findAll();
	}

}
