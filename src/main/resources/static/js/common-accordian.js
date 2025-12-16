// JavaScript Document

(function($) {
  $.fn.krisAccordian = function(options) {
	  
	  var settings = $.extend({
            activeClass:"active",
			int:function(){},
			initiallyContShow:true,
			slideDownCallback:function(){},
			slideUpCallback:function(){}
            }, options);
			
    return this.each(function() {
		
		settings.int();
		var accordian = $(this);
		var kid = accordian.children();
		
		if(settings.initiallyContShow==true){
		accordian.children(':first').addClass(settings.activeClass);
		accordian.children().children(':last-child').hide();
		accordian.children(':first').children(':last-child').show();
		}else{
			accordian.children().children(':last-child').hide();
			}
		
		   var findh3 = accordian.children().children(':first-child');
			var finddiv = accordian.children().children(':last-child');
			
		//findh3.css('color','#555');
		
		findh3.click(function(){
			
			if($(this).next(finddiv).is(':hidden') == true){
			findh3.next().slideUp();
			kid.removeClass(settings.activeClass);
			}
			
			if($(this).next(finddiv).is(':hidden') == true){
				$(this).next(finddiv).slideDown(function(){
					settings.slideDownCallback();
					});
				$(this).parent().addClass(settings.activeClass);
				
			}else{
				$(this).next(finddiv).slideUp();
				$(this).parent().removeClass(settings.activeClass);
				settings.slideUpCallback();
				}
			
				
			
		})
		
    });
	
     }
})(jQuery);
//accordian plagin end

