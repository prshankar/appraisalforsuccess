function initializeSession(userId, userName, role, organisationId, jwtToken) {
	if(localStorage.getItem("userId") == null)  {
	    localStorage.setItem("userId", userId);
	}

	if(localStorage.getItem("userName") == null)  {
	    localStorage.setItem("userName", userName);
	}

	if(localStorage.getItem("role") == null)  {
	    localStorage.setItem("role", role);
	}

	if(localStorage.getItem("organisationId") == null)  {
	    localStorage.setItem("organisationId", organisationId);
	}

	if(localStorage.getItem("jwtToken") == null)  {
	    localStorage.setItem("jwtToken", jwtToken);
	}
}

var getUrlParameter = function getUrlParameter(sParam) {
    var sPageURL = window.location.search.substring(1),
        sURLVariables = sPageURL.split('&'),
        sParameterName,
        i;

    for (i = 0; i < sURLVariables.length; i++) {
        sParameterName = sURLVariables[i].split('=');

        if (sParameterName[0] === sParam) {
            return typeof sParameterName[1] === undefined ? true : decodeURIComponent(sParameterName[1]);
        }
    }
    return false;
};

function showPage(page) {
	document.getElementById('Authorization').value = "Bearer " +  localStorage.getItem("jwtToken");

	if(localStorage.getItem("userId") != null)  {
		document.menuForm.userId.value = localStorage.getItem("userId");
	}

	document.menuForm.action = page;
	document.menuForm.submit();
}

function showPopup(page) {
	window.open(page + "?Authorization=Bearer " + localStorage.getItem("jwtToken") + "&userId=" + localStorage.getItem("userId"), '_blank','width=900,height=300');
}

$('#appraisalForm').submit(function() {
	$('#appraisalForm').val(localStorage.getItem("userId"));
    return true;
});

function createUser(save) {
    if ($("#userForm")[0].checkValidity()) {
	    $("#userForm").find('input[name=userId]').val(localStorage.getItem("userId"));
	    $("#userForm").find('input[name=Authorization]').val("Bearer " + localStorage.getItem("jwtToken"));
	    $("#userForm").find('input[name=saveType]').val(save);
		$.post(
	      "/createOrUpdateUser",
	      $("#userForm").serialize(),
	      function(data) {
			if(data == true) {
				showAPIMessage('message', 'User created/updated successfully.', '#129c2b');
			    if(save == 'create') {
				    $("#userForm")[0].reset();
				    //$("#userForm")[0].find('#mobileNumber').prop('selectedIndex',0);
					showPage("/register");
			    }
			}
	      })
	      .fail(function(response) {
		  	showAPIMessage('message', response.responseJSON.message, 'red');
		  }
	    );
    } else {
        //Validate Form
        $("#userForm")[0].reportValidity()
    }
}

function createAppraisal() {
	var allApprailsalRowsSelected = checkAllRatings();
	if(!allApprailsalRowsSelected) {
		return;
	} else {
		if($("#appraisalUserId").val() == '') {
			$("#appraisalUserId").val(localStorage.getItem("userId"));
		}

	    $("#appraisalForm").find('input[name=userId]').val(localStorage.getItem("userId"));
	    $("#appraisalForm").find('input[name=Authorization]').val("Bearer " + localStorage.getItem("jwtToken"));
		$.post(
	      "/createOrUpdateAppraisal",
	      $("#appraisalForm").serialize(),
	      function(data) {
	      	$('#appraisalBody').replaceWith(data);
			showAPIMessage('message', 'Appraisal created/updated successfully.', '#129c2b');
	      })
	      .fail(function(response) {
			  showAPIMessage('modalMessage', response.responseJSON.message, 'red');
		  }
		);
	}
}

function submitAppraisal() {
	var allApprailsalRowsSelected = checkAllRatings();
	if(!allApprailsalRowsSelected) {
		return;
	} else {
		if(confirm("You are agreeing to submit your appraisal for the financial year " + $("#year").val() + " . Once submitted you will not be able to update the rating but you will still be able to update your comments.")) {
		    $("#appraisalForm").find('input[name=userId]').val($("#appraisalUserId").val());
		    $("#appraisalForm").find('input[name=Authorization]').val("Bearer " + localStorage.getItem("jwtToken"));
			$.post(
		      "/submitAppraisal",
		      $("#appraisalForm").serialize(),
		      function(data) {
				showAPIMessage('message', 'Appraisal submitted successfully.', '#129c2b');
				$("#appraisalSubmit").remove();
				$("#appraisalSave").remove();
				$("#appraisalReset").remove();
		    	$("#appraisalBody").find('input[type="checkbox"]').prop('disabled', true);
				getAppraisalYear();
		      })
		      .fail(function(response) {
		      	  showAPIMessage('message', response.responseJSON.message, 'red');
			  }
			);
		}
	}
}

