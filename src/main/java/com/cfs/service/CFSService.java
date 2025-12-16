package com.cfs.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cfs.dao.CFSDao;
import com.cfs.exception.CFSException;
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
import com.cfs.pojo.model.StartReviewDto;
import com.cfs.pojo.model.TaskDto;
import com.cfs.pojo.model.UserAppraisalDto;
import com.cfs.pojo.model.UserDto;
import com.cfs.pojo.model.ValueDto;
import com.cfs.pojo.model.YearDto;
import com.cfs.util.CFSAppraisalStatus;
import com.cfs.util.CFSEnum;

@Service
public class CFSService {

	@Autowired
	private CFSDao cfsDao;

	public UserDto getUser(Long userId) throws CFSException {
        return cfsDao.getUser(userId);
    }

	public UserDto getUserByPrimaryEmail(String primaryEmail) throws CFSException {
        return cfsDao.getUserByPrimaryEmail(primaryEmail);
    }

	public List<User> getAppraisalUser(Long userId, Long organisationId) throws CFSException {
        return cfsDao.getAppraisalUser(userId, organisationId);
    }

	public List<EmployeePosition> getAllEmployeePositions() throws CFSException {
        return cfsDao.getAllEmployeePositions();
    }

	public List<Organisation> getAllOrganisations() throws CFSException {
        return cfsDao.getAllOrganisations();
    }

	public List<Organisation> getOrganisations(String userId) throws CFSException {
        return cfsDao.getOrganisations(userId);
    }

	public List<String> getRoleControl(String userId) throws CFSException {
        return cfsDao.getRoleControl(userId);
    }

	public String getUserRole(String userId) throws CFSException {
        return cfsDao.getUserRole(userId);
    }

	public List<String> getAppraisalYear(String userId) throws CFSException {
        return cfsDao.getAppraisalYear(userId);
    }

	public List<String> getPerformanceGoalYear(Long organisationId) throws CFSException {
        return cfsDao.getPerformanceGoalYear(organisationId);
    }

	public PerformanceGoalDtos getPerformanceGoals(Long organisationId, String year) throws CFSException {
        return cfsDao.getPerformanceGoals(organisationId, year);
    }

	public PdpDto getPdp(Long userId, String year) throws CFSException {
        return cfsDao.getPdp(userId, year);
    }

	public List<Value> getValue() throws CFSException {
        return cfsDao.getValue();
    }

	public AppraisalDtos getUserAppraisals(Long userId, String year, Long valueId) throws CFSException {
        return cfsDao.getUserAppraisals(userId, year, valueId);
    }

	public String createOrUpdateUser(UserDto userDto) throws CFSException {
        return cfsDao.createOrUpdateUser(userDto);
    }

	public Boolean createOrUpdateAppraisal(AppraisalDtos appraisalDtos) throws CFSException {
        return cfsDao.createOrUpdateAppraisal(appraisalDtos);
    }

	public UserAppraisal submitAppraisal(UserAppraisalDto userAppraisalDto) throws CFSException {
		userAppraisalDto.setStatus(CFSAppraisalStatus.RATING_COMPLETE.toString());
        return cfsDao.submitAppraisal(userAppraisalDto);
    }

	public Boolean createAppraisalMessage(Long userId, Long appraisalId, String message) throws CFSException {
        return cfsDao.createAppraisalMessage(userId, appraisalId, message);
    }

	public AppraisalMessageDtos showAppraisalMessage(Long appraisalId) throws CFSException {
        return cfsDao.showAppraisalMessage(appraisalId);
    }

	public Boolean createOrUpdatePdp(PdpDto pdpDto) throws CFSException {
        return cfsDao.createOrUpdatePdp(pdpDto);
    }

	public void resetPassword(ResetPasswordDto resetPasswordDto) throws CFSException {
        cfsDao.resetPassword(resetPasswordDto);
    }

	public Boolean createOrUpdateOrganisation(OrganisationDto organisationDto) throws CFSException {
        return cfsDao.createOrUpdateOrganisation(organisationDto);
    }

	public Boolean deleteOrganisation(Long id) throws CFSException {
        return cfsDao.deleteOrganisation(id);
    }

	public Boolean deleteUser(Long userId) throws CFSException {
		cfsDao.deleteUserOrganisation(userId);
		cfsDao.deleteUserRole(userId);
		cfsDao.deleteUserAppraisal(userId);
		cfsDao.deleteAppraisal(userId);
		cfsDao.deleteUser(userId);

        return true;
    }

	public Boolean deleteValue(Long valueId) throws CFSException {
		cfsDao.deleteValue(valueId);
        return true;
    }

