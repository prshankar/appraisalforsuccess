package com.cfs.pojo.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "task")
public class Task {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@Column(name = "value_id")
	private Long valueId;

	@Column(name = "sort_order")
	private Long sortOrder;

	@Column(name = "name")
	private String name;

	@ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "value_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Value value;
}