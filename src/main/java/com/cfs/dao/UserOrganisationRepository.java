package com.cfs.dao;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.cfs.pojo.entity.UserOrganisation;

@Repository
public interface UserOrganisationRepository extends PagingAndSortingRepository<UserOrganisation, Long> {

	@Transactional
	@Modifying
	@Query(value = "delete from UserOrganisation uo where uo.userId=:userId")
	public void deleteByUserId(Long userId);
}
