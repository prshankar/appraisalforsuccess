package com.cfs.pojo.model;

import java.io.Serializable;
import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse implements Serializable {

	private static final long serialVersionUID = -8091879091924046844L;
	private Long userId;
	private List<Long> organisationId;
	private String role;
	private String userName;
	private String userType;
	private String country;
	private String jwttoken;
	private UserDetails UserDetails;
	private String userAccess;
}