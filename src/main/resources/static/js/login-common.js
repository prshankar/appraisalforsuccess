
(function($) {
  $.fn.kristab = function(options) {
	  
	   var settings = $.extend({
            activeClass:"tabActive",
			mobDestroyTab:false,
			breakpoint:1000,
			callback:function(){}
            }, options);
			
			
    return this.each(function() {
		
		var $this = $(this);
		var kid = $this.children();
		
		var $z = [];
		kid.each(function(){
			$z.push($(this).text())
			})
		
		if(settings.mobDestroyTab==false){
		$this.children(':first').addClass(settings.activeClass);
		$this.next().children().hide();
		$this.next().children(':first').show();
		}else{
			if($(window).width()>settings.breakpoint){
				$this.children(':first').addClass(settings.activeClass);
		$this.next().children().hide();
		$this.next().children(':first').show();
			}else{
				$this.hide();
				$this.next().children().each(function(i){
					$(this).prepend("<h2>"+$z[i]+"</h2>");
					})
				}
			
			}
				
		kid.click(function(){
			 var findtabcont = $(this).index()+1;
			 if($this.next().children(':nth-child('+findtabcont+')').is(':visible')==false){
		$this.next().children().hide();
		kid.removeClass(settings.activeClass);
		  }
		 $this.next().children(':nth-child('+findtabcont+')').fadeIn(function(){
			setTimeout(function(){settings.callback()},100);
			 }); 
			$(this).addClass(settings.activeClass);
			
		})
		
    });
	
     }
})(jQuery);
// tab plagin end

(function($) {
  $.fn.equalKidHeight = function(options) {
			
		 var settings = $.extend({
			 destroyHeight:false,
			destroyHeightPoint:1000,
			callback:function(){}
            }, options);	
			
    return this.each(function() {
		
		var $this = $(this);
		var kid = $this.children();
		
	var $a = [];	
	    kid.each(function(){
		$a.push($(this).outerHeight());
		})
		if($this.is(':visible') || settings.destroyHeight==false){				
		kid.outerHeight(Math.max.apply(Math, $a));		
		if($(window).width()<settings.destroyHeightPoint){
				kid.css('height','auto');
			}
		}
		
    });
	
     }
})(jQuery);
// equalkid height plagin end


$(function(){

var loginSec1Height =  function(){
	if($(window).width()>999){
		$('.loginSec1').outerHeight($(window).height());
	}else{
		$('.loginSec1').css('height','auto');
		}
}

loginSec1Height();
$(window).on('resize',function(){
	loginSec1Height();
	})
	

if($(window).width()>1024){
$('.uspList li').hover(function(){
	$(this).find('.uspPop').stop().fadeIn('fast');
	},
	function(){
	$(this).find('.uspPop').stop().fadeOut();
	}
	)
}else{
	$('.openUspInfo').click(function(){
	$('.uspPop').fadeOut();
	$(this).parent().find('.uspPop').fadeIn();
	return false
	})
	}
	
	
		
$('.uspPop samp').click(function(){
	$(this).parent().fadeOut();
	})

var navOpen = false;
$('.navTrigger').click(function(){
	if(navOpen == false){
		$(this).addClass('navTriggerActive');
	$('#navHolder').animate({'right':'0'});
	$('.top_panel').addClass('top_panelMoved')
	$('body').animate({'margin-left':'-165px','margin-right':'165px'},500);
	navOpen = true;
	}else{
		$(this).removeClass('navTriggerActive');
		$('#navHolder').animate({'right':'-165px'});
	$('.top_panel').removeClass('top_panelMoved')
	$('body').animate({'margin-left':'0px','margin-right':'0px'},500);
	navOpen = false;
		}
	})
	
	
 $('.forgot_pass').on('click',function(){
	   $('.signIn').addClass('signInHide');
	   $('.password_reset').addClass('forgot_password_show');
	})
   $('#next').on('click',function(){
	   $('.password_reset_success').addClass('password_reset_success_show');
	})
	
	 $('.fp_close').click(function(){
	   $('.signIn').removeClass('signInHide');
	   $('.password_reset').removeClass('forgot_password_show');
	})
	
	 $('.fp_close.goSignin, .goSignin').click(function(){
	   $('.signIn').removeClass('signInHide');
	   $('.password_reset').removeClass('forgot_password_show');
	    $('.password_reset_success').removeClass('password_reset_success_show');
	})
	
	
})