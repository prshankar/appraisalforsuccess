package com.cfs.pojo.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppraisalDto {

	private Long id;
	private Long valueId;
	private Long taskId;
	private Boolean exceedsEmployee;
	private Boolean exceedsManager;
	private Boolean meetsEmployee;
	private Boolean meetsManager;
	private Boolean doesNotMeetEmployee;
	private Boolean doesNotMeetManager;
	private String status;
	private String header;
	private String name;
	private String lastComment;
	private String lastCommentedBy;
	private String lastCommentDate;
}