function checkAllRatings() {
	var allApprailsalRowsSelected = true;
	$('#appraisalBody tr:not("#appraisalSubmitRow")').each(function() {
		var selected = [];
		if(localStorage.getItem("role") != null)  {
			if(localStorage.getItem("role") == 'EMPLOYEE')  {
				if($("#appraisalUserId").val() == '') {
					$("#appraisalUserId").val(localStorage.getItem("userId"));
				}

				$(this).find('.employeeCheckbox').each(function() {
			    	if ($(this).is(":checked")) {
			        	selected.push($(this));
			        	return false;
			       	}
			    });
			} else if(localStorage.getItem("role") == 'MANAGER')  {
				$(this).find('.managerCheckbox').each(function() {
			    	if ($(this).is(":checked")) {
			        	selected.push($(this));
			        	return false;
			       	}
			    });
			}
	
			if(selected.length == 0) {
				alert("Select atleast one of the rating in each value to proceed.");
				allApprailsalRowsSelected = false;
				return false;
			}
		}
	});

	return allApprailsalRowsSelected;
}

function createAppraisalMessage(id, appraisalId, message) {
	if(appraisalId == undefined) {
		appraisalId = $("#appraisalId").val();
	}

	if(appraisalId != null && appraisalId != '' && message != null && message != '') {
		$.ajax({
		   	url: "/createAppraisalMessage/" + localStorage.getItem("userId") + "/" + appraisalId + "/" + message,
		    type: "POST",
		    beforeSend: function(xhr){xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("jwtToken"));},
		    success: function(data) {
				if(data == true) {
					showAPIMessage('message', 'Appraisal Message created successfully.', '#129c2b');
					getLastComment(id, appraisalId);
				}
			},
			error: function (jqXHR, status, err) {
			  showAPIMessage('modalMessage', response.responseJSON.message, 'red');
			},
		});
	} else {
		alert("Enter comments.");
	}
}

function getLastComment(id, appraisalId) {
	if(appraisalId != null && appraisalId != '') {
		$.ajax({
		   	url: "/getLastComment/" + appraisalId,
		    type: "GET",
		    beforeSend: function(xhr){xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("jwtToken"));},
		    success: function(data) {
				$(id).prev().prev().val("");
				$(id).prev().prev().attr("placeholder", data);
			},
			error: function (jqXHR, status, err) {
			  showAPIMessage('modalMessage', response.responseJSON.message, 'red');
			},
		});
	}
}



function showAppraisalMessage(appraisalId) {
	if(appraisalId == null) {
		alert("Please submit the appraisal before starting a conversation.");
		return false;
	}

	$.ajax({
	   	url: "/showAppraisalMessage/" + appraisalId,
	    type: "POST",
	    beforeSend: function(xhr){xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("jwtToken"));},
	    success: function(data) {
    		var appraisal_message_window = window.open("", "_blank","width=800,height=800");
	        appraisal_message_window.document.write(data);
		}
	});

   	//$('#appraisal').css("display","block");
}

function resetPassword() {
	if ($("#resetPasswordForm")[0].checkValidity()) {
		var password = $('#password').val();
    	var confirmPassword = $('#confirmPassword').val();

		if (password != confirmPassword) {
		  alert('\nPassword and Confirm Password did not match.');
		  return false;
		} else {
			$.post(
		      "/resetPasswordUpdate?primaryEmail="+getUrlParameter('primaryEmail')+"&token=" + getUrlParameter('token'),
		      $("#resetPasswordForm").serialize(),
		      function(data) {
				if(data == 'SUCCESS') {
					showAPIMessage('message', 'Password reset successfully.', '#129c2b');
					passwordResetFormReset();
					
					//Redirect to login page after 5 seconds
					var delay = 7000; 
					setTimeout(function(){ window.location = "/"; }, delay);
				}
		      })
		      .fail(function(response) {
				showAPIMessage('message', response.responseJSON.message, 'red');
			  }
		   	);
		}
    } else {
        //Validate Form
        $("#resetPasswordForm")[0].reportValidity()
    }
}

function passwordResetFormReset() {
    $("#resetPasswordForm").trigger("reset");
}

function loginReset() {
    $("#loginForm").trigger("reset");
}

function createPdp() {
    $("#pdpForm").find('input[name=userId]').val(localStorage.getItem("userId"));
    $("#pdpForm").find('input[name=Authorization]').val("Bearer " + localStorage.getItem("jwtToken"));

	$.post(
      "/createOrUpdatePdp",
	  $("#pdpForm").find("input[type='hidden'], :input:not(:hidden)").serialize(),
      function(data) {
		if(data == true) {
			showAPIMessage('message', "Personal Development Plan created successfully.", '#129c2b');
		}
      })
      .fail(function(response) {
	  	showAPIMessage('message', response.responseJSON.message, 'red');
	  }
   );
}

/*function getYear() {
	$.ajax({
	   	url: "/getYear",
	    type: "GET",
	    beforeSend: function(xhr){xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("jwtToken"));},
	    success: function(data) {
			$.each(data, function (key, entry) {
				$("#year").append("<option value="+entry.year+">"+entry.year+"</option>");
			})
		}
	});
}*/

