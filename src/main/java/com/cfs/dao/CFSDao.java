package com.cfs.dao;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.cfs.exception.CFSException;
import com.cfs.exception.ErrorCodes;
import com.cfs.pojo.entity.Appraisal;
import com.cfs.pojo.entity.AppraisalMessage;
import com.cfs.pojo.entity.EmployeePosition;
import com.cfs.pojo.entity.Goal;
import com.cfs.pojo.entity.Organisation;
import com.cfs.pojo.entity.PerformanceGoals;
import com.cfs.pojo.entity.PersonalDevelopmentPlan;
import com.cfs.pojo.entity.Role;
import com.cfs.pojo.entity.Skill;
import com.cfs.pojo.entity.Task;
import com.cfs.pojo.entity.User;
import com.cfs.pojo.entity.UserAppraisal;
import com.cfs.pojo.entity.UserOrganisation;
import com.cfs.pojo.entity.Value;
import com.cfs.pojo.entity.Year;
import com.cfs.pojo.model.AppraisalDto;
import com.cfs.pojo.model.AppraisalDtos;
import com.cfs.pojo.model.AppraisalMessageDto;
import com.cfs.pojo.model.AppraisalMessageDtos;
import com.cfs.pojo.model.EmployeePositionDto;
import com.cfs.pojo.model.GoalDto;
import com.cfs.pojo.model.OrganisationDto;
import com.cfs.pojo.model.PdpDto;
import com.cfs.pojo.model.PerformanceGoalDto;
import com.cfs.pojo.model.PerformanceGoalDtos;
import com.cfs.pojo.model.ResetPasswordDto;
import com.cfs.pojo.model.ReviewDto;
import com.cfs.pojo.model.StartReviewDto;
import com.cfs.pojo.model.TaskDto;
import com.cfs.pojo.model.UserAppraisalDto;
import com.cfs.pojo.model.UserDto;
import com.cfs.pojo.model.ValueDto;
import com.cfs.pojo.model.YearDto;
import com.cfs.util.CFSAppraisalStatus;
import com.cfs.util.CFSEnum;
import com.cfs.util.Util;

@Component
public class CFSDao {

    private final static Logger log = Logger.getLogger(CFSDao.class.getName());

	private ModelMapper mapper = new ModelMapper();

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserOrganisationRepository userOrganisationRepository;

	@Autowired
	private AppraisalRepository appraisalRepository;

	@Autowired
	private ValueRepository valueRepository;

	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	private AppraisalMessageRepository appraisalMessageRepository;

	@Autowired
	private OrganisationRepository organisationRepository;

	@Autowired
	private GoalRepository goalRepository;

	@Autowired
	private EmployeePositionRepository employeePositionRepository;
	
	@Autowired
	private PerformanceGoalsRepository performanceGoalsRepository;

	@Autowired
	private YearRepository yearRepository;

	@Autowired
	private PdpRepository pdpRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private UserAppraisalRepository userAppraisalRepository;

    @Autowired
    private SkillRepository skillRepository;

