package com.cfs.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.cfs.pojo.entity.Value;

@Repository
public interface ValueRepository extends PagingAndSortingRepository<Value, Long> {

	@Query(value = "Select distinct(v.id) from Value v")
	public List<String> getValueIds();

	@Query(value = "Select v.id from Value v where v.organisationId=:organisationId and v.employeePositionId=:employeePositionId")
	public List<String> getValueIds(Long organisationId, Long employeePositionId);
	
	@Query(value = "Select v from Value v where v.name=:name")
	public Optional<Value> findByName(String name);

	@Query(value = "Select v from Value v where v.organisationId=:organisationId")
	public List<Value> findByOrganisation(Long organisationId);
	
	@Query(value = "Select v from Value v where v.organisationId=:organisationId and v.employeePositionId=:employeePositionId")
	public List<Value> findByOrganisationAndEmployeePosition(Long organisationId, Long employeePositionId);
}
