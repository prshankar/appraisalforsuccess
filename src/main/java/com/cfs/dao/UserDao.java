package com.cfs.dao;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.cfs.pojo.entity.User;

@Repository
public interface UserDao extends PagingAndSortingRepository<User, Long> {
	
	User findByPrimaryEmail(String primaryEmail);
}