package com.cfs.pojo.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAppraisalDto {

	private Long userId;
	private String year;
	private String status;

	public UserAppraisalDto(Long userId, String year, String status) {
		this.userId = userId;
		this.year = year;
		this.status = status;
	}
	
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
