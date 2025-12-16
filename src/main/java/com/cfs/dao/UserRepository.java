package com.cfs.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.cfs.pojo.entity.User;

@Repository
public interface UserRepository extends PagingAndSortingRepository<User, Long> {

	//@Query(value = "Select u from User u where lower(u.primaryEmail)=:primaryEmail")
	@Query(value = "Select u from User u where u.primaryEmail=:primaryEmail")
	public Optional<User> findByPrimaryEmail(String primaryEmail);

	@Query(value = "Select u from User u, UserOrganisation ua where u.id=ua.userId and ua.organisationId=:organisationId")
	public List<User> findByOrganisationId(Long organisationId);

	@Query(value = "Select u from User u, UserOrganisation ua where u.id=ua.userId and ua.organisationId IN (:organisationIds)")
	public List<User> findByOrganisationIds(Long[] organisationIds, Pageable pageable);
}