	public Boolean deleteTask(Long taskId) throws CFSException {
		cfsDao.deleteTask(taskId);
        return true;
    }

	public String changeState(Long userId) throws CFSException {
		UserDto userDto = cfsDao.getUser(userId);

		if(userDto.getStatus().equals(CFSEnum.ACTIVE.toString())) {
			userDto.setStatus(CFSEnum.INACTIVE.toString());
		} else {
			userDto.setStatus(CFSEnum.ACTIVE.toString());
		}

		return cfsDao.createOrUpdateUser(userDto);
    }

	public List<StartReviewDto> sendStartReviewNotification() throws CFSException {
		return cfsDao.sendStartReviewNotification();
    }

	public String updateReviewDate(ReviewDto reviewDto) throws CFSException {
		return cfsDao.updateReviewDate(reviewDto);
    }

	public Boolean createOrUpdateGoal(GoalDto goalDto) throws CFSException {
        return cfsDao.createOrUpdateGoal(goalDto);
    }

	public Boolean createOrUpdateValue(ValueDto valueDto) throws CFSException {
        return cfsDao.createOrUpdateValue(valueDto);
    }

	public Boolean createOrUpdateTask(TaskDto taskDto) throws CFSException {
        return cfsDao.createOrUpdateTask(taskDto);
    }

	public Boolean createOrUpdatePerformanceGoals(PerformanceGoalDtos performanceGoalDtos) throws CFSException {
        return cfsDao.createOrUpdatePerformanceGoals(performanceGoalDtos);
    }

	public Boolean deleteGoal(Long id) throws CFSException {
        return cfsDao.deleteGoal(id);
    }

	public List<Organisation> getAllOrganisations(Integer page, Integer size) throws CFSException {
        return cfsDao.getAllOrganisations(page, size);
    }

	public List<Goal> getAllGoals(Integer page, Integer size) throws CFSException {
        return cfsDao.getAllGoals(page, size);
    }

	public List<Value> getAllValues(Integer page, Integer size) throws CFSException {
        return cfsDao.getAllValues(page, size);
    }

	public List<Value> getValuesByOrganisationAndEmployeePosition(String organisationId, String employeePositionId) throws CFSException {
        return cfsDao.getValuesByOrganisationAndEmployeePosition(organisationId, employeePositionId);
    }

	public List<Value> getValuesForAppraisal(String organisationId, String employeePositionId) throws CFSException {
        return cfsDao.getValuesForAppraisal(organisationId, employeePositionId);
    }
	
	public String getLastComment(Long appraisalId) throws CFSException {
        return cfsDao.getLastComment(appraisalId);
    }

	public List<Task> getTasksByValue(Long valueId) throws CFSException {
        return cfsDao.getTasksByValue(valueId);
    }

	public List<Task> getAllTasks(Integer page, Integer size) throws CFSException {
        return cfsDao.getAllTasks(page, size);
    }

	public List<Year> getAllYears(boolean onlyActive, Integer page, Integer size) throws CFSException {
        return cfsDao.getAllYears(onlyActive, page, size);
    }

	public List<Organisation> getAllOrganisationByName(String organisationName, Integer page, Integer size) throws CFSException {
        return cfsDao.getAllOrganisationByName(organisationName, page, size);
    }

	public Boolean createOrUpdateEmployeePosition(EmployeePositionDto employeePositionDto) throws CFSException {
        return cfsDao.createOrUpdateEmployeePosition(employeePositionDto);
    }

	public Boolean deleteEmployeePosition(Long id) throws CFSException {
        return cfsDao.deleteEmployeePosition(id);
    }

	public Boolean createOrUpdateYear(YearDto yearDto) throws CFSException {
        return cfsDao.createOrUpdateYear(yearDto);
    }

	public List<EmployeePosition> getAllEmployeePositions(Integer page, Integer size) throws CFSException {
        return cfsDao.getAllEmployeePositions(page, size);
    }

	public List<EmployeePosition> getAllEmployeePositionByName(String name, Integer page, Integer size) throws CFSException {
        return cfsDao.getAllEmployeePositionByName(name, page, size);
    }

	public List<EmployeePosition> getEmployeePositionsByOrganisationId(Long[] organisationId) throws CFSException {
        return cfsDao.getEmployeePositionsByOrganisationId(organisationId);
    }

	public List<UserDto> getUsers(Integer page, Integer size) throws CFSException {
        return cfsDao.getUsers(page, size);
    }

	public List<UserDto> getUsersByOrganisationIds(Integer page, Integer size, String userId) throws CFSException {
        return cfsDao.getUsersByOrganisationIds(page, size, userId);
    }
}
