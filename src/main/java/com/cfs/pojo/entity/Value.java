package com.cfs.pojo.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "value")
public class Value {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@Column(name = "organisation_id")
	private Long organisationId;

	@Column(name = "employee_position_id")
	private Long employeePositionId;

	@Column(name = "sort_order")
	private Long sortOrder;

	@Column(name = "name")
	private String name;

	@OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_position_id", referencedColumnName = "id", insertable = false, updatable = false)
    private EmployeePosition employeePosition;

	@OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "value_id")
    private List<Task> tasks = new  ArrayList<Task>();
}