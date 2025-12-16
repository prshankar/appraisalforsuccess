
package com.cfs.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.cfs.config.JwtTokenUtil;
import com.cfs.email.EmailService;
import com.cfs.exception.CFSException;
import com.cfs.exception.ErrorCodes;
import com.cfs.jobs.ForgotPasswordNotificationTask;
import com.cfs.jobs.NewUserNotificationTask;
import com.cfs.pojo.entity.EmployeePosition;
import com.cfs.pojo.entity.Goal;
import com.cfs.pojo.entity.Organisation;
import com.cfs.pojo.entity.Task;
import com.cfs.pojo.entity.User;
import com.cfs.pojo.entity.UserAppraisal;
import com.cfs.pojo.entity.Value;
import com.cfs.pojo.entity.Year;
import com.cfs.pojo.model.AppraisalDtos;
import com.cfs.pojo.model.AppraisalMessageDtos;
import com.cfs.pojo.model.EmployeePositionDto;
import com.cfs.pojo.model.GoalDto;
import com.cfs.pojo.model.OrganisationDto;
import com.cfs.pojo.model.PdpDto;
import com.cfs.pojo.model.PerformanceGoalDtos;
import com.cfs.pojo.model.ResetPasswordDto;
import com.cfs.pojo.model.ReviewDto;
import com.cfs.pojo.model.TaskDto;
import com.cfs.pojo.model.UserAppraisalDto;
import com.cfs.pojo.model.UserDto;
import com.cfs.pojo.model.ValueDto;
import com.cfs.pojo.model.YearDto;
import com.cfs.service.CFSService;
import com.cfs.util.CFSEnum;
import com.fasterxml.jackson.core.JsonProcessingException;