function getPerformanceGoalYear() {
	$.ajax({
	   	url: "/getPerformanceGoalYear/" + localStorage.getItem("organisationId"),
	    type: "GET",
	    beforeSend: function(xhr){xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("jwtToken"));},
	    success: function(data) {
			$.each(data, function (key, entry) {
				$("#performanceGoalYear").append("<option value="+entry+">"+entry+"</option>");
			})
		}
	});
}

function getPerformanceGoals(year) {
	if(year == '') {
      	$('#performanceGoalBody').replaceWith("<tbody id='performanceGoalBody'></tbody>");
      	$('#performancegoals').css("display","none");
	} else {
		$.ajax({
		   	url: "/getPerformanceGoals/" + localStorage.getItem("organisationId")[1] + "/" + year,
		    type: "GET",
		    beforeSend: function(xhr){xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("jwtToken"));},
		    success: function(data) {
		      	$('#performanceGoalBody').replaceWith(data);
			}
		});

	   	$('#performancegoals').css("display","block");
	}
}

function getPdp(year) {
	if(year == '') {
      	$('#pdpBody').replaceWith("<div class='formBox' id='pdpBody'></div>");
      	$('#pdp').css("display","none");
	} else {
		$.ajax({
		   	url: "/getPdp/" + localStorage.getItem("userId") + "/" + year,
		    type: "GET",
		    beforeSend: function(xhr){xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("jwtToken"));},
		    success: function(data) {
		      	$('#pdpBody').replaceWith(data);
			}
		});

	   	$('#pdp').css("display","block");
	}
}

/*function getValue() {
	$.ajax({
	   	url: "/getValue",
	    type: "GET",
	    beforeSend: function(xhr){xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("jwtToken"));},
	    success: function(data) {
			$.each(data, function (key, entry) {
					$("#valueId").append("<option value="+entry.id+">"+entry.name+"</option>");
		  	})
		}
	});
}*/

function getUserAppraisal() {
	let appraisalUserId = $('#appraisalUserId').find(":selected").val();
	let valueId = $('#valueId').find(":selected").val();
	let year = $('#year').find(":selected").val();

	if(appraisalUserId == undefined) {
		appraisalUserId = localStorage.getItem("userId");
	}

	if(appraisalUserId != '' && year != '' && valueId != '') {
	
		$.ajax({
		   	url: "/getUserAppraisal/" + appraisalUserId + "/" + year + "/" + valueId,
		    type: "GET",
		    beforeSend: function(xhr){xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("jwtToken"));},
		    success: function(data) {
		      	$('#appraisalBody').replaceWith(data);
			}
		});

	   	$('#appraisal').css("display","block");
	} else {
      	$('#appraisalBody').replaceWith("<tbody id='appraisalBody'><tr><td colspan='4'>Appraisal Not Found.</td></tr></tbody>");
      	$('#appraisal').css("display","none");
	}
}

function getAppraisalYear() {
	var userId = $(appraisalUserId).find('option:selected').val();
	if(userId != '') {
		$.ajax({
		   	url: "/getAppraisalYear/" + userId,
		    type: "GET",
		    beforeSend: function(xhr){xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("jwtToken"));},
		    success: function(response) {
		    	$("#valueId").find('option').not(':first').remove();
		    	$("#year").find('option').not(':first').remove();
			    response.forEach(function (year) {
					$("#year").append("<option value="+year+">"+year+"</option>");
			    });
			}
		});

      	$('#appraisalBody').replaceWith("<tbody id='appraisalBody'><tr><td colspan='4'></td></tr></tbody>");
	   	$('#appraisal').css("display", "none");
    	$("#valueId").val($("#target option:first").val());
	}
}

function logout() {
	cleanLocalStorage();
	window.location = "/";
}

function cleanLocalStorage() {
	localStorage.removeItem("userId");
	localStorage.removeItem("userName");
	localStorage.removeItem("role");
	localStorage.removeItem("organisationId");
	localStorage.removeItem("jwtToken");
}

function forgotPasswordEmail() {
	$.ajax({
	   	url: "/forgotPasswordEmail/" + $('#primaryEmail').val(),
	    type: "GET",
	    success: function(data) {
     	    showAPIMessage('message', 'Reset Password Email sent.', '#129c2b');
		    $('#message').fadeOut(7000);
		    $("#forgotPasswordForm")[0].reset();
		},
		fail: function(xhr, textStatus, errorThrown){
			showAPIMessage('message', textStatus, '#129c2b');
	  	}
	});
}

function showLabel() {
    $(".field-wrapper .field-placeholder").on("click", function () {
        $(this).closest(".field-wrapper").find("input").focus();
    });

    $(".field-wrapper input").on("keyup", function () {
        var value = $.trim($(this).val());
        if (value) {
            $(this).closest(".field-wrapper").addClass("hasValue");
        } else {
            $(this).closest(".field-wrapper").removeClass("hasValue");
        }
    });
}

