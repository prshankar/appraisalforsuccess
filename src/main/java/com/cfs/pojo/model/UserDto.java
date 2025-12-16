
package com.cfs.pojo.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {

	@JsonProperty("id")
	private Long id;

	@JsonProperty("organisationId")
	private List<String> organisationId;

	@JsonProperty("firstName")
	private String firstName;

	@JsonProperty("lastName")
	private String lastName;

	@JsonProperty("employeeNumber")
	private String employeeNumber;

	@JsonProperty("employeePositionId")
	private Long employeePositionId;

	@JsonProperty("employeePosition")
	private String employeePosition;

	@JsonProperty("reviewDate")
	private String reviewDate;

	@JsonProperty("previousPosition")
	private String previousPosition;

	@JsonProperty("department")
	private String department;

	@JsonProperty("dateJoined")
	private String dateJoined;

	@JsonProperty("reviewerId")
	private Long reviewerId;

	@JsonProperty("reviewersName")
	private String reviewersName;

	@JsonProperty("dob")
	private String dob;

	@JsonProperty("primaryEmail")
	private String primaryEmail;

	@JsonProperty("mobileNumber")
	private String mobileNumber;

	@JsonProperty("password")
	private String password;

	@JsonProperty("status")
	private String status;

	@JsonProperty("login_expiry")
	private String loginExpiry;

	@JsonProperty("password_retries_left")
	private Long passwordRetriesLeft = new Long(5);

	@JsonProperty("password_attempt_time")
	private String passwordAttemptTime;

	@JsonProperty("reset_token")
	private String resetToken;
	
	@JsonProperty("reset_token_expiry")
	private String resetTokenExpiry;

	@JsonProperty("employeeType")
	private String employeeType;

	@JsonProperty("saveType")
	private String saveType;
}
