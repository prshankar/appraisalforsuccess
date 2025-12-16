package com.cfs.pojo.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "skill")
public class Skill extends EntityLog implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@Column(name = "pdp_id")
	private Long pdpId;

	@Column(name = "skill_order")
	private Long skillOrder;

	@Column(name = "skills_to_be_developed")
	private String skillsToBeDeveloped;

	@Column(name = "how_skills_to_be_developed")
	private String howSkillsToBeDeveloped;

	@Column(name = "assistance_required")
	private String assistanceRequired;

	@Column(name = "whenn")
	private String whenn;
}