function showLabelForDate() {
    $(".field-wrapper .field-placeholder").on("click", function () {
        $(this).closest(".field-wrapper").find("input").focus();
    });

    $(".field-wrapper input").on("change", function () {
        var value = $.trim($(this).val());
        if (value) {
            $(this).closest(".field-wrapper").addClass("hasValue");
        } else {
            $(this).closest(".field-wrapper").removeClass("hasValue");
        }
    });

	$(".reviewButton").on('click', function() {
		$("#startDateModalLabel").html("Start Review for " + $(this).data('user-name'));
		$("#userModelForm").find("#userId").val($(this).data('user-id'));
	});

	$("#startDateModal").on('hidden.bs.modal', function() {
	  $("#reviewDateDt").val('');
	});

	$("#startReviewSubmit").on('click', function() {
		$("#reviewDate").val($("#reviewDateDt").val());
	
		$.ajax({
		  url: "/updateReviewDate",
  	      data: $("#userModelForm").serialize(),
	      type: "POST",
	      beforeSend: function(xhr){xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("jwtToken"));},
	      success: function(data) {
			showAPIMessage('modalMessage', data, '#129c2b');
			setTimeout(function(){
	    	    $("#startDateModal").modal('toggle');
			}, 7000);
		  },
		  fail: function(xhr, textStatus, errorThrown){
			showAPIMessage('modalMessage', 'Review date updated.', '#129c2b');
		  }
		});
	});
}

function showAPIMessage(field, message, alertColor) {
	$('#' + field).css("display","block");
	$('#' + field).css("color", alertColor).html(message);
	$('#' + field).fadeOut(10000);
}

function insertRow() {
	let x = $('#pdpTable');
	let y = $('tbody').find('tr:hidden');

	if(y.length == 1) {
		$("#skill2").show();
	} else if(y.length == 2) {
		$("#skill1").show();
	}
}

function deleteRow() {
	let x = $('#pdpTable');
	let y = $('tbody').find('tr:hidden');

	if(y.length == 0) {
		$("#skill2").hide();
		$("#skillsToBeDeveloped2").val("");
		$("#howSkillsToBeDeveloped2").val("");
		$("#assistanceRequired2").val("");
		$("#whenn2").val("");
	} else if(y.length == 1) {
		$("#skill1").hide();
		$("#skillsToBeDeveloped1").val("");
		$("#howSkillsToBeDeveloped1").val("");
		$("#assistanceRequired1").val("");
		$("#whenn1").val("");
	}
}

function createOrUpdateOrganisation() {
    if ($("#organisationForm")[0].checkValidity()) {
	    $("#organisationForm").find('input[name=userId]').val(localStorage.getItem("userId"));
	    $("#organisationForm").find('input[name=Authorization]').val("Bearer " + localStorage.getItem("jwtToken"));
		$.post(
	      "/createOrUpdateOrganisation",
	      $("#organisationForm").serialize(),
	      function(data) {
			if(data == true) {
				showAPIMessage('message', 'Organisation created/updated successfully.', '#129c2b');
				organisationFormReset();

				$.ajax({
				   	url: "/getAllOrganisations",
				    type: "POST",
				    beforeSend: function(xhr){xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("jwtToken"));},
				    success: function(data) {
						$('#organisationDiv').empty().append(data);
			        	$('#organisations').DataTable();
					}
				});
			}
	      })
	      .fail(function(response) {
			showAPIMessage('message', response.responseJSON.message, 'red');
		  }
	   	);
    } else {
        //Validate Form
        $("#organisationForm")[0].reportValidity()
    }
}

function createOrUpdatePerformanceGoals() {
    if ($("#performanceGoalForm")[0].checkValidity()) {
	    $("#performanceGoalForm").find('input[name=userId]').val(localStorage.getItem("userId"));
	    $("#performanceGoalForm").find('input[name=Authorization]').val("Bearer " + localStorage.getItem("jwtToken"));
		$.post(
	      "/createOrUpdatePerformanceGoals",
	      $("#performanceGoalForm").serialize(),
	      function(data) {
			if(data == true) {
				showAPIMessage('message', 'Performance Goals created/updated successfully.', '#129c2b');
				//performanceGoalFormReset();

				$.ajax({
				   	url: "/getAllOrganisations",
				    type: "POST",
				    beforeSend: function(xhr){xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("jwtToken"));},
				    success: function(data) {
						$('#organisationDiv').empty().append(data);
			        	$('#organisations').DataTable();
					}
				});
			}
	      })
	      .fail(function(response) {
			showAPIMessage('message', response.responseJSON.message, 'red');
		  }
	   	);
    } else {
        //Validate Form
        $("#performanceGoalForm")[0].reportValidity()
    }
}

function performanceGoalFormReset() {
	$("#id").val('');
	$("#name").val('');
    $("#name").closest(".field-wrapper").removeClass("hasValue");
	$("#submit").html("<i class='ace-icon fa fa-check bigger-110'></i>Submit");
}

function updateOrganisation(id, name) {
	$("#id").val(id);
	$("#name").val(name);
    $("#name").closest(".field-wrapper").addClass("hasValue");
	$("#submit").html("<i class='ace-icon fa fa-check bigger-110'></i>Update");
}

