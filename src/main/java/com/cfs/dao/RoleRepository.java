package com.cfs.dao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.cfs.pojo.entity.Role;

@Repository
public interface RoleRepository extends CrudRepository<Role, Long> {

	Role findByName(String name);

	Role findRoleByName(String name);

	@Query(value = "Select r from Role r where r.name=:roleName")
	public Role findByRoleName(String roleName);
}