@Controller
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class CFSController {

    private static final Logger logger = Logger.getLogger(CFSController.class);
    private ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);

	@org.springframework.beans.factory.annotation.Value("${jwt.resetpassword.token.validity}")
    public long RESET_PASSWORD_TOKEN_VALIDITY;

	@Autowired
	private CFSService cfsService;

	@Autowired
    public EmailService emailService;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Autowired
	private NewUserNotificationTask newUserNotificationTask;

	@Autowired
	private ForgotPasswordNotificationTask forgotPasswordNotificationTask;
	
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
	@GetMapping("/getPerformanceGoals/{organisationId}/{year}")
	public ModelAndView getPerformanceGoals(@Context HttpServletRequest request, @PathVariable Long organisationId, @PathVariable String year) {
    	ModelAndView mav = new ModelAndView();

    	try {
        	PerformanceGoalDtos performanceGoalDtos = cfsService.getPerformanceGoals(organisationId, year);
    		mav.setViewName("coach/performancegoals_results.html");
    		mav.addObject("performanceGoalDtos", performanceGoalDtos);
    	} catch(Exception e) {
    		logger.error("Error in /getPerformanceGoals route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
    	}

    	return mav;
	}

    @PreAuthorize("hasAnyRole('MANAGER','EMPLOYEE')")
	@GetMapping("/getPdp/{userId}/{year}")
	public ModelAndView getPdp(@Context HttpServletRequest request, @PathVariable Long userId, @PathVariable String year) {
		ModelAndView mav = new ModelAndView();

		try {
	    	PdpDto pdpDto = cfsService.getPdp(userId, year);
			mav.setViewName("coach/pdp_results.html");
			mav.addObject("pdpDto", pdpDto);
    	} catch(Exception e) {
    		logger.error("Error in /getPdp route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
    	}

		return mav;
	}

    @PreAuthorize("hasAnyRole('MANAGER','EMPLOYEE')")
	@GetMapping(value = "/getUserAppraisal/{userId}/{year}/{valueId}", produces = MediaType.APPLICATION_JSON)
	public ModelAndView getUserAppraisals(@Context HttpServletRequest request, @PathVariable Long userId, @PathVariable String year, @PathVariable Long valueId) throws JsonProcessingException {
    	ModelAndView mav = new ModelAndView();

    	try {
        	String role = jwtTokenUtil.getRoleFromToken(request.getHeader("Authorization").substring(7));
        	AppraisalDtos appraisalDtos = cfsService.getUserAppraisals(userId, year, valueId);
        	
        	if(role != null && role.equals(CFSEnum.MANAGER.toString())) {
        		appraisalDtos.setScreenLock(CFSEnum.UNLOCKED.toString());
        	}

    		mav.setViewName("coach/appraisal_rate.html");
    		mav.addObject("appraisalDtos", appraisalDtos);
    	} catch(Exception e) {
    		logger.error("Error in /getUserAppraisals route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
    	}

		return mav;
	}

    @PreAuthorize("hasAnyRole('MANAGER','EMPLOYEE')")
	@PostMapping(value = "/createOrUpdateAppraisal")
	public ModelAndView createOrUpdateAppraisal(@Context HttpServletRequest request, @ModelAttribute("appraisalDtos") AppraisalDtos appraisalDtos) throws CFSException, JsonProcessingException {
    	ModelAndView mav = new ModelAndView();

    	try {
    		ResponseEntity<Boolean> responseEntity = new ResponseEntity<Boolean>(cfsService.createOrUpdateAppraisal(appraisalDtos), HttpStatus.OK);

    		if(responseEntity.getStatusCode().is2xxSuccessful()) {
    			AppraisalDtos appraisalDto = cfsService.getUserAppraisals(appraisalDtos.getAppraisalUserId(), appraisalDtos.getYear(), appraisalDtos.getValueId());
    			mav.setViewName("coach/appraisal_rate.html");
    			mav.addObject("appraisalDtos", appraisalDto);
    		}
    	} catch(Exception e) {
    		logger.error("Error in /createOrUpdateAppraisal route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
    	}
		
		return mav;
	}

    @PreAuthorize("hasAnyRole('MANAGER','EMPLOYEE')")
	@PostMapping(value = "/showAppraisalMessage/{appraisalId}")
    public ModelAndView showAppraisalMessage(@Context HttpServletRequest request, @PathVariable Long appraisalId) throws CFSException, JsonProcessingException {
    	ModelAndView mav = new ModelAndView();

    	try {
        	AppraisalMessageDtos appraisalMessageDtos = cfsService.showAppraisalMessage(appraisalId);
    		mav.setViewName("coach/appraisal_messages.html");
    		mav.addObject("appraisalMessageDtos", appraisalMessageDtos);
    	} catch(Exception e) {
    		logger.error("Error in /showAppraisalMessage route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
    	}

		return mav;
	}
	
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERUSER')")
    @PostMapping("/administration/organisation")
	public ModelAndView organisation(@Context HttpServletRequest request) {
    	ModelAndView mav = new ModelAndView();

    	try {
    		mav.setViewName("admin/organisation.html");
    		mav.addObject("selectedMenu", "administration");

    		List<Organisation> organisations = cfsService.getAllOrganisations(0, Integer.MAX_VALUE);
    		mav.addObject("organisations", organisations);
    	} catch(Exception e) {
    		logger.error("Error in /administration/organisation route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
    	}

		return mav;
	}

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERUSER')")
    @PostMapping("/administration/goal")
	public ModelAndView goal(@Context HttpServletRequest request) {
    	ModelAndView mav = new ModelAndView();

    	try {
    		mav.setViewName("admin/goal.html");
    		mav.addObject("selectedMenu", "administration");

    		List<Goal> goals = cfsService.getAllGoals(0, Integer.MAX_VALUE);
    		mav.addObject("goals", goals);
    	} catch(Exception e) {
    		logger.error("Error in /administration/goal route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
    	}

		return mav;
	}

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERUSER')")
    @PostMapping("/administration/value")
	public ModelAndView value(@Context HttpServletRequest request) {
    	ModelAndView mav = new ModelAndView();

    	try {
    		mav.setViewName("admin/value.html");
    		mav.addObject("selectedMenu", "administration");

    		List<Organisation> organisations = cfsService.getAllOrganisations(0, Integer.MAX_VALUE);
    		mav.addObject("organisations", organisations);

    		List<Value> values = cfsService.getAllValues(0, Integer.MAX_VALUE);
    		mav.addObject("values", values);
    	} catch(Exception e) {
    		logger.error("Error in /administration/value route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
    	}
    	
		return mav;
	}

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERUSER')")
    @PostMapping("/administration/task")
	public ModelAndView task(@Context HttpServletRequest request) {
    	ModelAndView mav = new ModelAndView();

    	try {
    		mav.setViewName("admin/task.html");
    		mav.addObject("selectedMenu", "administration");

    		List<Value> values = cfsService.getAllValues(0, Integer.MAX_VALUE);
    		mav.addObject("values", values);

    		List<Task> tasks = cfsService.getAllTasks(0, Integer.MAX_VALUE);
    		mav.addObject("tasks", tasks);
    	} catch(Exception e) {
    		logger.error("Error in /administration/task route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
    	}

		return mav;
	}

    @PreAuthorize("hasAnyRole('ADMIN','SUPERUSER')")
    @PostMapping("/administration/employeeposition")
	public ModelAndView employeeposition(@Context HttpServletRequest request) {
    	ModelAndView mav = new ModelAndView();

    	try {
    		mav.setViewName("admin/employee_position.html");
    		mav.addObject("selectedMenu", "administration");

    		List<Organisation> organisations = cfsService.getAllOrganisations(0, Integer.MAX_VALUE);
    		mav.addObject("organisations", organisations);

    		List<EmployeePosition> employeePositions = cfsService.getAllEmployeePositions(0, Integer.MAX_VALUE);
    		mav.addObject("employeePositions", employeePositions);
    	} catch(Exception e) {
    		logger.error("Error in /administration/employeeposition route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
    	}

		return mav;
	}

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERUSER')")
    @PostMapping("/administration/user")
	public ModelAndView user() {
    	ModelAndView mav = new ModelAndView();

    	try {
    		mav.setViewName("admin/user.html");
    		mav.addObject("selectedMenu", "administration");

    		List<UserDto> userDtos = cfsService.getUsers(0, Integer.MAX_VALUE);
    		mav.addObject("users", userDtos);
    	} catch(Exception e) {
    		logger.error("Error in /administration/user route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
    	}

		return mav;
	}

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERUSER')")
    @PostMapping("/administration/performancegoals")
	public ModelAndView performancegoals() {
    	ModelAndView mav = new ModelAndView();

    	try {
    		mav.setViewName("admin/performancegoals.html");
    		mav.addObject("selectedMenu", "administration");

    		List<Organisation> organisations = cfsService.getAllOrganisations(0, Integer.MAX_VALUE);
    		mav.addObject("organisations", organisations);

    		List<Year> years = cfsService.getAllYears(true, 0, Integer.MAX_VALUE);
    		mav.addObject("years", years);
    	} catch(Exception e) {
    		logger.error("Error in /administration/performancegoals route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
    	}

		return mav;
	}

    @PreAuthorize("hasAnyRole('ADMIN','SUPERUSER','MANAGER','EMPLOYEE')")
	@GetMapping("/getAdminPerformanceGoals/{organisationId}/{year}")
	public ModelAndView getAdminPerformanceGoals(@Context HttpServletRequest request, @PathVariable Long organisationId, @PathVariable String year) {
		ModelAndView mav = new ModelAndView();

		try {
	    	PerformanceGoalDtos performanceGoalDtos = cfsService.getPerformanceGoals(organisationId, year);
			mav.setViewName("admin/performancegoals_results.html");
			mav.addObject("performanceGoalDtos", performanceGoalDtos);
    	} catch(Exception e) {
    		logger.error("Error in /getAdminPerformanceGoals route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
    	}

		return mav;
	}

    @PreAuthorize("hasAnyRole('ADMIN','SUPERUSER')")
    @PostMapping("/administration/year")
	public ModelAndView year(@Context HttpServletRequest request) {
    	ModelAndView mav = new ModelAndView();

    	try {
    		mav.setViewName("admin/year.html");
    		mav.addObject("selectedMenu", "administration");

    		List<Year> years = cfsService.getAllYears(false, 0, Integer.MAX_VALUE);
    		mav.addObject("years", years);
    	} catch(Exception e) {
    		logger.error("Error in /administration/year route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
    	}

		return mav;
	}

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERUSER')")
	@PostMapping("/getUser/{userId}")
    public ModelAndView getUser(@Context HttpServletRequest request, @PathVariable String userId) {
    	ModelAndView mav = new ModelAndView();

    	try {
			List<UserDto> users = cfsService.getUsersByOrganisationIds(0, Integer.MAX_VALUE, userId);
			mav.addObject("users", users);
    		mav.setViewName("admin/user_list.html");
    	} catch(Exception e) {
    		logger.error("Error in /getUser route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
    	}
		
		return mav;
	}
    
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERUSER')")
	@PostMapping("/getAllOrganisations")
	public ModelAndView getAllOrganisations(@Context HttpServletRequest request) {
    	ModelAndView mav = new ModelAndView();

    	try {
    		List<Organisation> organisations = cfsService.getAllOrganisations(0, Integer.MAX_VALUE);
    		mav.addObject("organisations", organisations);
    		mav.setViewName("admin/organisation_list.html");
    	} catch(Exception e) {
    		logger.error("Error in /getAllOrganisations route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
    	}
		
		return mav;
	}

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERUSER')")
	@PostMapping("/getAllGoals")
	public ModelAndView getAllGoals(@Context HttpServletRequest request) {
    	ModelAndView mav = new ModelAndView();

    	try {
    		List<Goal> goals = cfsService.getAllGoals(0, Integer.MAX_VALUE);
    		mav.addObject("goals", goals);
    		mav.setViewName("admin/goal_list.html");
    	} catch(Exception e) {
    		logger.error("Error in /getAllGoals route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
    	}
		
		return mav;
	}

    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','ADMIN','SUPERUSER')")
    @GetMapping("/getValuesByOrganisationAndEmployeePosition/{organisationId}/{employeePositionId}")
	public ModelAndView getValuesByOrganisationAndEmployeePosition(@Context HttpServletRequest request, @PathVariable String organisationId, @PathVariable String employeePositionId) {
    	ModelAndView mav = new ModelAndView();

    	try {
    		List<Value> values = cfsService.getValuesByOrganisationAndEmployeePosition(organisationId, employeePositionId);
    		mav.addObject("values", values);
    		mav.setViewName("admin/value_list.html");
    	} catch(Exception e) {
    		logger.error("Error in /getValuesByOrganisationAndEmployeePosition route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
    	}
		
		return mav;
	}

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERUSER')")
    @GetMapping("/getTasksByValue/{valueId}")
	public ModelAndView getTasksByValue(@Context HttpServletRequest request, @PathVariable Long valueId) {
    	ModelAndView mav = new ModelAndView();

    	try {
    		List<Task> tasks = cfsService.getTasksByValue(valueId);
    		mav.addObject("tasks", tasks);
    		mav.setViewName("admin/task_list.html");
    	} catch(Exception e) {
    		logger.error("Error in /getTasksByValue route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
    	}

		return mav;
	}

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERUSER')")
    @GetMapping("/getAllTasks")
	public ModelAndView getAllTasks(@Context HttpServletRequest request) {
    	ModelAndView mav = new ModelAndView();

    	try {
    		List<Task> tasks = cfsService.getAllTasks(0, Integer.MAX_VALUE);
    		mav.addObject("tasks", tasks);
    		mav.setViewName("admin/task_list.html");
    	} catch(Exception e) {
    		logger.error("Error in /getTasksByValue route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
    	}

		return mav;
	}
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERUSER')")
	@PostMapping("/getAllEmployeePositions")
	public ModelAndView getAllEmployeePositions(@Context HttpServletRequest request) {
    	ModelAndView mav = new ModelAndView();

    	try {
    		List<EmployeePosition> employeePositions = cfsService.getAllEmployeePositions(0, Integer.MAX_VALUE);
    		mav.addObject("employeePositions", employeePositions);
    		mav.setViewName("admin/employee_position_list.html");
    	} catch(Exception e) {
    		logger.error("Error in /getAllEmployeePositions route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
    	}

		return mav;
	}

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERUSER')")
	@PostMapping("/getEmployeePositionsByOrganisation/{organisationId}")
	public ModelAndView getEmployeePositionsByOrganisation(@Context HttpServletRequest request, @PathVariable Long[] organisationId) {
    	ModelAndView mav = new ModelAndView();

    	try {
    		List<EmployeePosition> employeePositions = cfsService.getEmployeePositionsByOrganisationId(organisationId);
    		mav.addObject("employeePositions", employeePositions);
    		mav.setViewName("admin/employee_position_list.html");
    	} catch(Exception e) {
    		logger.error("Error in /getEmployeePositionsByOrganisation route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
    	}

		return mav;
	}

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERUSER')")
	@PostMapping("/getAllYears")
	public ModelAndView getAllYears(@Context HttpServletRequest request) {
    	ModelAndView mav = new ModelAndView();

    	try {
    		List<Year> years = cfsService.getAllYears(false, 0, Integer.MAX_VALUE);
    		mav.addObject("years", years);
    		mav.setViewName("admin/year_list.html");
    	} catch(Exception e) {
    		logger.error("Error in /getAllYears route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
    	}
		
		return mav;
	}

    @RequestMapping("/")
	public ModelAndView login(@Context HttpServletRequest request) {
    	ModelAndView mav = new ModelAndView();

    	try {
    		mav.setViewName("login/login.html");
    	} catch(Exception e) {
    		logger.error("Error in login page route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
    	}

		return mav;
	}

    @PreAuthorize("hasAnyRole('SUPERUSER','ADMIN','MANAGER')")
    @PostMapping("/register")
	public ModelAndView register(@Context HttpServletRequest request) {
		ModelAndView mav = new ModelAndView();

		try {
	    	String userId = getUserId(request);
			String role = jwtTokenUtil.getRoleFromToken(request.getParameter("Authorization").substring(7));
			List<Organisation> organisations = cfsService.getOrganisations(userId);
			List<String> roleControl = cfsService.getRoleControl(role);
			List<EmployeePosition> employeePositions = cfsService.getAllEmployeePositions();
			mav.setViewName("admin/register.html");
			mav.addObject("selectedMenu", "administration");
			mav.addObject("organisations", organisations);
			mav.addObject("roleControl", roleControl);
			//mav.addObject("employeePositions", employeePositions);

			List<UserDto> userDtos = cfsService.getUsersByOrganisationIds(0, Integer.MAX_VALUE, userId);
			mav.addObject("users", userDtos);
		} catch(Exception e) {
    		logger.error("Error in /register route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
    	}
		
		return mav;
	}

	@RequestMapping("/forgotPassword")
	public ModelAndView forgotPassword(@Context HttpServletRequest request) {
		ModelAndView mav = new ModelAndView();

		try {
			mav.setViewName("login/forgotpassword.html");
    	} catch(Exception e) {
    		logger.error("Error in /forgotPassword route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
    	}

		return mav;
	}

	@RequestMapping("/resetPassword")
	public ModelAndView resetPassword(@Context HttpServletRequest request) {
		ModelAndView mav = new ModelAndView();

		try {
			mav.setViewName("login/resetpassword.html");
    	} catch(Exception e) {
    		logger.error("Error in /resetPassword route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
    	}

		return mav;
	}

	@PostMapping(value = "/resetPasswordUpdate")
	public ResponseEntity<String> resetPassword(@Context HttpServletRequest request, @ModelAttribute("passwordDto") ResetPasswordDto resetPasswordDto) throws CFSException, JsonProcessingException {
        try {
        	String token = request.getParameter("token");
        	resetPasswordDto.setPrimaryEmail(request.getParameter("primaryEmail"));
        	if(jwtTokenUtil.validateToken(token)) {
            	cfsService.resetPassword(resetPasswordDto);
        	} else {
    			throw new CFSException(ErrorCodes.ERROR_004, "Link has expired.");
        	}
		} catch (Exception e) {
    		logger.error("Error in /resetPasswordUpdate route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
		}

    	return new ResponseEntity<String>("SUCCESS", HttpStatus.OK);
	}

    @PreAuthorize("hasAnyRole('SUPERUSER','ADMIN','MANAGER','EMPLOYEE')")
    @PostMapping("/dashboard")
	public ModelAndView dashboard(@Context HttpServletRequest request) {
    	ModelAndView mav = new ModelAndView();

    	try {
    		mav.setViewName("coach/dashboard.html");
    		mav.addObject("selectedMenu", "dashboard");
    	} catch(Exception e) {
    		logger.error("Error in /dashboard route : " + e);
    	}

		return mav;
	}

    @PreAuthorize("hasAnyRole('SUPERUSER','ADMIN','MANAGER')")
    @PostMapping("/administration")
	public ModelAndView administration() {
    	ModelAndView mav = new ModelAndView();

    	try {
    		mav.setViewName("admin/index.html");
    		mav.addObject("selectedMenu", "administration");
    	} catch(Exception e) {
    		logger.error("Error in /administration route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
    	}

		return mav;
	}

    @PreAuthorize("hasAnyRole('SUPERUSER','ADMIN','MANAGER','EMPLOYEE')")
    @PostMapping("/faq")
	public ModelAndView faq(@Context HttpServletRequest request) {
    	ModelAndView mav = new ModelAndView();

    	try {
    		mav.setViewName("coach/faq.html");
    		mav.addObject("selectedMenu", "faq");
    	} catch(Exception e) {
    		logger.error("Error in /faq route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
    	}

		return mav;
	}

    @PreAuthorize("hasAnyRole('SUPERUSER','ADMIN','MANAGER','EMPLOYEE')")
	@PostMapping("/personaldetails")
	public ModelAndView personaldetails(@Context HttpServletRequest request) throws CFSException, JsonProcessingException {
		ModelAndView mav = new ModelAndView();

		try {
	    	String userId = request.getParameter("userId");

			UserDto user = cfsService.getUser(Long.valueOf(userId));
			mav.setViewName("coach/personaldetails.html");
			mav.addObject("selectedMenu", "personaldetails");
			mav.addObject("user", user);
    	} catch(Exception e) {
    		logger.error("Error in /personaldetails route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
    	}

		return mav;
	}

    @PreAuthorize("hasAnyRole('SUPERUSER','ADMIN','MANAGER')")
	@PostMapping("/edituser")
	public ModelAndView editUser(@Context HttpServletRequest request) throws CFSException, JsonProcessingException {
		ModelAndView mav = new ModelAndView();

		try {
	    	String userId = request.getParameter("userId");
			UserDto user = cfsService.getUser(Long.valueOf(userId));
			System.out.println(user.getDob());

			mav.setViewName("admin/edituser.html");
			mav.addObject("selectedMenu", "administration");
			mav.addObject("user", user);

			String role = jwtTokenUtil.getRoleFromToken(request.getParameter("Authorization").substring(7));
			List<String> roleControl = cfsService.getRoleControl(role);
			String userRole = cfsService.getUserRole(userId);
			mav.addObject("roleControl", roleControl);
			mav.addObject("userRole", userRole);
		} catch(Exception e) {
    		logger.error("Error in /edituser route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
    	}

		return mav;
	}

    @PreAuthorize("hasAnyRole('SUPERUSER','ADMIN','MANAGER','EMPLOYEE')")
    @PostMapping("/performancegoals")
	public ModelAndView performancegoals(@Context HttpServletRequest request) {
    	ModelAndView mav = new ModelAndView();
    	PerformanceGoalDtos performanceGoalDtos = new PerformanceGoalDtos();

    	try {
    		mav.setViewName("coach/performancegoals.html");
    		mav.addObject("selectedMenu", "performancegoals");

    		List<Year> years = cfsService.getAllYears(true, 0, Integer.MAX_VALUE);
    		mav.addObject("years", years);
    		if(years.size() > 0) {
        		mav.addObject("latestYear", years.get(0).getYear());
        		List<Integer> organisationIds = jwtTokenUtil.getOrganisationIdFromToken(request.getParameter("Authorization").substring(7));		
            	performanceGoalDtos = cfsService.getPerformanceGoals(new Long(organisationIds.get(0)), years.get(0).getYear());
    		}

    		mav.addObject("performanceGoalDtos", performanceGoalDtos);
    	} catch(Exception e) {
    		logger.error("Error in /performancegoals route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
    	}

		return mav;
	}

    @PreAuthorize("hasAnyRole('SUPERUSER','ADMIN','MANAGER','EMPLOYEE')")
    @PostMapping("/appraisal")
	public ModelAndView appraisal(@Context HttpServletRequest request) {
    	ModelAndView mav = new ModelAndView();

    	try {
    		mav.setViewName("coach/appraisal.html");
    		mav.addObject("selectedMenu", "appraisal");

    		List<Integer> organisationIds = jwtTokenUtil.getOrganisationIdFromToken(request.getParameter("Authorization").substring(7));		
    		String userId = getUserId(request);
    		UserDto user = cfsService.getUser(Long.valueOf(userId));

    		List<User> appraisalUsers = cfsService.getAppraisalUser(Long.valueOf(userId), new Long(organisationIds.get(0)));
    		mav.addObject("appraisalUsers", appraisalUsers);
    		mav.addObject("organisationId", organisationIds.get(0));
    		mav.addObject("employeePositionId", user.getEmployeePositionId());

    		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    		if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"))) {
    			List<String> years = cfsService.getAppraisalYear(userId);
    			if(!years.contains(CFSEnum.CURRENT.toString())) {
    				years.add(0, CFSEnum.CURRENT.toString());
    			}
    			mav.addObject("years", years);
    		}

    		//List<Value> values = cfsService.getValue();
    		//mav.addObject("values", values);
    	} catch(Exception e) {
    		logger.error("Error in /appraisal route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
    	}

		return mav;
	}

    @PreAuthorize("hasAnyRole('SUPERUSER','ADMIN','MANAGER','EMPLOYEE')")
    @GetMapping("/appraisalMessages")
	public ModelAndView appraisalMessages(@Context HttpServletRequest request) {
    	ModelAndView mav = new ModelAndView();

    	try {
    		mav.setViewName("coach/appraisal_messages.html");
    	} catch(Exception e) {
    		logger.error("Error in /appraisalMessages route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
    	}

		return mav;
	}

    @PreAuthorize("hasAnyRole('SUPERUSER','ADMIN','MANAGER','EMPLOYEE')")
    @PostMapping("/pdp")
	public ModelAndView pdp(@Context HttpServletRequest request) {
    	ModelAndView mav = new ModelAndView();

    	try {
    		mav.setViewName("coach/pdp.html");
    		mav.addObject("selectedMenu", "pdp");

    		String userId = getUserId(request);

    		List<Year> years = cfsService.getAllYears(true, 0, Integer.MAX_VALUE);
    		mav.addObject("years", years);
    		mav.addObject("latestYear", years.get(0));

    		PdpDto pdpDto = cfsService.getPdp(new Long(userId), years.get(0).getYear().toString());
    		mav.addObject("pdpDto", pdpDto);
    	} catch(Exception e) {
    		logger.error("Error in /pdp route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
    	}

		return mav;
	}

    @PreAuthorize("hasAnyRole('SUPERUSER','ADMIN','MANAGER','EMPLOYEE')")
	@GetMapping("/getUser/{userId}")
	public ResponseEntity<UserDto> getUser(@Context HttpServletRequest request, @PathVariable Long userId) {
    	UserDto userDto = null;

    	try {
    		userDto = cfsService.getUser(userId);
		} catch (Exception e) {
    		logger.error("Error in /getUser route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
		}

    	return new ResponseEntity<UserDto>(userDto, HttpStatus.OK);
	}

    @PreAuthorize("hasAnyRole('SUPERUSER','ADMIN','MANAGER','EMPLOYEE')")
	@GetMapping("/getAppraisalUser/{organisationId}")
	public ResponseEntity<List<User>> getAppraisalUser(@Context HttpServletRequest request, @PathVariable Long organisationId) {
    	List<User> users = null;

    	try {
        	String userId = getUserId(request);
        	users = cfsService.getAppraisalUser(Long.valueOf(userId), organisationId);
    	} catch (Exception e) {
    		logger.error("Error in /getAppraisalUser route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
		}

    	return new ResponseEntity<List<User>>(users, HttpStatus.OK);
	}

    @PreAuthorize("hasAnyRole('SUPERUSER','ADMIN','MANAGER','EMPLOYEE')")
	@GetMapping("/getPerformanceGoalYear/{organisationId}")
	public ResponseEntity<List<String>> getPerformanceGoalYear(@Context HttpServletRequest request, @PathVariable Long organisationId) {
    	List<String> performanceGoalYears = null;

    	try {
    		performanceGoalYears = cfsService.getPerformanceGoalYear(organisationId);
		} catch (Exception e) {
    		logger.error("Error in /getPerformanceGoalYear route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
		}

    	return new ResponseEntity<List<String>>(performanceGoalYears, HttpStatus.OK);
	}

    @PreAuthorize("hasAnyRole('MANAGER','EMPLOYEE')")
	@GetMapping("/getValue")
	public ResponseEntity<List<Value>> getValue(@Context HttpServletRequest request) {
    	List<Value> values= null;

    	try {
    		values = cfsService.getValue();
		} catch (Exception e) {
    		logger.error("Error in /getValue route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
		}
    	
    	return new ResponseEntity<List<Value>>(values, HttpStatus.OK);
	}

    @PreAuthorize("hasAnyRole('SUPERUSER','ADMIN','MANAGER','EMPLOYEE')")
	@PostMapping(value = "/createOrUpdateUser")
	public ResponseEntity<Boolean> createOrUpdateUser(@Context HttpServletRequest request, UserDto userDto) throws CFSException, IOException, MessagingException {
        try {
        	String userId = getUserId(request);

        	Map<String, Object> templateModel = new HashMap<String, Object>();
            userDto.setMobileNumber(userDto.getMobileNumber().replaceAll("\\s+",""));
            userDto.setReviewerId(Long.valueOf(userId));
            String newPassword = cfsService.createOrUpdateUser(userDto);
            if(newPassword != null && !newPassword.equals("") && userDto.getSaveType().equals("create")) {
                templateModel.put("newPassword", newPassword);
                templateModel.put("primaryEmail", userDto.getPrimaryEmail());
                templateModel.put("userName", userDto.getFirstName() + " "+ userDto.getLastName());

                newUserNotificationTask.setTemplateModel(templateModel);
                newUserNotificationTask.setPrimaryEmail(userDto.getPrimaryEmail());
                executor.execute(newUserNotificationTask);
            }
        } catch (Exception e) {
    		logger.error("Error in /createOrUpdateUser route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
		}

        return new ResponseEntity<Boolean>(new Boolean(true), HttpStatus.OK);
	}

	@GetMapping(value = "/forgotPasswordEmail/{primaryEmail}")
	public ResponseEntity<Boolean> forgotPassword(@Context HttpServletRequest request, @PathVariable String primaryEmail) throws CFSException {
		Map<String, Object> templateModel = new HashMap<String, Object>();

        Boolean isTokenValid = new Boolean(true);
        if(isTokenValid) {
            try {
            	String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
            			.replacePath(null)
            			.build()
            			.toUriString();
                String passwordResetLink = baseUrl + "/resetPassword?primaryEmail=" + primaryEmail + "&token=" + jwtTokenUtil.generateResetpasswordToken(RESET_PASSWORD_TOKEN_VALIDITY);
                templateModel.put("passwordResetLink", passwordResetLink);

                UserDto user = cfsService.getUserByPrimaryEmail(primaryEmail);
	    		logger.error(user == null);
                System.out.println("User with email id11 '" + primaryEmail + "' not found");
                if(user == null) {
                	System.out.println("User with email id '" + primaryEmail + "' not found");
                	throw new Exception("User with email id '" + primaryEmail + "' not found");
                }
                
                if(user != null && user.getFirstName() != null && user.getLastName() != null) {
                    templateModel.put("userName", user.getFirstName() + " "+ user.getLastName());
                    forgotPasswordNotificationTask.setTemplateModel(templateModel);
                    forgotPasswordNotificationTask.setPrimaryEmail(primaryEmail);
                    executor.execute(forgotPasswordNotificationTask);
                }
            } catch (CFSException e) {
	    		logger.error("Error in /forgotPassword route : " + e);
				throw new CFSException(ErrorCodes.ERROR_004, e);
            } catch (Exception e) {
	    		logger.error("Error in /forgotPassword route : " + e);
				throw new CFSException(ErrorCodes.ERROR_004, e);
			}
        }
        
		return new ResponseEntity<Boolean>(isTokenValid, HttpStatus.OK);
	}

    @PreAuthorize("hasAnyRole('MANAGER','EMPLOYEE')")
	@PostMapping(value = "/submitAppraisal")
	public ResponseEntity<UserAppraisal> submitAppraisal(@Context HttpServletRequest request, @ModelAttribute("userAppraisalDto") UserAppraisalDto userAppraisalDto) throws CFSException, JsonProcessingException {
    	ResponseEntity<UserAppraisal> responseEntity = null;
    	Map<String, Object> templateModel = new HashMap<String, Object>();

        try {
        	responseEntity = new ResponseEntity<UserAppraisal>(cfsService.submitAppraisal(userAppraisalDto), HttpStatus.OK);
        	UserDto userDto = cfsService.getUser(userAppraisalDto.getUserId());
        	emailService.sendMessageUsingThymeleafTemplate(
            		userDto.getPrimaryEmail(),
                    "Welcome to Coach For Success!!!",
                    templateModel, "/email/employeereviewcomplete.html");
        } catch (Exception e) {
    		logger.error("Error in /submitAppraisal route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
		}

    	return responseEntity;
	}
    
    @PreAuthorize("hasAnyRole('MANAGER','EMPLOYEE')")
	@PostMapping(value = "/createAppraisalMessage/{userId}/{appraisalId}/{message}")
    public ResponseEntity<Boolean> createAppraisalMessage(@Context HttpServletRequest request, @PathVariable Long userId, @PathVariable Long appraisalId, @PathVariable String message) throws CFSException, JsonProcessingException {
    	Boolean isAppraisalMessageCreated = new Boolean(false);

    	try {
    		isAppraisalMessageCreated = cfsService.createAppraisalMessage(userId, appraisalId, message);
		} catch (Exception e) {
    		logger.error("Error in /createAppraisalMessage route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
		}

    	return new ResponseEntity<Boolean>(isAppraisalMessageCreated, HttpStatus.OK);
	}

    @PreAuthorize("hasAnyRole('MANAGER','EMPLOYEE')")
	@PostMapping(value = "/createOrUpdatePdp")
	public ResponseEntity<Boolean> createOrUpdatePdp(@Context HttpServletRequest request, @ModelAttribute("pdp") PdpDto pdpDto) throws CFSException, JsonProcessingException {
    	Boolean isPdpCreated = new Boolean(false);

    	try {
    		isPdpCreated = cfsService.createOrUpdatePdp(pdpDto);
		} catch (Exception e) {
    		logger.error("Error in /createOrUpdatePdp route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
		}

    	return new ResponseEntity<Boolean>(isPdpCreated, HttpStatus.OK);
	}

    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','ADMIN','SUPERUSER')")
    @GetMapping("/getValuesForAppraisal/{organisationId}/{employeePositionId}")
	public ResponseEntity<List<Value>> getValuesForAppraisal(@Context HttpServletRequest request, @PathVariable String organisationId, @PathVariable String employeePositionId) {
    	List<Value> valuesForAppraisal = null;

    	try {
    		valuesForAppraisal = cfsService.getValuesForAppraisal(organisationId, employeePositionId);
		} catch (Exception e) {
    		logger.error("Error in /getValuesForAppraisal route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
		}

    	return new ResponseEntity<List<Value>>(valuesForAppraisal, HttpStatus.OK);
	}

    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','ADMIN','SUPERUSER')")
    @GetMapping("/getLastComment/{appraisalId}")
	public ResponseEntity<String> getLastComment(@Context HttpServletRequest request, @PathVariable Long appraisalId) {
    	String lastComment = "";

    	try {
    		lastComment = cfsService.getLastComment(appraisalId);
		} catch (Exception e) {
    		logger.error("Error in /getLastComment route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
		}

    	return new ResponseEntity<String>(lastComment, HttpStatus.OK);
	}

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERUSER')")
	@GetMapping("/getAllOrganisationByName/{name}/{page}/{size}")
	public ResponseEntity<List<Organisation>> getAllOrganisationByName(@PathVariable String name, @PathVariable Integer page, @PathVariable Integer size) {
    	List<Organisation> organisations = null;

    	try {
    		organisations = cfsService.getAllOrganisationByName(name, page, size);
		} catch (Exception e) {
    		logger.error("Error in /getAllOrganisationByName route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
		}

    	return new ResponseEntity<List<Organisation>>(organisations, HttpStatus.OK);
	}

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERUSER')")
	@GetMapping("/getAllEmployeePositionByName/{name}/{page}/{size}")
	public ResponseEntity<List<EmployeePosition>> getAllEmployeePositionByName(@PathVariable String name, @PathVariable Integer page, @PathVariable Integer size) {
    	List<EmployeePosition> employeePositions = null;

    	try {
    		employeePositions = cfsService.getAllEmployeePositionByName(name, page, size);
		} catch (Exception e) {
    		logger.error("Error in /getAllEmployeePositionByName route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
		}

    	return new ResponseEntity<List<EmployeePosition>>(employeePositions, HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERUSER')")
	@PostMapping("/getEmployeePositions/{organisationIds}")
	public ResponseEntity<List<EmployeePosition>> getEmployeePositionsByOrganisationId(@PathVariable Long[] organisationIds) {
    	List<EmployeePosition> employeePositions = null;

    	try {
    		employeePositions = cfsService.getEmployeePositionsByOrganisationId(organisationIds);
		} catch (Exception e) {
    		logger.error("Error in /getEmployeePositions route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
		}

		return new ResponseEntity<List<EmployeePosition>>(employeePositions, HttpStatus.OK);
	}
    
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERUSER')")
	@PostMapping(value = "/createOrUpdateOrganisation")
	public ResponseEntity<Boolean> createOrUpdateOrganisation(@ModelAttribute("organisation") OrganisationDto organisationDto) throws CFSException, JsonProcessingException {
    	Boolean isCreated = new Boolean(false);

    	try {
    		isCreated = cfsService.createOrUpdateOrganisation(organisationDto);
		} catch (Exception e) {
    		logger.error("Error in /createOrUpdateOrganisation route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
		}

    	return new ResponseEntity<Boolean>(isCreated, HttpStatus.OK);
	}

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERUSER')")
	@DeleteMapping(value = "/deleteOrganisation/{organisationId}")
	public ResponseEntity<Boolean> deleteOrganisation(@PathVariable Long organisationId) throws CFSException, JsonProcessingException {
    	Boolean isDeleted = new Boolean(false);

    	try {
    		isDeleted = cfsService.deleteOrganisation(organisationId);
		} catch (Exception e) {
    		logger.error("Error in /deleteOrganisation route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
		}

    	return new ResponseEntity<Boolean>(isDeleted, HttpStatus.OK);
	}

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERUSER')")
	@DeleteMapping(value = "/deleteUser/{userId}")
	public ResponseEntity<Boolean> deleteUser(@PathVariable Long userId) throws CFSException, JsonProcessingException {
    	Boolean isDeleted = new Boolean(false);

    	try {
    		isDeleted = cfsService.deleteUser(userId);
		} catch (Exception e) {
    		logger.error("Error in /deleteUser route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
		}

    	return new ResponseEntity<Boolean>(isDeleted, HttpStatus.OK);
	}

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERUSER')")
	@GetMapping(value = "/deleteValue/{valueId}")
	public ResponseEntity<Boolean> deleteValue(@PathVariable String valueId) throws CFSException, JsonProcessingException {
    	Boolean isDeleted = new Boolean(false);

    	try {
    		isDeleted = cfsService.deleteValue(Long.valueOf(valueId));
		} catch (Exception e) {
    		logger.error("Error in /deleteValue route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
		}

    	return new ResponseEntity<Boolean>(isDeleted, HttpStatus.OK);
	}

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERUSER')")
	@DeleteMapping(value = "/deleteTask/{taskId}")
	public ResponseEntity<Boolean> deleteTask(@PathVariable Long taskId) throws CFSException, JsonProcessingException {
    	Boolean isDeleted = new Boolean(false);

    	try {
    		isDeleted = cfsService.deleteTask(taskId);
		} catch (Exception e) {
    		logger.error("Error in /deleteTask route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
		}

    	return new ResponseEntity<Boolean>(isDeleted, HttpStatus.OK);
	}

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERUSER')")
	@PostMapping(value = "/changeState/{userId}")
	public ResponseEntity<String> changeState(@PathVariable Long userId) throws CFSException, JsonProcessingException {
    	String state = "";

    	try {
    		state = cfsService.changeState(userId);
		} catch (Exception e) {
    		logger.error("Error in /changeState route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
		}

    	return new ResponseEntity<String>(state, HttpStatus.OK);
	}

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERUSER')")
	@PostMapping(value = "/updateReviewDate")
	public ResponseEntity<String> updateReviewDate(@ModelAttribute("reviewDto") ReviewDto reviewDto) throws CFSException, JsonProcessingException {
    	String status = "";

    	try {
    		status = cfsService.updateReviewDate(reviewDto);
		} catch (Exception e) {
    		logger.error("Error in /updateReviewDate route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
		}

    	return new ResponseEntity<String>(status, HttpStatus.OK);
	}

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERUSER')")
	@PostMapping(value = "/createOrUpdateGoal")
	public ResponseEntity<Boolean> createOrUpdateGoal(@ModelAttribute("goal") GoalDto goalDto) throws CFSException, JsonProcessingException {
    	Boolean isCreated = new Boolean(false);

    	try {
    		isCreated = cfsService.createOrUpdateGoal(goalDto);
		} catch (Exception e) {
    		logger.error("Error in /createOrUpdateGoal route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
		}

    	return new ResponseEntity<Boolean>(isCreated, HttpStatus.OK);
	}

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERUSER')")
	@PostMapping(value = "/createOrUpdateValue")
	public ResponseEntity<Boolean> createOrUpdateValue(@ModelAttribute("value") ValueDto valueDto) throws CFSException, JsonProcessingException {
    	Boolean isCreated = new Boolean(false);

    	try {
    		isCreated = cfsService.createOrUpdateValue(valueDto);
		} catch (Exception e) {
    		logger.error("Error in /createOrUpdateValue route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
		}

    	return new ResponseEntity<Boolean>(isCreated, HttpStatus.OK);
	}

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERUSER')")
	@PostMapping(value = "/createOrUpdateTask")
	public ResponseEntity<Boolean> createOrUpdateTask(@ModelAttribute("task") TaskDto taskDto) throws CFSException, JsonProcessingException {
    	Boolean isCreated = new Boolean(false);

    	try {
    		isCreated = cfsService.createOrUpdateTask(taskDto);
		} catch (Exception e) {
    		logger.error("Error in /createOrUpdateTask route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
		}

    	return new ResponseEntity<Boolean>(isCreated, HttpStatus.OK);
	}

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERUSER')")
	@DeleteMapping(value = "/deleteGoal/{goalId}")
	public ResponseEntity<Boolean> deleteGoal(@PathVariable Long goalId) throws CFSException, JsonProcessingException {
    	Boolean isDeleted = new Boolean(false);

    	try {
    		isDeleted = cfsService.deleteGoal(goalId);
		} catch (Exception e) {
    		logger.error("Error in /deleteGoal route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
		}

    	return new ResponseEntity<Boolean>(isDeleted, HttpStatus.OK);
	}

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERUSER')")
	@PostMapping(value = "/createOrUpdatePerformanceGoals")
	public ResponseEntity<Boolean> createOrUpdatePerformanceGoals(@ModelAttribute("performanceGoalDtos") PerformanceGoalDtos performanceGoalDtos) throws CFSException, JsonProcessingException {
    	Boolean isCreated = new Boolean(false);

    	try {
    		isCreated = cfsService.createOrUpdatePerformanceGoals(performanceGoalDtos);
		} catch (Exception e) {
    		logger.error("Error in /createOrUpdatePerformanceGoals route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
		}

    	return new ResponseEntity<Boolean>(isCreated, HttpStatus.OK);
	}

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERUSER')")
	@PostMapping(value = "/createOrUpdateEmployeePosition")
	public ResponseEntity<Boolean> createOrUpdateEmployeePosition(@ModelAttribute("employeePosition") EmployeePositionDto employeePositionDto) throws CFSException, JsonProcessingException {
    	Boolean isCreated = new Boolean(false);

    	try {
    		isCreated = cfsService.createOrUpdateEmployeePosition(employeePositionDto);
		} catch (Exception e) {
    		logger.error("Error in /createOrUpdateEmployeePosition route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
		}

    	return new ResponseEntity<Boolean>(isCreated, HttpStatus.OK);
	}

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERUSER')")
	@DeleteMapping(value = "/deleteEmployeePosition/{id}")
	public ResponseEntity<Boolean> deleteEmployeePosition(@PathVariable Long id) throws CFSException, JsonProcessingException {
    	Boolean isDeleted = new Boolean(false);

    	try {
    		isDeleted = cfsService.deleteEmployeePosition(id);
		} catch (Exception e) {
    		logger.error("Error in /deleteEmployeePosition route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
		}

    	return new ResponseEntity<Boolean>(isDeleted, HttpStatus.OK);
	}

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERUSER')")
	@PostMapping(value = "/createOrUpdateYear")
	public ResponseEntity<Boolean> createOrUpdateYear(@ModelAttribute("year") YearDto yearDto) throws CFSException, JsonProcessingException {
    	Boolean isCreated = new Boolean(false);

    	try {
    		isCreated = cfsService.createOrUpdateYear(yearDto);
		} catch (Exception e) {
    		logger.error("Error in /createOrUpdateYear route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
		}

    	return new ResponseEntity<Boolean>(isCreated, HttpStatus.OK);
	}

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERUSER')")
	@GetMapping("/getAppraisalYear/{userId}")
	public ResponseEntity<List<String>> getAppraisalYear(@PathVariable String userId) {
    	List<String> appraisalYear = null;

    	try {
    		appraisalYear = cfsService.getAppraisalYear(userId);
		} catch (Exception e) {
    		logger.error("Error in /getAppraisalYear route : " + e);
			throw new CFSException(ErrorCodes.ERROR_004, e);
		}

    	return new ResponseEntity<List<String>>(appraisalYear, HttpStatus.OK);
	}

	private String getUserId(HttpServletRequest request) {
		return jwtTokenUtil.getUserIdFromToken(request.getParameter("Authorization").substring(7));
	}
}