function updateValue(id, name) {
	let organisationId = $('#organisationId').val();
	let employeePositionId = $('#employeePositionId').val();
	if(organisationId !== '' && employeePositionId != '') {
		$("#id").val(id);
		$("#name").val(name);
	    $("#name").closest(".field-wrapper").addClass("hasValue");
		$("#submit").html("<i class='ace-icon fa fa-check bigger-110'></i>Update");
	} else {
		alert("Select Organisation & Employee Position.");
		return;
	}
}

function deleteValue(id, name) {
	if(confirm("Are you sure want to delete value : " + name)) {
		$.ajax({
		  url: "/deleteValue/"+id,
		  type: 'GET',
    	  beforeSend: function(xhr){xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("jwtToken"));},
		  success: function(data) {
		  alert(data);
			if(data == true) {
				showAPIMessage('message', 'Value deleted successfully.', '#129c2b');
				getValuesByOrganisationAndEmployeePosition();
			}
		  },
		  fail: function(xhr, textStatus, errorThrown){
			showAPIMessage('message', errorThrown, 'red');
		  }
		});
	}
}

function updateTask(id, name, valueId) {
	if(id !== '' && name != null) {
		$("#valueId").val(valueId).trigger('change');
		$("#id").val(id);
		$("#name").val(name);
	    $("#name").closest(".field-wrapper").addClass("hasValue");
		$("#submit").html("<i class='ace-icon fa fa-check bigger-110'></i>Update");
	}
}

function deleteTask(id, name) {
	if(confirm("Are you sure want to delete task : " + name)) {
		$.ajax({
		  url: "/deleteTask/"+id+"?Authorization="+"Bearer " + localStorage.getItem("jwtToken"),
		  type: 'DELETE',
		  success: function(data) {
			if(data == true) {
				showAPIMessage('message', 'Task deleted successfully.', '#129c2b');
				getTasksByValue();
			}
		  },
		  fail: function(xhr, textStatus, errorThrown){
			showAPIMessage('message', errorThrown, 'red');
		  }
		});
	}
}

function deleteOrganisation(id, name) {
	if(confirm("Are you sure want to delete organisation : " + name)) {
		$.ajax({
		  url: "/deleteOrganisation/"+id+"?Authorization="+"Bearer " + localStorage.getItem("jwtToken"),
		  type: 'DELETE',
		  success: function(data) {
			if(data == true) {
				showAPIMessage('message', 'Organisation deleted successfully.', '#129c2b');

				$.ajax({
				   	url: "/getAllOrganisations",
				    type: "POST",
				    beforeSend: function(xhr){xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("jwtToken"));},
				    success: function(data) {
						$('#organisationDiv').empty().append(data);
			        	$('#organisations').DataTable();
					}
				});
			}
		  },
		  fail: function(xhr, textStatus, errorThrown){
			showAPIMessage('message', errorThrown, 'red');
		  }
		});
	}
}

function organisationFormReset() {
	$("#id").val('');
	$("#name").val('');
    $("#name").closest(".field-wrapper").removeClass("hasValue");
	$("#submit").html("<i class='ace-icon fa fa-check bigger-110'></i>Submit");
}

function createOrUpdateEmployeePosition() {
	const organisationId = $('#organisationId').val();
	if(organisationId != '' && $("#employeePositionForm")[0].checkValidity()) {
	    $("#employeePositionForm").find('input[name=userId]').val(localStorage.getItem("userId"));
	    $("#employeePositionForm").find('input[name=Authorization]').val("Bearer " + localStorage.getItem("jwtToken"));
		$.post(
	      "/createOrUpdateEmployeePosition",
	      $("#employeePositionForm").serialize(),
	      function(data) {
			if(data == true) {
				showAPIMessage('message', 'Employee Position created/updated successfully.', '#129c2b');
				employeePositionFormReset();
				var delay = 7000; 
				setTimeout(function(){ showPage("/administration/employeeposition"); }, delay);
			}
	      })
	      .fail(function(response) {
			showAPIMessage('message', response.responseJSON.message, 'red');
		  }
	   	);
	} else {
		if(organisationId == '') {
			alert("Select Organisation");
		} else {
	        //Validate Form
	        $("#employeePositionForm")[0].reportValidity()
		}
	}
}

function updateEmployeePosition(id, name, organisationId) {
	$("#id").val(id);
	$("#name").val(name);
	$("#organisationId").val(organisationId).trigger('change');
    $("#name").closest(".field-wrapper").addClass("hasValue");
	$("#submit").html("<i class='ace-icon fa fa-check bigger-110'></i>Update");
}

function deleteEmployeePosition(id, name) {
	if(confirm("Are you sure want to delete employee position : " + name)) {
		$.ajax({
		  url: "/deleteEmployeePosition/"+id+"?Authorization="+"Bearer " + localStorage.getItem("jwtToken"),
		  type: 'DELETE',
		  success: function(data) {
			if(data == true) {
				showAPIMessage('message', 'Employee Position deleted successfully.', '#129c2b');

				$.ajax({
				   	url: "/getAllEmployeePositions",
				    type: "POST",
				    beforeSend: function(xhr){xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("jwtToken"));},
				    success: function(data) {
						$('#employeePositionDiv').empty().append(data);
			        	$('#employeePositions').DataTable();
					}
				});
			}
		  },
		  fail: function(xhr, textStatus, errorThrown) {
			showAPIMessage('message', errorThrown, 'red');
		  }
		});
	}
}

