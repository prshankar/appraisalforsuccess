package com.cfs.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.cfs.pojo.entity.EmployeePosition;

@Repository
public interface EmployeePositionRepository extends PagingAndSortingRepository<EmployeePosition, Long> {

	@Query(value = "Select ep from EmployeePosition ep where ep.name=:name")
	public Optional<EmployeePosition> findByName(String name);

	@Query(value = "Select ep from EmployeePosition ep where ep.organisationId IN (:organisationId)")
	public List<EmployeePosition> findByOrganisationId(Long[] organisationId);

    List<EmployeePosition> findAllByName(String name, Pageable pageable);

	@Query(value = "Select ep from EmployeePosition ep where ep.organisationId=:organisationId and ep.name=:name")
	public Optional<EmployeePosition> findByOrganisationIdAndName(Long organisationId, String name);
}