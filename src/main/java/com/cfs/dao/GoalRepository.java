package com.cfs.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.cfs.pojo.entity.Goal;

@Repository
public interface GoalRepository extends PagingAndSortingRepository<Goal, Long> {

	@Query(value = "Select g from Goal g where g.name=:name")
	public Optional<Goal> findByName(String name);

    List<Goal> findAllByName(String name, Pageable pageable);
}
