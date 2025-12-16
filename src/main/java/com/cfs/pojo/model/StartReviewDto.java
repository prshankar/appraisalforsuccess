
package com.cfs.pojo.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StartReviewDto {

	@JsonProperty("emailId")
	private String emailId;

	@JsonProperty("startDate")
	private String startDate;

	@JsonProperty("reviewerName")
	private String reviewerName;

	@JsonProperty("year")
	private String year;
}
