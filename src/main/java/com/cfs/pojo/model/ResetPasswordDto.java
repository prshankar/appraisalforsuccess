package com.cfs.pojo.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordDto {

	@JsonProperty("primaryEmail")
	private String primaryEmail;

	@JsonProperty("password")
	private String password;
}
