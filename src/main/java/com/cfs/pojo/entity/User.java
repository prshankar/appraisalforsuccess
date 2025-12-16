package com.cfs.pojo.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user")
public class User extends EntityLog implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	/*
	 CFS-TODO
	@OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_type_id", referencedColumnName = "id")
    private UserType userType;*/

	@OneToMany
    @JoinTable(name = "user_organisation",
            joinColumns = {
            @JoinColumn(name = "user_id")
            },
            inverseJoinColumns = {
            @JoinColumn(name = "organisation_id") })
	private List<Organisation> organzations;

	@Column(name = "first_name")
	private String firstName;

	@Column(name = "last_name")
	private String lastName;

	@Column(name = "employee_number")
	private String employeeNumber;

	@Column(name = "employee_position_id")
	private Long employeePositionId;

	@Column(name = "review_date")
	private String reviewDate;

	@Column(name = "previous_position")
	private String previousPosition;

	@Column(name = "department")
	private String department;

	@Column(name = "date_joined")
	private String dateJoined;

	@Column(name = "reviewer_id")
	private Long reviewerId;

	@Column(name = "dob")
	private String dob;

	@Column(name = "primary_email")
	private String primaryEmail;

	@Column(name = "mobile_number")
	private String mobileNumber;

	@Column(name = "password")
	private String password;

	@Column(name = "status")
	private String status;

	@Column(name = "login_expiry")
	private String loginExpiry;

	@Column(name = "password_retries_left")
	private Long passwordRetriesLeft = new Long(5);

	@Column(name = "password_attempt_time")
	private String passwordAttemptTime;

	@Column(name = "reset_token")
	private String resetToken;
	
	@Column(name = "reset_token_expiry")
	private String resetTokenExpiry;

	@Column(name = "first_password_set")
	private String firstPasswordSet;
	
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "USER_ROLE",
            joinColumns = {
            @JoinColumn(name = "USER_ID")
            },
            inverseJoinColumns = {
            @JoinColumn(name = "ROLE_ID") })
    private Role role;
}