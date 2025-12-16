package com.cfs.pojo.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PerformanceGoalDto {

	private Long id;
	private Long goalId;
	private String goalName;
	private String result;
}
