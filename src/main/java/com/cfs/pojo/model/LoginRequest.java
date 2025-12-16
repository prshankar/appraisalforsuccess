package com.cfs.pojo.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginRequest implements Serializable {

	private static final long serialVersionUID = 5926468583005150707L;

	@JsonProperty("primaryEmail")
	private String primaryEmail;
	
	@JsonProperty("password")
	private String password;

	public LoginRequest(String primaryEmail, String password) {
		this.setPrimaryEmail(primaryEmail);
		this.setPassword(password);
	}
}