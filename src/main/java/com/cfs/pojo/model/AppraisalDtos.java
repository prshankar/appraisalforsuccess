package com.cfs.pojo.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppraisalDtos {

	private Long appraisalId;
	private Long appraisalUserId;
	private Long userId;
	private Long valueId;
	private String year;
	private String status;
	private List<AppraisalDto> appraisalDto = new ArrayList<AppraisalDto>();
	private String screenLock;
}
