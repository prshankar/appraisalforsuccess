package com.cfs.pojo.model;

import java.util.ArrayList;
import java.util.List;

import com.cfs.pojo.entity.Goal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PerformanceGoalDtos {

	private Long organisationId;
	private String year;
	private List<PerformanceGoalDto> performanceGoalDto = new ArrayList<PerformanceGoalDto>();
	private List<Goal> goals = new ArrayList<Goal>();
}
