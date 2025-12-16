package com.cfs.dao;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.cfs.pojo.entity.UserAppraisal;

@Repository
public interface UserAppraisalRepository extends PagingAndSortingRepository<UserAppraisal, Long> {

	@Query(value = "Select ua from UserAppraisal ua where ua.userId=:userId and ua.year=:year")
	public Optional<UserAppraisal> findByUserIdAndYear(Long userId, String year);

	@Transactional
	@Modifying
	@Query(value = "delete from UserAppraisal ua where ua.userId=:userId")
	public void deleteByUserId(Long userId);

	@Query(value = "Select ua from UserAppraisal ua where ua.year=:year and ua.status=:status and ua.reviewDate IS NOT NULL")
	public List<UserAppraisal> findAllReadyForReviewUsers(String year, String status);

	@Query(value = "Select ua from UserAppraisal ua where ua.userId=:userId")
	public List<UserAppraisal> findByUserId(Long userId);
}