function employeePositionFormReset() {
	$("#id").val('');
	$("#name").val('');
    $("#name").closest(".field-wrapper").removeClass("hasValue");
   	$("#organisationId").val('').trigger('change');
	$("#submit").html("<i class='ace-icon fa fa-check bigger-110'></i>Submit");
}

function createOrUpdateYear() {
    if ($("#yearForm")[0].checkValidity()) {
	    $("#yearForm").find('input[name=userId]').val(localStorage.getItem("userId"));
	    $("#yearForm").find('input[name=Authorization]').val("Bearer " + localStorage.getItem("jwtToken"));
		$.post(
	      "/createOrUpdateYear",
	      $("#yearForm").serialize(),
	      function(data) {
			if(data == true) {
				showAPIMessage('message', 'Year created/updated successfully.', '#129c2b');
				yearFormReset();

				$.ajax({
				   	url: "/getAllYears",
				    type: "POST",
				    beforeSend: function(xhr){xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("jwtToken"));},
				    success: function(data) {
						$('#yearDiv').empty().append(data);
			        	$('#years').DataTable();
					}
				});
			}
	      })
	      .fail(function(response) {
			showAPIMessage('message', response.responseJSON.message, 'red');
		  }
	   	);
    } else {
        //Validate Form
        $("#yearForm")[0].reportValidity()
    }
}

function updateYear(id, year, status) {
	$("#id").val(id);
	$("#year").val(year);
	$("#status").val(status).change();
    $("#year").closest(".field-wrapper").addClass("hasValue");
	$("#submit").html("<i class='ace-icon fa fa-check bigger-110'></i>Update");
}

function yearFormReset() {
	$("#id").val('');
	$("#year").val('');
	$("#status").val('');
    $("#name").closest(".field-wrapper").removeClass("hasValue");
	$("#submit").html("<i class='ace-icon fa fa-check bigger-110'></i>Submit");
}

function createOrUpdateGoal() {
    if ($("#goalForm")[0].checkValidity()) {
	    $("#goalForm").find('input[name=userId]').val(localStorage.getItem("userId"));
	    $("#goalForm").find('input[name=Authorization]').val("Bearer " + localStorage.getItem("jwtToken"));
		$.post(
	      "/createOrUpdateGoal",
	      $("#goalForm").serialize(),
	      function(data) {
			if(data == true) {
				showAPIMessage('message', 'Goal created/updated successfully.', '#129c2b');
				goalFormReset();

				$.ajax({
				   	url: "/getAllGoals",
				    type: "POST",
				    beforeSend: function(xhr){xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("jwtToken"));},
				    success: function(data) {
						$('#goalDiv').empty().append(data);
			        	$('#goals').DataTable();
					}
				});
			}
	      })
	      .fail(function(response) {
			showAPIMessage('message', response.responseJSON.message, 'red');
		  }
	   	);
    } else {
        //Validate Form
        $("#goalForm")[0].reportValidity()
    }
}

function updateGoal(id, name) {
	$("#id").val(id);
	$("#name").val(name);
    $("#name").closest(".field-wrapper").addClass("hasValue");
	$("#submit").html("<i class='ace-icon fa fa-check bigger-110'></i>Update");
}

function deleteGoal(id, name) {
	if(confirm("Are you sure want to delete goal : " + name)) {
		$.ajax({
		  url: "/deleteGoal/"+id+"?Authorization="+"Bearer " + localStorage.getItem("jwtToken"),
		  type: 'DELETE',
		  success: function(data) {
			if(data == true) {
				showAPIMessage('message', 'Goal deleted successfully.', '#129c2b');

				$.ajax({
				   	url: "/getAllGoals",
				    type: "POST",
				    beforeSend: function(xhr){xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("jwtToken"));},
				    success: function(data) {
						$('#goalDiv').empty().append(data);
			        	$('#goals').DataTable();
					}
				});
			}
		  },
		  fail: function(xhr, textStatus, errorThrown){
			showAPIMessage('message', errorThrown, 'red');
		  }
		});
	}
}

function goalFormReset() {
	$("#id").val('');
	$("#name").val('');
    $("#name").closest(".field-wrapper").removeClass("hasValue");
	$("#submit").html("<i class='ace-icon fa fa-check bigger-110'></i>Submit");
}

function valueFormReset() {
	$("#id").val('');
	$("#name").val('');
    $("#name").closest(".field-wrapper").removeClass("hasValue");
	$("#description").val('');
    $("#description").closest(".field-wrapper").removeClass("hasValue");
	$("#submit").html("<i class='ace-icon fa fa-check bigger-110'></i>Submit");
	$("#organisationId").val('').trigger('change')
	$("#employeePositionId").val('').trigger('change')
}

