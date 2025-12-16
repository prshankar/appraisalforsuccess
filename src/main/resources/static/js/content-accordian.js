// JavaScript Document
$(function(){

if($('.awardsAccordian').length>0){
$('.awardsAccordian').krisAccordian({
	activeClass:"awardActive",
	slideDownCallback:function(){},
	slideUpCallback:function(){},
	initiallyContShow:true,
	int:function(){
	//alert('a');
	}
	});
}
if($('.faqAccordian').length>0){
$('.faqAccordian').krisAccordian({
	activeClass:"faqActive",
	slideDownCallback:function(){},
	slideUpCallback:function(){},
	initiallyContShow:true,
	int:function(){
	//alert('a');
	}
	});
}
})/* ready function end */
