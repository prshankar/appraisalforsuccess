package com.cfs.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.cfs.pojo.entity.PersonalDevelopmentPlan;

@Repository
public interface PdpRepository extends CrudRepository<PersonalDevelopmentPlan, Long> {

	@Query(value = "Select pdp from PersonalDevelopmentPlan pdp where pdp.userId=:userId and pdp.year=:year")
	public Optional<PersonalDevelopmentPlan> findByUserIdAndYear(Long userId, String year);
}