function taskFormReset() {
	$("#id").val('');
	$("#name").val('');
    $("#name").closest(".field-wrapper").removeClass("hasValue");
	$("#valueId").val('0').trigger('change');
	$("#submit").html("<i class='ace-icon fa fa-check bigger-110'></i>Submit");
}

function createOrUpdateValue() {
    if ($("#valueForm")[0].checkValidity()) {
	    $("#valueForm").find('input[name=userId]').val(localStorage.getItem("userId"));
	    $("#valueForm").find('input[name=Authorization]').val("Bearer " + localStorage.getItem("jwtToken"));
		$.post(
	      "/createOrUpdateValue",
	      $("#valueForm").serialize(),
	      function(data) {
			if(data == true) {
				showAPIMessage('message', 'Value created/updated successfully.', '#129c2b');
				getValuesByOrganisationAndEmployeePosition()
				valueFormReset();
			}
	      })
	      .fail(function(response) {
			showAPIMessage('message', response.responseJSON.message, 'red');
		  }
	   	);
    } else {
        //Validate Form
        $("#valueForm")[0].reportValidity()
    }
}

function createOrUpdateTask() {
	const valueId = $('#valueId').val();
	if(valueId != 0) {
	    if ($("#taskForm")[0].checkValidity()) {
		    $("#taskForm").find('input[name=userId]').val(localStorage.getItem("userId"));
		    $("#taskForm").find('input[name=Authorization]').val("Bearer " + localStorage.getItem("jwtToken"));
			$.post(
		      "/createOrUpdateTask",
		      $("#taskForm").serialize(),
		      function(data) {
				if(data == true) {
					showAPIMessage('message', 'Task created/updated successfully.', '#129c2b');
					getTasksByValue();
					taskFormReset();
				}
		      })
		      .fail(function(response) {
				showAPIMessage('message', response.responseJSON.message, 'red');
			  }
		   	);
	    } else {
	        //Validate Form
	        $("#taskForm")[0].reportValidity()
	    }
	} else {
		alert("Select Value");
	}
}

function createOrUpdateUser() {
    if ($("#userForm")[0].checkValidity()) {
	    $("#userForm").find('input[name=userId]').val(localStorage.getItem("userId"));
	    $("#userForm").find('input[name=Authorization]').val("Bearer " + localStorage.getItem("jwtToken"));
		$.post(
	      "/createOrUpdateUser",
	      $("#userForm").serialize(),
	      function(data) {
			if(data == true) {
				showAPIMessage('message', 'User created/updated successfully.', '#129c2b');
				userFormReset();

				$.ajax({
				   	url: "/getUsers/" + localStorage.getItem("userId"),
				    type: "POST",
				    beforeSend: function(xhr){xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("jwtToken"));},
				    success: function(data) {
						$('#userDiv').empty().append(data);
			        	$('#users').DataTable();
					}
				});
			}
	      })
	      .fail(function(response) {
			showAPIMessage('message', response.responseJSON.message, 'red');
		  }
	   	);
    } else {
        //Validate Form
        $("#userForm")[0].reportValidity()
    }
}

function updateUser(userId) {
	document.getElementById('userListForm').elements['Authorization'].value = "Bearer " +  localStorage.getItem("jwtToken");

	if(localStorage.getItem("userId") != null)  {
		document.getElementById('userListForm').elements['userId'].value = userId;
	}

	document.getElementById('userListForm').submit();
}

function activateUser(user) {
	var status = "activate";
	if(user.status == "ACTIVE") {
		status = "deactivate";
	}

	if(confirm("Are you sure want to " + status + " user : " + user.firstName + " " + user.lastName)) {
		$.ajax({
		  url: "/changeState/"+user.id,
	      type: "POST",
	      beforeSend: function(xhr){xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("jwtToken"));},
	      success: function(data) {
			if(data == "update") {
				showAPIMessage('message', 'User activate/deactivate successfully.', '#129c2b');

				$.ajax({
				   	url: "/getUsers/"+user.id,
				    type: "POST",
				    beforeSend: function(xhr){xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("jwtToken"));},
				    success: function(data) {
						$('#userDiv').empty().append(data);
			        	$('#users').DataTable();
					}
				});
			}
		  },
		  fail: function(xhr, textStatus, errorThrown){
			showAPIMessage('message', errorThrown, 'red');
		  }
		});
	}
}

function deleteUser(user) {
	if(confirm("Are you sure want to delete user : " + user.firstName + " " + user.lastName)) {
		$.ajax({
		  url: "/deleteUser/"+user.id,
	      type: "DELETE",
	      beforeSend: function(xhr){xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("jwtToken"));},
	      success: function(data) {
			if(data) {
				showAPIMessage('message', 'User deleted successfully.', '#129c2b');

				$.ajax({
				   	url: "/getUser/"+localStorage.getItem("userId"),
				    type: "POST",
				    beforeSend: function(xhr){xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("jwtToken"));},
				    success: function(data) {
						$('#userDiv').empty().append(data);
			        	$('#users').DataTable();
					}
				});
			}
		  },
		  fail: function(xhr, textStatus, errorThrown){
			showAPIMessage('message', errorThrown, 'red');
		  }
		});
	}
}

