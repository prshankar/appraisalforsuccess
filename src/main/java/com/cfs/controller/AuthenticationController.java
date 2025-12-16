package com.cfs.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.cfs.config.JwtTokenUtil;
import com.cfs.exception.CFSException;
import com.cfs.pojo.model.LoginRequest;
import com.cfs.pojo.model.LoginResponse;
import com.cfs.service.AuthenticationService;

@Controller
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AuthenticationController {

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Value("${jwt.token.validity}")
    public long TOKEN_VALIDITY;

	@Value("${jwt.resetpassword.token.validity}")
    public long RESET_PASSWORD_TOKEN_VALIDITY;

	@Autowired
	private AuthenticationService authenticationService;

    @Autowired
    private AuthenticationManager authenticationManager;

	@PostMapping(value = "/loginservice")
	public Object loginservice(LoginRequest loginRequest) throws CFSException {
		ModelAndView mav = new ModelAndView();

		try {
			LoginResponse loginResponse  = authenticationService.login(loginRequest);
			loginResponse.setUserDetails(null);

			final Authentication authentication = authenticationManager.authenticate(
	                new UsernamePasswordAuthenticationToken(
	                		loginRequest.getPrimaryEmail(),
	                		loginRequest.getPassword()
	                )
	        );

			SecurityContextHolder.getContext().setAuthentication(authentication);
	        String token = jwtTokenUtil.generateToken(authentication, loginResponse.getOrganisationId(), loginResponse.getUserId(), loginResponse.getRole(), TOKEN_VALIDITY);
	        loginResponse.setJwttoken(token);

			mav.setViewName("coach/dashboard.html");
			mav.addObject("selectedMenu", "dashboard");

			mav.addObject("userId", loginResponse.getUserId());
			mav.addObject("userName", loginResponse.getUserName());
			mav.addObject("role", loginResponse.getRole());
			mav.addObject("organisationId", loginResponse.getOrganisationId());
			mav.addObject("jwtToken", loginResponse.getJwttoken());
		} catch (CFSException e) {
			if(e.getErrorCode().equals(e.getErrorCode().ERROR_008)) {
				//mav.setViewName("login/resetpassword.html");
			    return "redirect:/resetPassword?token="+jwtTokenUtil.generateResetpasswordToken(RESET_PASSWORD_TOKEN_VALIDITY)
			    +"&primaryEmail="+loginRequest.getPrimaryEmail();
			} else {
				mav.setViewName("login/login.html");
				mav.addObject("message", e.getMessage());
			}
		}

		return mav;
	}

	@PostMapping(value = "/logout")
	public ModelAndView logout() throws CFSException {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("login/login.html");
		return mav;
	}
}