package com.cfs.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.cfs.pojo.entity.AppraisalMessage;

@Repository
public interface AppraisalMessageRepository extends CrudRepository<AppraisalMessage, Long> {

	@Query(value = "Select a from AppraisalMessage a where a.appraisalId=:appraisalId order by a.creationDate ASC")
	public List<AppraisalMessage> findByAppraisalId(Long appraisalId);
}
