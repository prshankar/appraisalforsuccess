package com.cfs.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.cfs.pojo.entity.Task;

@Repository
public interface TaskRepository extends PagingAndSortingRepository<Task, Long> {

	@Query(value = "Select t from Task t where t.valueId=:valueId order by id")
	public List<Task> findByValueId(Long valueId);

	@Query(value = "Select t from Task t where t.name=:name and t.valueId=:valueId")
	public Optional<Task> findByNameAndValueId(String name, Long valueId);
}

