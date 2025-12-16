package com.cfs.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.cfs.dao.UserRepository;
import com.cfs.exception.CFSException;
import com.cfs.exception.ErrorCodes;
import com.cfs.pojo.constants.CFSConstants;
import com.cfs.pojo.entity.User;
import com.cfs.pojo.model.LoginRequest;
import com.cfs.pojo.model.LoginResponse;
import com.cfs.util.Util;

@Service(value = "userService")
public class AuthenticationService implements UserDetailsService {

	@Autowired
	private UserRepository userRespository;

	@Override
	public UserDetails loadUserByUsername(String primaryEmail) throws UsernameNotFoundException {
		Optional<User> user = userRespository.findByPrimaryEmail(primaryEmail);

		if (user == null) {
			throw new UsernameNotFoundException("User not found with username: " + primaryEmail);
		}

		return new org.springframework.security.core.userdetails.User(user.get().getPrimaryEmail(), user.get().getPassword(),
				getAuthority(user.get())); 
	}

    private Set<SimpleGrantedAuthority> getAuthority(User user) {
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName()));
        return authorities;
    }

    public LoginResponse login(LoginRequest loginRequest) throws CFSException {
		LoginResponse loginResponse = new LoginResponse();
		Optional<User> user = userRespository.findByPrimaryEmail(loginRequest.getPrimaryEmail());
		if (!user.isPresent()) {
			throw new CFSException(ErrorCodes.ERROR_001,
					"User not found having email: " + loginRequest.getPrimaryEmail() + "");
		} else {
			if(user.get().getStatus() != null && user.get().getStatus().equals("INACTIVE")) {
				throw new CFSException(ErrorCodes.ERROR_001,
						"Your account is inactive. Please contact the administrator.");
			}
			
			String password = loginRequest.getPassword();
			if(user.get().getFirstPasswordSet() != null && user.get().getFirstPasswordSet().equals("TRUE")) {
				password = Util.encodeString(loginRequest.getPassword());
			}

			if (user.get().getPrimaryEmail().equals(loginRequest.getPrimaryEmail())) {
				if (loginRequest.getPassword() != null
						&& !password.equals(user.get().getPassword())) {
					Long passwordRetriesLeft = user.get().getPasswordRetriesLeft() == null ? 5
							: user.get().getPasswordRetriesLeft();
					if (passwordRetriesLeft == 1) {
						throw new CFSException(ErrorCodes.ERROR_001,
								"Incorrect password. You have exhausted maximum password retry. Please contact administrator.");
					} else {
						--passwordRetriesLeft;
						updateUser(user.get().getId(), CFSConstants.password_attempt,
								passwordRetriesLeft.toString());
						throw new CFSException(ErrorCodes.ERROR_001,
								"Incorrect password. You have '" + passwordRetriesLeft + "' attempts remaining before being locked out.");
					}
				} else {
					if(user.get().getFirstPasswordSet() != null && user.get().getFirstPasswordSet().equals("FALSE")) {
						throw new CFSException(ErrorCodes.ERROR_008, "User needs reset password when logging in for the first time.");
					}

					Long passwordRetriesLeft = user.get().getPasswordRetriesLeft() == null ? 5 : user.get().getPasswordRetriesLeft();
					if (passwordRetriesLeft == 1) {
						throw new CFSException(ErrorCodes.ERROR_001,
								"Incorrect password. You have exhausted maximum password retry. Please contact administrator.");
					}

					if(user.get().getId() == 1) {
						updateUser(user.get().getId(), CFSConstants.password_attempt, "100000");
					} else {
						updateUser(user.get().getId(), CFSConstants.password_attempt, "5");
					}

					loginResponse.setUserId(user.get().getId());
					loginResponse.setUserName(user.get().getFirstName() + " " + user.get().getLastName());

					List<Long> organisationIds = new ArrayList<Long>();
					user.get().getOrganzations().forEach(userOrganisation -> {
						organisationIds.add(userOrganisation.getId());
					});

					loginResponse.setOrganisationId(organisationIds);
					loginResponse.setRole(user.get().getRole().getName());
				}
			} else {
				throw new CFSException(ErrorCodes.ERROR_001,
						"User not found having '" + loginRequest.getPrimaryEmail() + "'");
			}
		}

		loginResponse.setUserDetails(new org.springframework.security.core.userdetails.User(user.get().getPrimaryEmail(), user.get().getPassword(),
				new ArrayList<>()));

		return loginResponse;
	}

	/**
	 * it used to update the InspireProject with by spirProjectId, column and value
	 * 
	 * @param spirProjectId
	 * @param column
	 * @param value
	 */
	public void updateUser(Long id, String column, String value) {
		try {
			Optional<User> optional = userRespository.findById(id);
			if (optional.isPresent()) {
				User user = optional.get();
				if (user.getId() != null && column != null && value != null) {
					if (column.equals(CFSConstants.password_attempt))
						user.setPasswordRetriesLeft(Long.valueOf(value));
				}

				userRespository.save(user);
			}
		} catch (Exception e) {
			//log.log(Level.SEVERE, "Error while updateAppoUser: " + e.getMessage());
		}
	}
}