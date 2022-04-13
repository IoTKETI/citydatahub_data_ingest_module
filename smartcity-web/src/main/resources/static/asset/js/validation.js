
  $(function() {
    $(document).on("keyup", "input:text[numberOnly]", function() {
      $(this).val($(this).val().replace(/[^0-9]/gi, ""));
    });
    $(document).on("keyup", "input:text[uppercase]", function() {
      $(this).val($(this).val().toUpperCase());
    });
    
    $(document).on("keyup", "input:text[engNum]", function() {
      $(this).val($(this).val().replace(/[^a-zA-Z0-9-_]/g, ""));
    });
    
    $(document).on("keyup", "input:text[hanNum]", function() {
      $(this).val($(this).val().replace(/[^ㄱ-ㅎㅏ-ㅣ가-힣0-9-_]/g, ""));
    });
    
    $(document).on("keyup", "input:text[hanOnly]", function() {
      $(this).val($(this).val().replace(/[^ㄱ-ㅎㅏ-ㅣ가-힣]/g, ""));
    });
    
  });

  function validation(blockId){
    var rtn = true;
    var $body = $("#"+blockId);
    
    $body.find("input,select,textarea").each(function(i) {
      $obj = jQuery(this);
      
      if($obj.prop("required")  ) {
        
        //alert($obj.attr("type") + $obj.prop("tagName").toLowerCase())
        if($obj.prop("tagName").toLowerCase() == "input") {
          if($obj.attr("type").toLowerCase() == "radio") {
            if ($obj.filter(":checked").length < 1) {
              alert(getLabelName($obj)+"은(는) 필수 선택입니다.");
              rtn = false;
              return false;
            }  
          } 
          if($obj.attr("type").toLowerCase() == "checkbox") {
            if ($obj.filter(":checked").length < 1  ) {
              alert(getLabelName($obj)+"은(는) 적어도 1개 이상은 선택해 주세요.");
              rtn = false;
              return false;
            }    
          }
          if($obj.attr("type").toLowerCase() == "text") {
            if($obj.val() == "" || $obj.val() == undefined) {
              alert(getLabelName($obj)+"은(는) 필수입력입니다.");
              $obj.focus(); 
              rtn = false;
              return false;
            } 
          }
          
        } else if($obj.prop("tagName").toLowerCase() == "select") {
          // $obj.children("option:selected").val() 
          if ($obj.val() == "" || $obj.val() == undefined ) {
            alert(getLabelName($obj)+"은(는) 필수 선택입니다.");
            $obj.focus(); 
            rtn = false;
            return false;
          }  
        } else if($obj.prop("tagName").toLowerCase() == "textarea") {
          if($obj.val().length == "" || $obj.val() == undefined) {
            alert(getLabelName($obj)+"은(는) 필수입력입니다.");
            $obj.focus(); 
            rtn = false;
            return false;
          }
        }
      }
    });
    if(rtn == false) return rtn;
    
    
    return rtn;
  }
  

  function getLabelName(obj) {
    var msg = "";
    if(obj.is("[label]")  ) {
      msg = obj.attr("label");
    } else if (obj.parent().is("[label]") ){
      msg = obj.parent().attr("label");
    } else if(obj.is("[name]")  ) {
      msg = obj.attr("name");
    } else if(obj.is("[id]")  ) { 
      msg = obj.attr("id");
    } 
    return msg;
  }
  
  