function userFormReset() {
	$("#userId").val('');
	$("#userName").val('');
    $("#userName").closest(".field-wrapper").removeClass("hasValue");
	//$("#organisationId").val(null).trigger('change');
    $("#userName").closest(".field-wrapper").removeClass("hasValue");
	$("#submit").html("<i class='ace-icon fa fa-check bigger-110'></i>Submit");
}

function getAdminPerformanceGoals() {
	const yearId = $('#year').val();
	const organisationId = $('#organisationId').val();

	if(year != '' && organisationId != '') {
		$.ajax({
		   	url: "/getAdminPerformanceGoals/" + organisationId + "/" + yearId,
		    type: "GET",
		    beforeSend: function(xhr){xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("jwtToken"));},
		    success: function(data) {
		      	$('#performanceGoalBody').replaceWith(data);
			}
		});

	   	$('#performancegoals').css("display","block");
	} else {
		if(year == '') {
			alert("Select year");
		}
	}
}

function getEmployeePosition() {
	const organisationId = $('#organisationId').val();
	if(organisationId == null) {
		$("#employeePositionId").empty();
		$("#employeePositionId").append("<option value=''>Select Employee Position</option>");
	} else {
		$.ajax({
		   	url: "/getEmployeePositions/" + $('#organisationId').val(),
		    type: "POST",
		    beforeSend: function(xhr){xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("jwtToken"));},
		    success: function(data) {
				$("#employeePositionId").empty();
				$("#employeePositionId").append("<option value=''>Select Employee Position</option>");
				$.each(data, function (key, entry) {
					$("#employeePositionId").append("<option value="+entry.id+">"+entry.name+"</option>");
				})
			}
		});
	}
}

function getValuesByOrganisationAndEmployeePosition() {
	let organisationId = $('#organisationId').val();
	let employeePositionId = $('#employeePositionId').val();
	if(employeePositionId == '') {
		employeePositionId = 'NULL';
	}

	if(organisationId !== '' && employeePositionId != '') {
		$.ajax({
		   	url: "/getValuesByOrganisationAndEmployeePosition/" + organisationId + "/" + employeePositionId,
		    type: "GET",
		    beforeSend: function(xhr){xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("jwtToken"));},
		    success: function(data) {
				$('#valueDiv').empty().append(data);
	        	$('#values').DataTable();
			}
		});
	}
}

function getValuesForAppraisal() {
	let selectedEmployeePositionId = $("#appraisalUserId").find(":selected").data('employee-position-id');
	let organisationId = $('#organisationId').val();
	let employeePositionId = $('#employeePositionId').val();

	if(selectedEmployeePositionId != '' && selectedEmployeePositionId != undefined) {
		employeePositionId = selectedEmployeePositionId;
	}
	
	if(organisationId !== '' && employeePositionId != '') {
		$.ajax({
		   	url: "/getValuesForAppraisal/" + organisationId + "/" + employeePositionId,
		    type: "GET",
		    beforeSend: function(xhr){xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("jwtToken"));},
		    success: function(data) {
		    	$("#valueId").find('option').not(':first').remove();
				$.each(data, function (key, entry) {
					$("#valueId").append("<option value="+entry.id+">"+entry.name+"</option>");
				})

				$("#valueId").find('option:eq(1)').prop('selected', true);
				getUserAppraisal();
			}
		});
	}
}

function getTasksByValue() {
	const valueId = $('#valueId').val();
	if(valueId != '' && valueId != 0) {
		$.ajax({
		   	url: "/getTasksByValue/" + valueId,
		    type: "GET",
		    beforeSend: function(xhr){xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("jwtToken"));},
		    success: function(data) {
				$('#taskDiv').empty().append(data);
	        	$('#tasks').DataTable();
			}
		});
	} else if(valueId == 0) {
		$.ajax({
		   	url: "/getAllTasks",
		    type: "GET",
		    beforeSend: function(xhr){xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("jwtToken"));},
		    success: function(data) {
				$('#taskDiv').empty().append(data);
	        	$('#tasks').DataTable();
			}
		});
	}
}

function getEmployeePositionsByOrganisation() {
	let organisationId = $('#organisationId').val();
	if(organisationId !== '') {
		$.ajax({
		   	url: "/getEmployeePositionsByOrganisation/" + organisationId,
		    type: "POST",
		    beforeSend: function(xhr){xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("jwtToken"));},
		    success: function(data) {
				$('#employeePositionDiv').empty().append(data);
	        	$('#employeePositions').DataTable();
			}
		});
	}
}


function formatPhoneNumber(elementId) {
	document.getElementById(elementId).addEventListener("keydown", function(e){
		if(e.keyCode != 8) {
			txt = this.value;
	   		if (txt.length == 4 || txt.length == 8)
	    		this.value = this.value + " ";
		}
	});
}