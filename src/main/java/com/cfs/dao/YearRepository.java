package com.cfs.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.cfs.pojo.entity.Year;

@Repository
public interface YearRepository extends PagingAndSortingRepository<Year, Long> {

	@Query(value = "Select y from Year y where y.year=:year")
	public Optional<Year> findByYear(String year);

	@Query(value = "Select y from Year y where y.status=ACTIVE")
	public List<Year> findYears();

}
