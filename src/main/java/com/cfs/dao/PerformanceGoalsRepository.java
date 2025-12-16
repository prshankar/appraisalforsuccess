package com.cfs.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.cfs.pojo.entity.PerformanceGoals;

@Repository
public interface PerformanceGoalsRepository extends PagingAndSortingRepository<PerformanceGoals, Long> {

	@Query(value = "Select pg from PerformanceGoals pg where pg.organisationId=:organisationId and pg.year=:year")
	public List<PerformanceGoals> findByOrganisationIdAndYear(Long organisationId, String year);

	@Query(value = "Select DISTINCT(pg.year) from PerformanceGoals pg where pg.organisationId=:organisationId")
	public List<String> findYears(Long organisationId);
}