    public UserDto getUser(Long userId) throws CFSException {
		UserDto userDto = null;
		
		try {
			Optional<User> user = userRepository.findById(userId);
			Optional<User> reviewUser = userRepository.findById(user.get().getReviewerId());

			if(user.isPresent()) {
				userDto = mapper.map(user.get(), UserDto.class);
				userDto.setReviewersName(reviewUser.get().getFirstName() + " " + reviewUser.get().getLastName());

				if(user.isPresent() && user.get().getEmployeePositionId() != null) {
					Optional<EmployeePosition> employeePosition = employeePositionRepository.findById(user.get().getEmployeePositionId());
					userDto.setEmployeePosition(employeePosition.get().getName());
				}

				return userDto;
			}

			throw new CFSException(ErrorCodes.ERROR_001, "User not found.");
		} catch (CFSException e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, "Exception : " + e);
		}
	}

    public UserDto getUserByPrimaryEmail(String primaryEmail) throws CFSException {
		UserDto userDto = null;
		
		try {
			Optional<User> user = userRepository.findByPrimaryEmail(primaryEmail);
	        if(user.isPresent()) {
				userDto = mapper.map(user.get(), UserDto.class);
	        	return userDto;
	        } else {
				throw new CFSException(ErrorCodes.ERROR_001, "User not found.");
	        }
		} catch (CFSException e) {
			log.debug(e);
			throw e;
		}
	}

	public List<User> getAppraisalUser(Long userId, Long organisationId) throws CFSException {
		try {
			Optional<User> loggedInUser = userRepository.findById(userId);
			List<User> users = userRepository.findByOrganisationId(organisationId);
			List<String> roleControl = new ArrayList<String>();
			List<User> filteredUsers = new ArrayList<User>();
			users.forEach(user -> {
				if(!user.getRole().getRoleControl().contains(loggedInUser.get().getRole().getName()) && user.getId() != loggedInUser.get().getId()) {
					filteredUsers.add(user);
				}
			});
			
			return filteredUsers;
		} catch (CFSException e) {
			e.printStackTrace();
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, "Exception : " + e);
		}
	}

	public List<Organisation> getAllOrganisations() throws CFSException {
		List<Organisation> organisations = new ArrayList<Organisation>();
		
		try {
			Iterable<Organisation> organisationItr = organisationRepository.findAll();
			organisationItr.forEach(organisation -> {
				organisations.add(organisation);
			});
		} catch (CFSException e) {
			e.printStackTrace();
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, "Exception : " + e);
		}

		return organisations;
	}

	public List<Organisation> getOrganisations(String userId) throws CFSException {
		List<Organisation> organisations = new ArrayList<Organisation>();

		try {
			Optional<User> user = userRepository.findById(Long.valueOf(userId));
			if(user.isPresent()) {
				if(user.get().getRole().getName().equals(CFSEnum.SUPERUSER.toString())) {
					organisationRepository.findAll().forEach(orgs -> {
						if(orgs != null && !orgs.getName().equals(CFSEnum.ALL.toString())) {
							organisations.add(orgs);
						}
					});
				} else {
					user.get().getOrganzations().forEach(userOrganisation -> {
						organisations.add(userOrganisation);
					});
				}
			}
		} catch (CFSException e) {
			e.printStackTrace();
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, "Exception : " + e);
		}

		return organisations;
	}

	public List<String> getRoleControl(String roleName) throws CFSException {
		List<String> roleControl = new ArrayList<String>();

		try {
			Role role = roleRepository.findByRoleName(roleName);
			if(role != null && role.getRoleControl() != null && !role.getRoleControl().equals("")) {
				roleControl= Stream.of(role.getRoleControl().split("\\|"))
					     .map(String::trim)
					     .collect(Collectors.toList());
			}
		} catch (CFSException e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, "Exception : " + e);
		}

		return roleControl;
	}

	public String getUserRole(String userId) throws CFSException {
		String userRole = "";

		try {
			Optional<User> user = userRepository.findById(Long.valueOf(userId));
			if(user.isPresent()) {
				userRole = user.get().getRole().getName();
			}
		} catch (CFSException e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, "Exception : " + e);
		}

		return userRole;
	}

	public List<EmployeePosition> getAllEmployeePositions() throws CFSException {
		List<EmployeePosition> employeePositions = new ArrayList<EmployeePosition>();
		
		try {
			Iterable<EmployeePosition> employeePositionItr = employeePositionRepository.findAll();
			employeePositionItr.forEach(employeePosition -> {
				if(employeePosition != null && employeePosition.getOrganisation() != null 
						&& employeePosition.getOrganisation().getName() != null && !employeePosition.getOrganisation().getName().equals(CFSEnum.ALL.toString())) {
					employeePositions.add(employeePosition);
				}
			});
		} catch (CFSException e) {
			e.printStackTrace();
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, "Exception : " + e);
		}

		return employeePositions;
	}

	public List<Year> getAllYears(boolean onlyActive, Integer page, Integer size) throws CFSException {
		List<Year> yearList = new ArrayList<Year>();

		try {
			Pageable pageable = PageRequest.of(page, size, Sort.by("year").descending());
			yearRepository.findAll(pageable).forEach(year -> {
				if(!onlyActive) {
					yearList.add(mapper.map(year, Year.class));
				} else {
					if(year.getStatus().equals(CFSEnum.ACTIVE.toString())) {
						yearList.add(mapper.map(year, Year.class));
					}
				}
			});
		} catch (CFSException e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, "Exception : " + e);
		}

		return yearList;
	}

	public List<String> getAppraisalYear(String userId) throws CFSException {
		List<String> years = new ArrayList<String>();

		try {
			userAppraisalRepository.findByUserId(Long.valueOf(userId)).forEach(userAppraisal -> {
				years.add(userAppraisal.getYear());
			});
		} catch (CFSException e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, "Exception : " + e);
		}

		return years;
	}

	public List<String> getPerformanceGoalYear(Long organisationId) throws CFSException {
		List<String> performanceGoals = null;

		try {
			performanceGoals = performanceGoalsRepository.findYears(organisationId);
		} catch (CFSException e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, "Exception : " + e);
		}

		return performanceGoals;
	}

	public PerformanceGoalDtos getPerformanceGoals(Long organisationId, String year) throws CFSException {
		PerformanceGoalDtos performanceGoalDtos = new PerformanceGoalDtos();

		try {
			performanceGoalsRepository.findByOrganisationIdAndYear(organisationId, year).forEach(pg -> {
				PerformanceGoalDto performanceGoalDto = new PerformanceGoalDto();
				performanceGoalDto.setId(pg.getId());
				performanceGoalDto.setResult(pg.getResult());
				performanceGoalDto.setGoalId(pg.getGoalId());
				//performanceGoalDto.setGoalName(pg.getGoal().getName());
				performanceGoalDtos.getPerformanceGoalDto().add(performanceGoalDto);
			});

			goalRepository.findAll().forEach(goal->{
				performanceGoalDtos.getGoals().add(goal);
			});

			if(performanceGoalDtos.getPerformanceGoalDto().size() == 0) {
				PerformanceGoalDto performanceGoalDto = new PerformanceGoalDto();
				performanceGoalDtos.setOrganisationId(organisationId);
				performanceGoalDtos.setYear(year);
				performanceGoalDtos.getPerformanceGoalDto().add(performanceGoalDto);
				performanceGoalDtos.getPerformanceGoalDto().add(performanceGoalDto);
				performanceGoalDtos.getPerformanceGoalDto().add(performanceGoalDto);
			}
		} catch (CFSException e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, "Exception : " + e);
		}

		return performanceGoalDtos;
	}

	public PdpDto getPdp(Long userId, String year) throws CFSException {
		PdpDto pdpDto = new PdpDto();
		Optional<PersonalDevelopmentPlan> personalDevelopmentPlan = null;
		
		try {
			personalDevelopmentPlan = pdpRepository.findByUserIdAndYear(userId, year);
			if(personalDevelopmentPlan.isPresent()) {
				pdpDto.setYear(personalDevelopmentPlan.get().getYear());
				pdpDto.setUserId(personalDevelopmentPlan.get().getUserId());
				pdpDto.setTrainingProgressCurrent(personalDevelopmentPlan.get().getTrainingProgressCurrent());
				pdpDto.setTrainingProgressFuture(personalDevelopmentPlan.get().getTrainingProgressFuture());
				pdpDto.setCareerPreferences(personalDevelopmentPlan.get().getCareerPreferences());
				AtomicInteger index = new AtomicInteger(0);
				personalDevelopmentPlan.get().getSkill().forEach(skill-> {
					if(index.get() == 0) {
						pdpDto.setSkillsToBeDeveloped0(skill.getSkillsToBeDeveloped());
						pdpDto.setHowSkillsToBeDeveloped0(skill.getHowSkillsToBeDeveloped());
						pdpDto.setAssistanceRequired0(skill.getAssistanceRequired());
						pdpDto.setWhenn0(skill.getWhenn());
					} else if(index.get() == 1) {
						pdpDto.setSkillsToBeDeveloped1(skill.getSkillsToBeDeveloped());
						pdpDto.setHowSkillsToBeDeveloped1(skill.getHowSkillsToBeDeveloped());
						pdpDto.setAssistanceRequired1(skill.getAssistanceRequired());
						pdpDto.setWhenn1(skill.getWhenn());
					} else if(index.get() == 2) {
						pdpDto.setSkillsToBeDeveloped2(skill.getSkillsToBeDeveloped());
						pdpDto.setHowSkillsToBeDeveloped2(skill.getHowSkillsToBeDeveloped());
						pdpDto.setAssistanceRequired2(skill.getAssistanceRequired());
						pdpDto.setWhenn2(skill.getWhenn());
					}

					index.incrementAndGet();
				});
			}
		} catch (CFSException e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, "Exception : " + e);
		}

		return pdpDto;
	}

	public List<Value> getValue() throws CFSException {
		List<Value> valueDtoList = new ArrayList<Value>();

		try {
			valueRepository.findAll().forEach(value -> {
				valueDtoList.add(mapper.map(value, Value.class));
			});
		} catch (CFSException e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, "Exception : " + e);
		}

		return valueDtoList;
	}

	public AppraisalDtos getUserAppraisals(Long userId, String appraisalYear, Long valueId) throws CFSException {
		Map<Long, Task> taskMap = new HashMap<>();
		AppraisalDtos appraisalDtos = new AppraisalDtos();

		try {
			//CFS-TODO - Work on OneToOne
			taskRepository.findAll().forEach(asc -> {
				taskMap.put(asc.getId(), asc);
			});

			List<Appraisal> appraisals = appraisalRepository.findByUserAndAppraisalYearAndValue(Long.valueOf(userId), valueId, appraisalYear);
			if(appraisals != null && appraisals.size() == 0) {
				taskRepository.findByValueId(valueId).forEach(task -> {
					AppraisalDto appraisalDto = new AppraisalDto();
					appraisalDto.setValueId(null);
					appraisalDto.setTaskId(task.getId());
					//appraisalDto.setHeader(task.getHeader());
					appraisalDto.setName(task.getName());
					appraisalDtos.setAppraisalUserId(userId);
					appraisalDtos.setValueId(valueId);
					appraisalDtos.setYear(appraisalYear);
					appraisalDtos.setUserId(Long.valueOf(userId));
					appraisalDtos.getAppraisalDto().add(appraisalDto);
					appraisalDtos.setStatus(CFSAppraisalStatus.STARTED.toString());
					appraisalDtos.setScreenLock(CFSEnum.UNLOCKED.toString());
				});

				createOrUpdateAppraisal(appraisalDtos);
			}

			appraisalDtos.getAppraisalDto().clear();
			appraisals = appraisalRepository.findByUserAndAppraisalYearAndValue(Long.valueOf(userId), valueId, appraisalYear);
			appraisals.forEach(appraisal -> {
				appraisalDtos.setAppraisalId(appraisal.getId());
				appraisalDtos.setYear(appraisal.getYear());
				appraisalDtos.setValueId(valueId);
				appraisalDtos.setUserId(appraisal.getUserId());
				appraisalDtos.setAppraisalUserId(appraisal.getUserAppraisalId());
				appraisalDtos.setScreenLock(CFSEnum.UNLOCKED.toString());
				//CFS-TODO - Work on OneToOne
				Optional<UserAppraisal> userAppraisal = userAppraisalRepository.findById(appraisal.getUserAppraisalId());
				if(userAppraisal.isPresent()) {
					appraisalDtos.setStatus(userAppraisal.get().getStatus());
				}

				AppraisalDto appraisalDto = new AppraisalDto();
				appraisalDto.setValueId(appraisal.getId());
				appraisalDto.setTaskId(appraisal.getTaskId());
				appraisalDto.setExceedsEmployee(appraisal.getExceedsEmployee());
				appraisalDto.setExceedsManager(appraisal.getExceedsManager());
				appraisalDto.setMeetsEmployee(appraisal.getMeetsEmployee());
				appraisalDto.setMeetsManager(appraisal.getMeetsManager());
				appraisalDto.setDoesNotMeetEmployee(appraisal.getDoesnotmeetEmployee());
				appraisalDto.setDoesNotMeetManager(appraisal.getDoesnotmeetManager());
				appraisalDto.setStatus(appraisal.getStatus());
				appraisalDto.setName(taskMap.get(appraisal.getTaskId()).getName());

				AppraisalMessageDtos appraisalMessageDtos = showAppraisalMessage(appraisal.getId());
				if(appraisalMessageDtos != null && appraisalMessageDtos.getAppraisalMessageDto().size() > 0) {
					StringBuffer chatMessage = new StringBuffer();
					AppraisalMessageDto appraisalMessageDto = appraisalMessageDtos.getAppraisalMessageDto().get(appraisalMessageDtos.getAppraisalMessageDto().size() - 1);
					chatMessage.append("Last Comment: " + appraisalMessageDto.getMessage() + "\n");
					chatMessage.append("By : " + appraisalMessageDto.getName() + "\n");
					chatMessage.append("Date : " + appraisalMessageDto.getCreationDate() + "\n");
					appraisalDto.setLastComment(chatMessage.toString());
				}
				
				appraisalDtos.getAppraisalDto().add(appraisalDto);
			});

			Optional<UserAppraisal> userAppraisal = userAppraisalRepository.findByUserIdAndYear(userId, appraisalYear);
			if(userAppraisal.isPresent() && userAppraisal.get().getStatus().equals(CFSAppraisalStatus.RATING_COMPLETE.toString())) {
				appraisalDtos.setScreenLock(CFSEnum.LOCKED.toString());
			}

			if(userAppraisal.isPresent() && userAppraisal.get().getReviewDate() != null) {
				//CFS-TODO
				Date reviewDate = userAppraisal.get().getReviewDate();
				java.util.Date today = new SimpleDateFormat("yyyy-MM-dd").parse(LocalDateTime.now().toString());
				//int diffInDays = (int)( (reviewDate.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));
				int diffInDays = (int)( (reviewDate.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));
				if(diffInDays >= 12 || userAppraisal.get().getStatus().equals(CFSAppraisalStatus.RATING_COMPLETE.toString()) ) {
					appraisalDtos.setScreenLock(CFSEnum.LOCKED.toString());
				}
			}
		} catch (CFSException | ParseException e) {
			e.printStackTrace();
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, "Exception : " + e);
		}

		return appraisalDtos;
	}

	public String createOrUpdateUser(UserDto userDto) throws CFSException {
		try {
			Optional<User> user = userRepository.findByPrimaryEmail(userDto.getPrimaryEmail());
	        if(user.isPresent()) {
	        	if(userDto.getSaveType() != null && userDto.getSaveType().equals("create")) {
	    			throw new CFSException(ErrorCodes.ERROR_001, "User with email ' " + userDto.getPrimaryEmail() + " ' is alreaady present in the system.");
	        	}

	        	if(userDto.getEmployeeType() != null ) {
		            Role role = getRole(userDto);
		        	user.get().setRole(role);
	        	}

	        	user.get().setFirstName(userDto.getFirstName());
	        	user.get().setLastName(userDto.getLastName());
	        	user.get().setEmployeeNumber(userDto.getEmployeeNumber());
	        	user.get().setEmployeePositionId(userDto.getEmployeePositionId());
	        	user.get().setReviewDate(userDto.getReviewDate());
	        	user.get().setPreviousPosition(userDto.getPreviousPosition());
	        	user.get().setDepartment(userDto.getDepartment());
	        	user.get().setDateJoined(userDto.getDateJoined());
	        	user.get().setReviewerId(userDto.getReviewerId());
	        	user.get().setDob(userDto.getDob());
	        	user.get().setMobileNumber(userDto.getMobileNumber());
	        	user.get().setStatus(userDto.getStatus());
	        	userRepository.save(user.get());
	        	return "update";
	        } else {
	            Role role = getRole(userDto);

	            //userDto.setPassword(Util.encodeString(userDto.getPassword()));
	            userDto.setPassword(Util.generatePassayPassword());
	        	User userObj = mapper.map(userDto, User.class);
	        	userObj.setId(null);
	        	userObj.setRole(role);
	        	userObj.setEmployeePositionId(userDto.getEmployeePositionId());
	        	userObj.setFirstPasswordSet("FALSE");
	        	userObj.setStatus(CFSEnum.ACTIVE.toString());
	        	userObj.setEmployeeNumber(userDto.getEmployeeNumber() == "" ? null : userDto.getEmployeeNumber());
	        	User userCreated = userRepository.save(userObj);

	        	userDto.getOrganisationId().forEach(organisationId -> {
		        	UserOrganisation userOrganisation = new UserOrganisation();
		        	userOrganisation.setUserId(userCreated.getId());
		        	userOrganisation.setOrganisationId(Long.valueOf(organisationId));
		        	userOrganisationRepository.save(userOrganisation);
	        	});

	        	return userCreated.getPassword();
	        }
		} catch (CFSException e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, "Exception : " + e);
		}
	}

	private Role getRole(UserDto userDto) {
		Role role = new Role();

		if(userDto.getEmployeeType().equals("ADMIN")) {
		    role = roleRepository.findByName("ADMIN");
		} else if(userDto.getEmployeeType().equals("EMPLOYEE")) {
			role = roleRepository.findByName("EMPLOYEE");
		} else if(userDto.getEmployeeType().equals("MANAGER")) {
		    role = roleRepository.findByName("MANAGER");
		}

		return role;
	}

	public List<StartReviewDto> sendStartReviewNotification() throws CFSException {
		List<StartReviewDto> startReviewDtos = new ArrayList<StartReviewDto>();
		
		try {
			int currentYear = Calendar.getInstance().get(Calendar.YEAR);
			List<UserAppraisal> userAppraisals= userAppraisalRepository.findAllReadyForReviewUsers(CFSEnum.CURRENT.toString(), CFSAppraisalStatus.EMPLOYEE_RATING_START.toString());
			userAppraisals.forEach(userAppraisal -> {
				String userReviewYear = Util.getYear(userAppraisal.getReviewDate().toString());
				if(String.valueOf(currentYear).equals(userReviewYear)) {
					Optional<User> user = userRepository.findById(userAppraisal.getUserId());
					Optional<User> reviewer = userRepository.findById(user.get().getReviewerId());
					StartReviewDto startReviewDto = new StartReviewDto();
					startReviewDto.setEmailId(user.get().getPrimaryEmail());
					startReviewDto.setReviewerName(reviewer.get().getFirstName() + " " + reviewer.get().getLastName());
					startReviewDto.setStartDate(userAppraisal.getReviewDate().toString());
					startReviewDto.setYear(userReviewYear);
					startReviewDtos.add(startReviewDto);
				}
			});
		} catch (CFSException e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, "Exception : " + e);
		}

		return startReviewDtos;
	}

	public String updateReviewDate(ReviewDto reviewDto) {
		UserAppraisal userAppraisal = null;
		Optional<UserAppraisal> userAppraisalGet = userAppraisalRepository.findByUserIdAndYear(reviewDto.getUserId(), CFSEnum.CURRENT.toString());
		
		if(!userAppraisalGet.isPresent()) {
			userAppraisal = createOrUpdateUserAppraisal(new UserAppraisalDto(reviewDto.getUserId(), CFSEnum.CURRENT.toString(), null));
		} else {
			userAppraisal = userAppraisalGet.get();
		}

		String newDate = Util.getDate("yyyy-MM-dd", "dd-MM-yyyy", reviewDto.getReviewDate());
		userAppraisal.setReviewDate(Date.valueOf(reviewDto.getReviewDate()));
		userAppraisalRepository.save(userAppraisal);
		return "Review date updated.";
	}

	public Boolean createOrUpdateAppraisal(AppraisalDtos appraisalDtos) throws CFSException {
		try {
			Optional<User> user = userRepository.findById(appraisalDtos.getUserId());
			List<Appraisal> appraisalResult = appraisalRepository.findByUserAndAppraisalYearAndValue(appraisalDtos.getAppraisalUserId(), appraisalDtos.getValueId(), appraisalDtos.getYear());
			if(appraisalResult.size() == 0) {
				UserAppraisal userAppraisal = createOrUpdateUserAppraisal(new UserAppraisalDto(appraisalDtos.getUserId(), appraisalDtos.getYear(), CFSAppraisalStatus.EMPLOYEE_RATING_START.toString()));
				
				appraisalDtos.getAppraisalDto().forEach(appraisalDto -> {
					Appraisal appraisal = mapper.map(appraisalDto, Appraisal.class);
					appraisal.setUserId(appraisalDtos.getUserId());
					appraisal.setValueId(appraisalDtos.getValueId());

					if(userAppraisal != null) {
						appraisal.setUserAppraisalId(userAppraisal.getId());
					}

					appraisal.setYear(appraisalDtos.getYear());
					appraisal.setUserId(appraisalDtos.getUserId());
					appraisal.setValue(null);
					appraisal.setTask(null);
					appraisal.setExceedsEmployee(appraisalDto.getExceedsEmployee());

    				if(user.get().getRole().getName().equals("EMPLOYEE")) {
    					appraisal.setMeetsEmployee(appraisalDto.getMeetsEmployee());
        			   	appraisal.setDoesnotmeetEmployee(appraisalDto.getDoesNotMeetEmployee());
    				} else {
    					appraisal.setExceedsManager(appraisalDto.getExceedsManager());
    					appraisal.setMeetsManager(appraisalDto.getMeetsManager());
        			   	appraisal.setDoesnotmeetManager(appraisalDto.getDoesNotMeetManager());
    				}

    				appraisalRepository.save(appraisal);
				});
	       } else {
	    	   appraisalDtos.getAppraisalDto().forEach(appraisalDto -> {
	    		   Optional<Appraisal> appraisal = appraisalRepository.findByUserAndAppraisalYearAndValueAndTask(appraisalDtos.getAppraisalUserId(), appraisalDtos.getValueId(), appraisalDto.getTaskId(), appraisalDtos.getYear());
	    		   if(appraisal.isPresent()) {
	    			   if(user.get().getRole().getName().equals("EMPLOYEE")) {
	    				   appraisal.get().setExceedsEmployee(appraisalDto.getExceedsEmployee());
		    			   appraisal.get().setMeetsEmployee(appraisalDto.getMeetsEmployee());
		    			   appraisal.get().setDoesnotmeetEmployee(appraisalDto.getDoesNotMeetEmployee());
	    			   } else {
	    				   appraisal.get().setExceedsManager(appraisalDto.getExceedsManager());
	    				   appraisal.get().setMeetsManager(appraisalDto.getMeetsManager());
	    				   appraisal.get().setDoesnotmeetManager(appraisalDto.getDoesNotMeetManager());
	    			   }
	    			   
	    	    	   appraisalRepository.save(appraisal.get());
	    		   }
	    	   });

	    	   Optional<User> appraisalUser = userRepository.findById(appraisalDtos.getAppraisalUserId());
	    	   List<String> valueIds = valueRepository.getValueIds(appraisalUser.get().getOrganzations().get(0).getId(), appraisalUser.get().getEmployeePositionId());
	    	   List<String> userValueIds = appraisalRepository.getValueIds(appraisalUser.get().getId(), appraisalDtos.getYear());

	    	   Collections.sort(valueIds);
	    	   Collections.sort(userValueIds);

	    	   AtomicBoolean managerAppraisalCompleted = new AtomicBoolean(false);
    		   List<Appraisal> appraisals = appraisalRepository.findByUserIdAndYear(appraisalDtos.getAppraisalUserId(), appraisalDtos.getYear());
    		   appraisals.forEach(appraisal -> {
    			   if(appraisal.getDoesnotmeetManager() != null && appraisal.getMeetsManager() != null && appraisal.getMeetsManager() != null) {
    				   managerAppraisalCompleted.set(true);
    			   } else {
       				   managerAppraisalCompleted.set(false);
    			   }
    		   });
	    	   
	    	   Optional<UserAppraisal> userAppraisal = userAppraisalRepository.findByUserIdAndYear(appraisalUser.get().getId(), appraisalDtos.getYear());
	    	   if(valueIds.equals(userValueIds) && managerAppraisalCompleted.get()) {
	    		   userAppraisal.get().setStatus(CFSAppraisalStatus.MANAGER_RATING_COMPLETE.toString());
	    	   }

	    	   userAppraisalRepository.save(userAppraisal.get());
	       }
		} catch (CFSException e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, "Exception : " + e);
		}

        return true;
	}
	
	/*public Boolean createOrUpdateAppraisal(AppraisalDtos appraisalDtos) throws CFSException {
		try {
			Optional<User> user = userRepository.findById(appraisalDtos.getUserId());
			List<Appraisal> appraisalResult = appraisalRepository.findByUserAndAppraisalYearAndValue(appraisalDtos.getAppraisalUserId(), appraisalDtos.getValueId(), appraisalDtos.getYear());
			Optional<UserAppraisal> userAppraisal = userAppraisalRepository.findByUserIdAndYear(appraisalDtos.getUserId(), appraisalDtos.getYear().toString());
			if(appraisalResult.size() == 0) {
				appraisalDtos.getAppraisalDto().forEach(appraisalDto -> {
					Appraisal appraisal = mapper.map(appraisalDto, Appraisal.class);
					appraisal.setUserId(appraisalDtos.getUserId());
					appraisal.setValueId(appraisalDtos.getValueId());

					if(userAppraisal.isPresent()) {
						appraisal.setUserAppraisalId(userAppraisal.get().getId());
					}

					appraisal.setYear(appraisalDtos.getYear());
					appraisal.setUserId(appraisalDtos.getUserId());
					appraisal.setValue
					(null);
					appraisal.setTask(null);
					appraisal.setExceedsEmployee(appraisalDto.getExceedsEmployee());

    				if(user.get().getRole().getName().equals("EMPLOYEE")) {
    					appraisal.setMeetsEmployee(appraisalDto.getMeetsEmployee());
        			   	appraisal.setDoesnotmeetEmployee(appraisalDto.getDoesNotMeetEmployee());
        			   	//appraisal.setSpecificExamples(appraisalDto.getSpecificExamples());
    				} else {
    					appraisal.setExceedsManager(appraisalDto.getExceedsManager());
    					appraisal.setMeetsManager(appraisalDto.getMeetsManager());
        			   	appraisal.setDoesnotmeetManager(appraisalDto.getDoesNotMeetManager());
    					
    				}

    				appraisalRepository.save(appraisal);

    				List<String> valueIds = valueRepository.getValueIds();
    				List<String> userValueIds = appraisalRepository.getValueIds(appraisalDtos.getUserId(), appraisalDtos.getYear());

    				Collections.sort(valueIds);
    				Collections.sort(userValueIds);

    				if(valueIds.equals(userValueIds)) {
        				userAppraisal.get().setStatus(CFSAppraisalStatus.EMPLOYEE_RATING_END.toString());
    				} else {
        				userAppraisal.get().setStatus(CFSAppraisalStatus.EMPLOYEE_RATING_START.toString());
    				}

    				userAppraisalRepository.save(userAppraisal.get());
				});
	       } else {
	    	   appraisalDtos.getAppraisalDto().forEach(appraisalDto -> {
	    		   Optional<Appraisal> appraisal = appraisalRepository.findByUserAndAppraisalYearAndValueAndTask(appraisalDtos.getAppraisalUserId(), appraisalDtos.getValueId(), appraisalDto.getTaskId(), appraisalDtos.getYear());
	    		   if(appraisal.isPresent()) {
	    			   if(user.get().getRole().getName().equals("EMPLOYEE")) {
	    				   appraisal.get().setExceedsEmployee(appraisalDto.getExceedsEmployee());
		    			   appraisal.get().setMeetsEmployee(appraisalDto.getMeetsEmployee());
		    			   appraisal.get().setDoesnotmeetEmployee(appraisalDto.getDoesNotMeetEmployee());
	    			   } else {
	    				   appraisal.get().setExceedsManager(appraisalDto.getExceedsManager());
	    				   appraisal.get().setMeetsManager(appraisalDto.getMeetsManager());
	    				   appraisal.get().setDoesnotmeetManager(appraisalDto.getDoesNotMeetManager());
	    			   }
	    			   
	    			   //appraisal.get().setSpecificExamples(appraisalDto.getSpecificExamples());
	    	    	   appraisalRepository.save(appraisal.get());
	    		   }
	    	   });
	       }
		} catch (CFSException e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, "Exception : " + e);
		}

        return true;
	}*/

	public UserAppraisal createOrUpdateUserAppraisal(UserAppraisalDto userAppraisalDto) throws CFSException {
		UserAppraisal userAppraisalResult = new UserAppraisal();

		try {
			Optional<UserAppraisal> userAppraisal = userAppraisalRepository.findByUserIdAndYear(userAppraisalDto.getUserId(), userAppraisalDto.getYear().toString());
			if(!userAppraisal.isPresent()) {
				userAppraisalResult.setUserId(userAppraisalDto.getUserId());
				userAppraisalResult.setStatus(CFSAppraisalStatus.EMPLOYEE_RATING_START.toString());
				userAppraisalResult.setYear(CFSEnum.CURRENT.toString());
				userAppraisalResult = userAppraisalRepository.save(userAppraisalResult);
			} else {
				return userAppraisal.get();
			}
		} catch (CFSException e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, "Exception : " + e);
		}

        return userAppraisalResult;
	}

	public UserAppraisal submitAppraisal(UserAppraisalDto userAppraisalDto) throws CFSException {
		UserAppraisal userAppraisalResult = new UserAppraisal();

		try {
			Optional<UserAppraisal> userAppraisal = userAppraisalRepository.findByUserIdAndYear(userAppraisalDto.getUserId(), userAppraisalDto.getYear().toString());
			if(userAppraisal.isPresent()) {
				String year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
				List<UserAppraisal> currentUserAppraisals = userAppraisalRepository.findByUserId(userAppraisalDto.getUserId());
				List<String> years = new ArrayList<String>();
				currentUserAppraisals.forEach(currentUserAppraisal -> {
					years.add(currentUserAppraisal.getYear());
				});

				if(years != null && years.contains(year)) {
					log.info("This current appraisal cannot be saved as there is an appraisal already present in the system for current fiscal year.");
					throw new CFSException(ErrorCodes.ERROR_001, "This current appraisal cannot be saved as there is an appraisal already present in the system for current fiscal year.");
				} else {
					userAppraisal.get().setYear(year);
					userAppraisal.get().setStatus(userAppraisalDto.getStatus());
					userAppraisalResult = userAppraisalRepository.save(userAppraisal.get());

					List<Appraisal> appraisals = appraisalRepository.findByUserIdAndYear(userAppraisalDto.getUserId(), userAppraisalDto.getYear().toString());
					appraisals.forEach(appraisal -> {
						appraisal.setYear(year);
						appraisalRepository.save(appraisal);
					});
				}
			}
		} catch (CFSException e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, "Exception : " + e);
		}

        return userAppraisalResult;
	}

	public Boolean createAppraisalMessage(Long userId, Long appraisalId, String message) throws CFSException {
		try {
			AppraisalMessage appraisalMessage = new AppraisalMessage();
			appraisalMessage.setAppraisalId(appraisalId);
			appraisalMessage.setUserId(userId);
			appraisalMessage.setMessage(message);
			appraisalMessageRepository.save(appraisalMessage);
		} catch (CFSException e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, "Exception : " + e);
		}

        return true;
	}

	public AppraisalMessageDtos showAppraisalMessage(Long appraisalId) throws CFSException {
		AppraisalMessageDtos appraisalMessageDtos = new AppraisalMessageDtos();

		try {
			List<AppraisalMessage> appraisalMessage = appraisalMessageRepository.findByAppraisalId(appraisalId);
			appraisalMessage.forEach(message -> {
				User user = userRepository.findById(message.getUserId()).get();
				String username = user.getFirstName() + " " + user.getLastName();
				
				AppraisalMessageDto appraisalMessageDto = new AppraisalMessageDto();
				appraisalMessageDto.setMessage(message.getMessage());
				appraisalMessageDto.setName(username);
				appraisalMessageDto.setMessage(message.getMessage());
				appraisalMessageDto.setCreationDate(message.getCreationDate().toString());
				
				appraisalMessageDtos.getAppraisalMessageDto().add(appraisalMessageDto);
			});
		} catch (CFSException e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, "Exception : " + e);
		}

        return appraisalMessageDtos;
	}

	public Boolean createOrUpdatePdp(PdpDto pdpDto) throws CFSException {
		PersonalDevelopmentPlan pdp = null;

		try {
			Optional<PersonalDevelopmentPlan> pdpResult = pdpRepository.findByUserIdAndYear(pdpDto.getUserId(), pdpDto.getYear());
			if(pdpResult.isPresent()) {
				pdpResult.get().setTrainingProgressCurrent(pdpDto.getTrainingProgressCurrent());
				pdpResult.get().setTrainingProgressFuture(pdpDto.getTrainingProgressFuture());
				pdpResult.get().setCareerPreferences(pdpDto.getCareerPreferences());
				pdpResult.get().setUserId(pdpDto.getUserId());
				pdpResult.get().setYear(pdpDto.getYear());
				pdp = pdpRepository.save(pdpResult.get());
			} else {
				PersonalDevelopmentPlan newPdp = new PersonalDevelopmentPlan();
				newPdp.setTrainingProgressCurrent(pdpDto.getTrainingProgressCurrent());
				newPdp.setTrainingProgressFuture(pdpDto.getTrainingProgressFuture());
				newPdp.setCareerPreferences(pdpDto.getCareerPreferences());
				newPdp.setUserId(pdpDto.getUserId());
				newPdp.setYear(pdpDto.getYear());
				pdp = pdpRepository.save(newPdp);
			}

			skillRepository.deleteByPdpId(pdpResult.get().getId());

			if(!StringUtils.isEmpty(pdpDto.getSkillsToBeDeveloped0()) || !StringUtils.isEmpty(pdpDto.getHowSkillsToBeDeveloped0()) || !StringUtils.isEmpty(pdpDto.getAssistanceRequired0()) || !StringUtils.isEmpty(pdpDto.getWhenn0())) {
        		Skill skill = new Skill();
        		skill.setSkillsToBeDeveloped(pdpDto.getSkillsToBeDeveloped0());
        		skill.setHowSkillsToBeDeveloped(pdpDto.getHowSkillsToBeDeveloped0());
        		skill.setAssistanceRequired(pdpDto.getAssistanceRequired0());
        		skill.setWhenn(pdpDto.getWhenn0());
        		skill.setSkillOrder(new Long(1));
        		skill.setPdpId(pdp.getId());
	        	skillRepository.save(skill);
        	}

        	if(!StringUtils.isEmpty(pdpDto.getSkillsToBeDeveloped1()) || !StringUtils.isEmpty(pdpDto.getHowSkillsToBeDeveloped1()) || !StringUtils.isEmpty(pdpDto.getAssistanceRequired1()) || !StringUtils.isEmpty(pdpDto.getWhenn1())) {
        		Skill skill = new Skill();
        		skill.setSkillsToBeDeveloped(pdpDto.getSkillsToBeDeveloped1());
        		skill.setHowSkillsToBeDeveloped(pdpDto.getHowSkillsToBeDeveloped1());
        		skill.setAssistanceRequired(pdpDto.getAssistanceRequired1());
        		skill.setWhenn(pdpDto.getWhenn1());
        		skill.setSkillOrder(new Long(2));
        		skill.setPdpId(pdp.getId());
	        	skillRepository.save(skill);
        	}

        	if(!StringUtils.isEmpty(pdpDto.getSkillsToBeDeveloped2()) || !StringUtils.isEmpty(pdpDto.getHowSkillsToBeDeveloped2()) || !StringUtils.isEmpty(pdpDto.getAssistanceRequired2()) || !StringUtils.isEmpty(pdpDto.getWhenn2())) {
        		Skill skill = new Skill();
        		skill.setSkillsToBeDeveloped(pdpDto.getSkillsToBeDeveloped2());
        		skill.setHowSkillsToBeDeveloped(pdpDto.getHowSkillsToBeDeveloped2());
        		skill.setAssistanceRequired(pdpDto.getAssistanceRequired2());
        		skill.setWhenn(pdpDto.getWhenn2());
        		skill.setSkillOrder(new Long(3));
        		skill.setPdpId(pdp.getId());
	        	skillRepository.save(skill);
        	}
		} catch (CFSException e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, "Exception : " + e);
		}

        return true;
	}

	public void resetPassword(ResetPasswordDto resetPasswordDto) throws CFSException {
		try {

			Optional<User> user = userRepository.findByPrimaryEmail(resetPasswordDto.getPrimaryEmail());
			Long passwordRetriesLeft = user.get().getPasswordRetriesLeft() == null ? 5 : user.get().getPasswordRetriesLeft();
			if (passwordRetriesLeft == 1) {
				throw new CFSException(ErrorCodes.ERROR_001,
						"Incorrect password. You have exhausted maximum password retry. Please contact administrator.");
			}

			if(user.isPresent()) {
	        	user.get().setPassword(Util.encodeString(resetPasswordDto.getPassword()));
	        	user.get().setFirstPasswordSet("TRUE");
	        	userRepository.save(user.get());
	        }
		} catch (CFSException e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, e);
		}
	}

	public Boolean createOrUpdateOrganisation(OrganisationDto organisationDto) throws CFSException {
		Optional<Organisation> organisationResult = null;

		try {
			if(organisationDto.getId() != null) {
				organisationResult = organisationRepository.findById(organisationDto.getId());
				if(organisationResult.isPresent()) {
					organisationResult.get().setName(organisationDto.getName());
					organisationRepository.save(organisationResult.get());
				} else {
					Organisation organisation = new Organisation();
					organisation.setName(organisationDto.getName());
					organisationRepository.save(organisation);
				}			 
			} else {
				organisationResult = organisationRepository.findByName(organisationDto.getName());
				if(organisationResult.isPresent()) {
					throw new CFSException(ErrorCodes.ERROR_001, "Organisation already exists.");
				} else {
					Organisation organisation = new Organisation();
					organisation.setName(organisationDto.getName());
					organisationRepository.save(organisation);
				}
			}
		} catch (Exception e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, e);
		}

        return true;
	}

	public Boolean deleteOrganisation(Long organisationId) throws CFSException {
		try {
			organisationRepository.deleteById(organisationId);
		} catch (CFSException e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, "Exception : " + e);
		}

        return true;
	}

	public Boolean deleteUserOrganisation(Long userId) throws CFSException {
		try {
			userOrganisationRepository.deleteByUserId(userId);
		} catch (CFSException e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, "Exception : " + e);
		}

        return true;
	}

	public Boolean deleteUserAppraisal(Long userId) throws CFSException {
		try {
			userAppraisalRepository.deleteByUserId(userId);
		} catch (CFSException e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, "Exception : " + e);
		}

        return true;
	}

	public Boolean deleteAppraisal(Long userId) throws CFSException {
		try {
			appraisalRepository.deleteByUserId(userId);
		} catch (CFSException e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, "Exception : " + e);
		}

        return true;
	}

	public Boolean deleteUserRole(Long userId) throws CFSException {
		try {
			userRoleRepository.deleteById(userId);
		} catch (CFSException e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, "Exception : " + e);
		}

        return true;
	}

	public Boolean deleteUser(Long userId) throws CFSException {
		try {
			userRepository.deleteById(userId);
		} catch (CFSException e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, "Exception : " + e);
		}

        return true;
	}

	public Boolean deleteValue(Long valueId) throws CFSException {
		try {
			if(taskRepository.findByValueId(valueId).size() == 1) {
				valueRepository.deleteById(valueId);
			} else {
				throw new CFSException(ErrorCodes.ERROR_001, "Cannot delete a value which has task.");
			}
		} catch (CFSException e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, "Exception : " + e);
		}

        return true;
	}

	public Boolean deleteTask(Long taskId) throws CFSException {
		try {
			taskRepository.deleteById(taskId);
		} catch (CFSException e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, "Exception : " + e);
		}

        return true;
	}

	public Boolean createOrUpdateGoal(GoalDto goalDto) throws CFSException {
		Optional<Goal> goalResult = null;

		try {
			if(goalDto.getId() != null) {
				goalResult = goalRepository.findById(goalDto.getId());
				if(goalResult.isPresent()) {
					goalResult.get().setName(goalDto.getName());
					goalRepository.save(goalResult.get());
				} else {
					Goal goal = new Goal();
					goal.setName(goalDto.getName());
					goalRepository.save(goal);
				}			 
			} else {
				goalResult = goalRepository.findByName(goalDto.getName());
				if(goalResult.isPresent()) {
					throw new CFSException(ErrorCodes.ERROR_001, "Organisation already exists.");
				} else {
					Goal goal = new Goal();
					goal.setName(goalDto.getName());
					goalRepository.save(goal);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, e);
		}

        return true;
	}

	public Boolean createOrUpdateValue(ValueDto valueDto) throws CFSException {
		Optional<Value> valueResult = null;

		try {
			if(valueDto.getId() != null) {
				valueResult = valueRepository.findById(valueDto.getId());
				if(valueResult.isPresent()) {
					valueResult.get().setOrganisationId(valueDto.getOrganisationId());
					valueResult.get().setEmployeePositionId(valueDto.getEmployeePositionId());
					valueResult.get().setSortOrder(valueDto.getOrder());
					valueResult.get().setName(valueDto.getName());
					valueRepository.save(valueResult.get());
				} else {
					Value value = new Value();
					value.setOrganisationId(valueDto.getOrganisationId());
					value.setEmployeePositionId(valueDto.getEmployeePositionId());
					value.setSortOrder(valueDto.getOrder());
					value.setName(valueDto.getName());
					valueRepository.save(value);
				}			 
			} else {
				valueResult = valueRepository.findByName(valueDto.getName());
				if(valueResult.isPresent()) {
					throw new CFSException(ErrorCodes.ERROR_001, "Value already exists.");
				} else {
					Value value = new Value();
					value.setOrganisationId(valueDto.getOrganisationId());
					value.setEmployeePositionId(valueDto.getEmployeePositionId());
					value.setSortOrder(valueDto.getOrder());
					value.setName(valueDto.getName());
					valueRepository.save(value);
				}
			}
		} catch (Exception e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, e);
		}

        return true;
	}

	public Boolean createOrUpdateTask(TaskDto taskDto) throws CFSException {
		Optional<Task> taskResult = null;

		try {
			if(taskDto.getId() != null) {
				taskResult = taskRepository.findById(taskDto.getId());
				if(taskResult.isPresent()) {
					taskResult.get().setValueId(taskDto.getValueId());
					taskResult.get().setSortOrder(taskDto.getOrder());
					taskResult.get().setName(taskDto.getName());
					taskRepository.save(taskResult.get());
				} else {
					Task task = new Task();
					task.setValueId(taskDto.getValueId());
					task.setSortOrder(taskDto.getOrder());
					task.setName(taskDto.getName());
					taskRepository.save(task);
				}
			} else {
				taskResult = taskRepository.findByNameAndValueId(taskDto.getName(), taskDto.getValueId());
				if(taskResult.isPresent()) {
					throw new CFSException(ErrorCodes.ERROR_001, "Task already exists.");
				} else {
					Task task = new Task();
					task.setValueId(taskDto.getValueId());
					task.setSortOrder(taskDto.getOrder());
					task.setName(taskDto.getName());
					taskRepository.save(task);
				}
			}
		} catch (Exception e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, e);
		}

        return true;
	}

	public Boolean deleteGoal(Long goalId) throws CFSException {
		try {
			goalRepository.deleteById(goalId);
		} catch (CFSException e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, "Exception : " + e);
		}

        return true;
	}

	public Boolean createOrUpdatePerformanceGoals(PerformanceGoalDtos performanceGoalDtos) throws CFSException {

		try {
			performanceGoalDtos.getPerformanceGoalDto().forEach(performanceGoalDto->{
				if(performanceGoalDto.getId() != null) {
					Optional<PerformanceGoals> performanceGoalsResult = performanceGoalsRepository.findById(performanceGoalDto.getId());
					if(performanceGoalsResult.isPresent()) {
						performanceGoalsResult.get().setGoalId(performanceGoalDto.getGoalId());
						performanceGoalsResult.get().setResult(performanceGoalDto.getResult());
						performanceGoalsRepository.save(performanceGoalsResult.get());
					}
				} else {
					PerformanceGoals performanceGoals = new PerformanceGoals();
					performanceGoals.setOrganisationId(performanceGoalDtos.getOrganisationId());
					performanceGoals.setYear(performanceGoalDtos.getYear());
					performanceGoals.setGoalId(performanceGoalDto.getGoalId());
					performanceGoals.setResult(performanceGoalDto.getResult());
					performanceGoalsRepository.save(performanceGoals);
				}
			});
		} catch (Exception e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, e);
		}

        return true;
	}

	public List<Organisation> getAllOrganisations(Integer page, Integer size) throws CFSException {
		List<Organisation> organisationList = new ArrayList<Organisation>();

		try {
			Pageable pageable = PageRequest.of(page, size);
			organisationRepository.findAll(pageable).forEach(organisation -> {
				if(organisation != null && !organisation.getName().equals(CFSEnum.ALL.toString())) {
					organisationList.add(mapper.map(organisation, Organisation.class));
				}
			});
		} catch (CFSException e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, "Exception : " + e);
		}

		return organisationList;
	}

	public List<Goal> getAllGoals(Integer page, Integer size) throws CFSException {
		List<Goal> goalList = new ArrayList<Goal>();

		try {
			Pageable pageable = PageRequest.of(page, size);
			goalRepository.findAll(pageable).forEach(goal -> {
				goalList.add(mapper.map(goal, Goal.class));
			});
		} catch (CFSException e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, "Exception : " + e);
		}

		return goalList;
	}

	public List<Value> getAllValues(Integer page, Integer size) throws CFSException {
		List<Value> values = new ArrayList<Value>();

		try {
			Pageable pageable = PageRequest.of(page, size);
			valueRepository.findAll(pageable).forEach(value -> {
				values.add(value);
			});
		} catch (CFSException e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, "Exception : " + e);
		}

		return values;
	}

	public List<Value> getValuesByOrganisationAndEmployeePosition(String organisationId, String employeePositionId) throws CFSException {
		List<Value> values = new ArrayList<Value>();

		try {
			if(organisationId != null && employeePositionId != null && !employeePositionId.equals("NULL")) {
				valueRepository.findByOrganisationAndEmployeePosition(Long.valueOf(organisationId), Long.valueOf(employeePositionId)).forEach(value -> {
					values.add(value);
				});
			} else if(organisationId != null && employeePositionId.equals("NULL")) {
				valueRepository.findByOrganisation(Long.valueOf(organisationId)).forEach(value -> {
					values.add(value);
				});
			}
		} catch (CFSException e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, "Exception : " + e);
		}

		return values;
	}

	public List<Value> getValuesForAppraisal(String organisationId, String employeePositionId) throws CFSException {
		List<Value> values = new ArrayList<Value>();

		try {
			if(organisationId != null && employeePositionId != null && !employeePositionId.equals("NULL")) {
				valueRepository.findByOrganisationAndEmployeePosition(Long.valueOf(organisationId), Long.valueOf(employeePositionId)).forEach(value -> {
					if(value.getTasks().size() > 0) {
						values.add(value);
					}
				});
			} else if(organisationId != null && employeePositionId.equals("NULL")) {
				valueRepository.findByOrganisation(Long.valueOf(organisationId)).forEach(value -> {
					if(value.getTasks().size() > 0) {
						values.add(value);
					}
				});
			}
		} catch (CFSException e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, "Exception : " + e);
		}

		return values;
	}

	public String getLastComment(Long appraisalId) throws CFSException {
		StringBuffer lastComment = new StringBuffer();

		try {
			if(appraisalId != null) {
				List<AppraisalMessage> appraisalMessages = appraisalMessageRepository.findByAppraisalId(appraisalId);
				if(appraisalMessages != null && appraisalMessages.size() > 0) {
					AppraisalMessage appraisalMessage = appraisalMessages.get(appraisalMessages.size() - 1);
					User user = userRepository.findById(appraisalMessage.getUserId()).get();
					String username = user.getFirstName() + " " + user.getLastName();
					lastComment.append("Last Comment: " + appraisalMessage.getMessage() + "\n");
					lastComment.append("By : " + username + "\n");
					lastComment.append("Date : " + appraisalMessage.getCreationDate() + "\n");
				}
			}
		} catch (CFSException e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, "Exception : " + e);
		}

		return lastComment.toString();
	}

	public List<Task> getTasksByValue(Long valueId) throws CFSException {
		List<Task> tasks = new ArrayList<Task>();

		try {
			if(valueId != null) {
				taskRepository.findByValueId(valueId).forEach(task -> {
					tasks.add(task);
				});
			}
		} catch (CFSException e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, "Exception : " + e);
		}

		return tasks;
	}

	public List<Task> getAllTasks(Integer page, Integer size) throws CFSException {
		List<Task> tasks = new ArrayList<Task>();

		try {
			Pageable pageable = PageRequest.of(page, size);
			taskRepository.findAll(pageable).forEach(task -> {
				tasks.add(task);
			});
		} catch (CFSException e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, "Exception : " + e);
		}

		return tasks;
	}

	public List<Organisation> getAllOrganisationByName(String name, Integer page, Integer size) throws CFSException {
		List<Organisation> organisationList = new ArrayList<Organisation>();

		try {
			Pageable pageable = PageRequest.of(page, size);
			organisationRepository.findAllByName(name, pageable).forEach(organisation -> {
				organisationList.add(mapper.map(organisation, Organisation.class));
			});
		} catch (CFSException e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, "Exception : " + e);
		}

		return organisationList;
	}

	public Boolean createOrUpdateEmployeePosition(EmployeePositionDto employeePositionDto) throws CFSException {
		Optional<EmployeePosition> employeePositionResult = null;
		
		try {
			if(employeePositionDto.getId() != null) {
				employeePositionResult = employeePositionRepository.findById(employeePositionDto.getId());
				if(employeePositionResult.isPresent()) {
					employeePositionResult.get().setName(employeePositionDto.getName());
					employeePositionResult.get().setOrganisationId(employeePositionDto.getOrganisationId());
					employeePositionRepository.save(employeePositionResult.get());
				} else {
					EmployeePosition employeePosition = new EmployeePosition();
					employeePosition.setName(employeePositionDto.getName());
					employeePosition.setOrganisationId(employeePositionDto.getOrganisationId());
					employeePositionRepository.save(employeePosition);
				}			
			} else {
				employeePositionResult = employeePositionRepository.findByOrganisationIdAndName(employeePositionDto.getOrganisationId(), employeePositionDto.getName());
				
				if(employeePositionResult.isPresent()) {
					throw new CFSException(ErrorCodes.ERROR_001, "Employee Position already exists.");
				} else {
					EmployeePosition employeePosition = new EmployeePosition();
					employeePosition.setName(employeePositionDto.getName());
					employeePosition.setOrganisationId(employeePositionDto.getOrganisationId());
					employeePositionRepository.save(employeePosition);
				}
			}
		} catch (Exception e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, e);
		}

        return true;
	}

	public Boolean deleteEmployeePosition(Long id) throws CFSException {
		try {
			employeePositionRepository.deleteById(id);
		} catch (CFSException e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, "Exception : " + e);
		}

        return true;
	}

	public Boolean createOrUpdateYear(YearDto yearDto) throws CFSException {
		Optional<Year> yearResult = null;
		
		try {
			if(yearDto.getId() != null) {
				yearResult = yearRepository.findById(yearDto.getId());
				if(yearResult.isPresent()) {
					yearResult.get().setYear(yearDto.getYear());
					yearResult.get().setStatus(yearDto.getStatus());
					yearRepository.save(yearResult.get());
				} else {
					Year year = new Year();
					year.setYear(yearDto.getYear());
					year.setStatus(yearDto.getStatus());
					yearRepository.save(year);
				}			
			} else {
				yearResult = yearRepository.findByYear(yearDto.getYear());
				if(yearResult.isPresent()) {
					throw new CFSException(ErrorCodes.ERROR_001, "Year already exists.");
				} else {
					Year year = new Year();
					year.setYear(yearDto.getYear());
					year.setStatus(yearDto.getStatus());
					yearRepository.save(year);
				}
			}
		} catch (Exception e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, e);
		}

        return true;
	}

	public List<EmployeePosition> getAllEmployeePositions(Integer page, Integer size) throws CFSException {
		List<EmployeePosition> employeePositionList = new ArrayList<EmployeePosition>();

		try {
			Pageable pageable = PageRequest.of(page, size);
			employeePositionRepository.findAll(pageable).forEach(employeePosition -> {
				if(employeePosition != null && employeePosition.getOrganisation() != null 
						&& employeePosition.getOrganisation().getName() != null && !employeePosition.getOrganisation().getName().equals(CFSEnum.ALL.toString())) {
					employeePositionList.add(mapper.map(employeePosition, EmployeePosition.class));
				}
			});
		} catch (CFSException e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, "Exception : " + e);
		}

		return employeePositionList;
	}

	public List<EmployeePosition> getEmployeePositionsByOrganisationId(Long[] organisationId) throws CFSException {
		List<EmployeePosition> employeePositionList = new ArrayList<EmployeePosition>();

		try {
			employeePositionRepository.findByOrganisationId(organisationId).forEach(employeePosition -> {
				employeePositionList.add(mapper.map(employeePosition, EmployeePosition.class));
			});
		} catch (CFSException e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, "Exception : " + e);
		}

		return employeePositionList;
	}

	public List<EmployeePosition> getAllEmployeePositionByName(String name, Integer page, Integer size) throws CFSException {
		List<EmployeePosition> employeePositionList = new ArrayList<EmployeePosition>();

		try {
			Pageable pageable = PageRequest.of(page, size);
			employeePositionRepository.findAllByName(name, pageable).forEach(employeePosition -> {
				employeePositionList.add(mapper.map(employeePosition, EmployeePosition.class));
			});
		} catch (CFSException e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, "Exception : " + e);
		}

		return employeePositionList;
	}

	public List<UserDto> getUsers(Integer page, Integer size) throws CFSException {
		List<UserDto> userDtos = new ArrayList<UserDto>();

		try {
			Pageable pageable = PageRequest.of(page, size);
			userRepository.findAll(pageable).forEach(user -> {
				UserDto userDto = mapper.map(user, UserDto.class);
				userDto.setEmployeePosition(employeePositionRepository.findById(user.getEmployeePositionId()).get().getName());
				userDtos.add(userDto);
			});
		} catch (CFSException e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, "Exception : " + e);
		}

		return userDtos;
	}

	public List<UserDto> getUsersByOrganisationIds(Integer page, Integer size, String userId) throws CFSException {
		List<UserDto> userDtos = new ArrayList<UserDto>();

		try {
			Optional<User> loggedInUser = userRepository.findById(Long.valueOf(userId));
			List<Organisation> organisations = getOrganisations(userId);
			List<Long> organisationsStr = organisations.stream()
					   .map(Organisation::getId)
					   .collect(Collectors.toList());

			Long[] arr = new Long[organisationsStr.size()];
			int index = 0;
			for (final Long value : organisationsStr) {
				arr[index++] = value;
			}

			Pageable pageable = PageRequest.of(page, size);
			if(loggedInUser.isPresent()) {
				userRepository.findByOrganisationIds(arr, pageable).forEach(user -> {
					if(loggedInUser.get().getRole().getRoleControl().contains(user.getRole().getName())) {
						UserDto userDto = mapper.map(user, UserDto.class);
						if(user.getEmployeePositionId() != null) {
							userDto.setEmployeePosition(employeePositionRepository.findById(user.getEmployeePositionId()).get().getName());
						}

						userDtos.add(userDto);
					}
				});
			}
		} catch (CFSException e) {
			log.debug(e);
			throw new CFSException(ErrorCodes.ERROR_001, "Exception : " + e);
		}

		return userDtos;
	}
}
