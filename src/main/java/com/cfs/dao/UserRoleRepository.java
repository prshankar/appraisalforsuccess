package com.cfs.dao;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.cfs.pojo.entity.UserRole;

@Repository
public interface UserRoleRepository extends PagingAndSortingRepository<UserRole, Long> {
}
