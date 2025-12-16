package com.cfs.pojo.entity;

import java.io.Serializable;
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
@Table(name = "appraisal")
public class Appraisal extends EntityLog implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id")
	private Long userId;

	@Column(name = "user_appraisal_id")
	private Long userAppraisalId;
	
	@Column(name = "value_id")
	private Long valueId;

	@Column(name = "task_id")
	private Long taskId;

	@Column(name = "year")
	private String year;

	@Column(name = "exceeds_employee")
	private Boolean exceedsEmployee;

	@Column(name = "exceeds_manager")
	private Boolean exceedsManager;

	@Column(name = "meets_employee")
	private Boolean meetsEmployee;

	@Column(name = "meets_manager")
	private Boolean meetsManager;

	@Column(name = "doesnotmeet_employee")
	private Boolean doesnotmeetEmployee;

	@Column(name = "doesnotmeet_manager")
	private Boolean doesnotmeetManager;

	@Column(name = "status")
	private String status;

	@OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "value_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Value value;

	@OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "task_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Task task;

	@OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
    private User user;

	@OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "appraisal_id")
    private List<AppraisalMessage> appraisalMessages = new  ArrayList<AppraisalMessage>();
}