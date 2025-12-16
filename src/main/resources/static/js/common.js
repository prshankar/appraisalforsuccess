
$(document).ready(function () {
    $('[data-toggle="tooltip"]').tooltip();
});
// Sigin Form
 $(document).ready(function () {
            //Initialize tooltips
            $('.nav-tabs > li a[title]').tooltip();

            //Wizard
            $('a[data-toggle="tab"]').on('show.bs.tab', function (e) {

                var $target = $(e.target);

                if ($target.parent().hasClass('disabled')) {
                    return false;
                }
            });

            $(".next-step").click(function (e) {

                var $active = $('.wizard .nav-tabs li.active');
                $active.next().removeClass('disabled');
                nextTab($active);

            });
            $(".prev-step").click(function (e) {

                var $active = $('.wizard .nav-tabs li.active');
                prevTab($active);

            });
        });

        function nextTab(elem) {
            $(elem).next().find('a[data-toggle="tab"]').click();
        }
        function prevTab(elem) {
            $(elem).prev().find('a[data-toggle="tab"]').click();
        }


//DOB
$(document).ready(function () {
	$(".phone input").intlTelInput({
  utilsScript: "https://cdnjs.cloudflare.com/ajax/libs/intl-tel-input/8.4.6/js/utils.js"
});
    $('#input-group').bootstrapBirthday({
        widget: {
            wrapper: {
                tag: 'div',
                class: 'input-group'
            },
            wrapperYear: {
                use: true,
                tag: 'span',
                class: 'input-group-addon'
            },
            wrapperMonth: {
                use: true,
                tag: 'span',
                class: 'input-group-addon'
            },
            wrapperDay: {
                use: true,
                tag: 'span',
                class: 'input-group-addon'
            },
            selectYear: {
                name: 'birthday[year]',
                class: 'form-control input-sm'
            },
            selectMonth: {
                name: 'birthday[month]',
                class: 'form-control input-sm'
            },
            selectDay: {
                name: 'birthday[day]',
                class: 'form-control input-sm'
            }
        }
    });

    $('#row-col').bootstrapBirthday({
        widget: {
            wrapper: {
                tag: 'div',
                class: 'row'
            },
            wrapperYear: {
                use: true,
                tag: 'div',
                class: 'col-sm-4'
            },
            wrapperMonth: {
                use: true,
                tag: 'div',
                class: 'col-sm-4'
            },
            wrapperDay: {
                use: true,
                tag: 'div',
                class: 'col-sm-4'
            },
            selectYear: {
                name: 'birthday[year]',
                class: 'form-control'
            },
            selectMonth: {
                name: 'birthday[month]',
                class: 'form-control'
            },
            selectDay: {
                name: 'birthday[day]',
                class: 'form-control'
            }
        }
    });

    $('#simple').bootstrapBirthday({
        widget: {
            wrapper: {
                tag: 'div',
                class: ''
            },
            wrapperYear: {
                use: false
            },
            wrapperMonth: {
                use: false
            },
            wrapperDay: {
                use: false
            },
            selectYear: {
                name: 'birthday[year]',
                class: 'form-control'
            },
            selectMonth: {
                name: 'birthday[month]',
                class: 'form-control'
            },
            selectDay: {
                name: 'birthday[day]',
                class: 'form-control'
            }
        }
    });
	

});


//text

function noPreview() {
  $('#image-preview-div').css("display", "none");
  $('#preview-img').attr('src', 'noimage');
  $('upload-button').attr('disabled', '');
}

function selectImage(e) {
  $('#file').css("color", "green");
  $('#image-preview-div').css("display", "block");
  $('#preview-img').attr('src', e.target.result);
  $('#preview-img').css('max-width', '550px');
}

$(document).ready(function (e) {

  var maxsize = 500 * 1024; // 500 KB

  $('#max-size').html((maxsize/1024).toFixed(2));

  $('#upload-image-form').on('submit', function(e) {

    e.preventDefault();

    $('#message').empty();
    $('#loading').show();

    $.ajax({
      url: "upload-image.php",
      type: "POST",
      data: new FormData(this),
      contentType: false,
      cache: false,
      processData: false,
      success: function(data)
      {
        $('#loading').hide();
        $('#message').html(data);
      }
    });

  });

  $('#file').change(function() {

    $('#message').empty();

    var file = this.files[0];
    var match = ["image/jpeg", "image/png", "image/jpg"];

    if ( !( (file.type == match[0]) || (file.type == match[1]) || (file.type == match[2]) ) )
    {
      noPreview();

      $('#message').html('<div class="alert alert-warning" role="alert">Unvalid image format. Allowed formats: JPG, JPEG, PNG.</div>');

      return false;
    }

    if ( file.size > maxsize )
    {
      noPreview();

      $('#message').html('<div class=\"alert alert-danger\" role=\"alert\">The size of image you are attempting to upload is ' + (file.size/1024).toFixed(2) + ' KB, maximum size allowed is ' + (maxsize/1024).toFixed(2) + ' KB</div>');

      return false;
    }

    $('#upload-button').removeAttr("disabled");

    var reader = new FileReader();
    reader.onload = selectImage;
    reader.readAsDataURL(this.files[0]);

  });

// search add

});

// Speach Developement team list

var closebtns = document.getElementsByClassName("close");
var i;

for (i = 0; i < closebtns.length; i++) {
  closebtns[i].addEventListener("click", function() {
    this.parentElement.style.display = 'none';
  });
}


// Images Upload
var textPresetVal = new Choices('#choices-text-preset-values',
	{
		removeItemButton: true,
});

function preview_images() 
{
 var total_file=document.getElementById("images").files.length;
 for(var i=0;i<total_file;i++)
 {
  $('#image_preview').append("<div class='col-md-3'><img class='img-responsive' src='"+URL.createObjectURL(event.target.files[i])+"'></div>");
 }
}

// Test Box Add data
 $( "#tags, #tags1, #tags2" ).masterblaster( {
	animate: true
  } );
  
 // preloader
 $(window).on( 'load', function() { // makes sure the whole site is loaded
    $('#status').fadeOut(); // will first fade out the loading animation
    $('#preloader').delay(350).fadeOut('slow'); // will fade out the white DIV that covers the website.
    $('body').delay(350).css({'overflow': 'visible'});
})
	  
