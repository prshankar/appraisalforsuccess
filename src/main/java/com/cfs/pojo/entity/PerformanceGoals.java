package com.cfs.pojo.entity;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "performance_goals")
public class PerformanceGoals extends EntityLog implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@Column(name = "organisation_id")
	private Long organisationId;

	@Column(name = "year")
	private String year;

	@Column(name = "goal_id")
	private Long goalId;

	@Column(name = "result")
	private String result;

	/*@OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "goal_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Goal goal;*/
}