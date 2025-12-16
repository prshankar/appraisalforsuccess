
package com.cfs.dao;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.cfs.pojo.entity.Appraisal;

@Repository
public interface AppraisalRepository extends CrudRepository<Appraisal, Long> {

	//@Query(value = "Select a from Appraisal a where a.userId=:userId and a.year=:year and a.valueId=(Select min(a.valueId) from Appraisal a)")
	@Query(value = "Select a from Appraisal a where a.userId=:userId and a.year=:year")
	public List<Appraisal> findByUserIdAndYear(Long userId, String year);

	@Query(value = "Select a from Appraisal a where a.userId=:userId and a.valueId=:valueId and a.year=:year")
	public List<Appraisal> findByUserAndAppraisalYearAndValue(Long userId, Long valueId, String year);

	@Query(value = "Select a from Appraisal a where a.userId=:userId  and a.valueId=:valueId and a.taskId=:taskId and a.year=:year")
	public Optional<Appraisal> findByUserAndAppraisalYearAndValueAndTask(Long userId, Long valueId, Long taskId, String year);

	@Query(value = "Select distinct(a.valueId) from Appraisal a where a.userId=:userId and a.year=:year")
	public List<String> getValueIds(Long userId, String year);
	
	@Transactional
	@Modifying
	@Query(value = "delete from Appraisal a where a.userId=:userId")
	public void deleteByUserId(Long userId);
}
