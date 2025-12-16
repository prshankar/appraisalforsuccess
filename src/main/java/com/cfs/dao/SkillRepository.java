package com.cfs.dao;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.cfs.pojo.entity.Skill;

@Repository
public interface SkillRepository extends CrudRepository<Skill, Long> {

	@Query(value = "Select s from Skill s where s.pdpId=:pdpId")
	public List<Skill> findByPdpId(Long pdpId);

	@Transactional
	@Modifying
	@Query(value = "delete from Skill s where s.pdpId=:pdpId")
	public void deleteByPdpId(Long pdpId);
}
