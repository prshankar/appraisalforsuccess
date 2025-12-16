package com.cfs.pojo.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "employee_position", uniqueConstraints={@UniqueConstraint(columnNames ={"name"})})
public class EmployeePosition implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@Column(name = "organisation_id")
	private Long organisationId;

	@Column(name = "name")
	private String name;

	@OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "organisation_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Organisation organisation;
}