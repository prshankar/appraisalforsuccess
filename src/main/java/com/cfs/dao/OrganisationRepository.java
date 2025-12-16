package com.cfs.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.cfs.pojo.entity.Organisation;

@Repository
public interface OrganisationRepository extends PagingAndSortingRepository<Organisation, Long> {

	@Query(value = "Select org from Organisation org where org.name=:name")
	public Optional<Organisation> findByName(String name);

    List<Organisation> findAllByName(String name, Pageable pageable);
}
