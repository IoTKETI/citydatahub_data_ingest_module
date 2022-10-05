String.prototype.toCapitalize = function() {
  return this.charAt(0).toUpperCase() + this.slice(1);
};
String.prototype.IsJson = function() {
  try {
    var json = JSON.parse(this);
    return (typeof json === 'object');
  } catch (e) {
    return false;
  }
};



var RowData = (function ( ) {
  function RowData(data) {
    this.data = data;
  }
  /*
   * 테이블의 행별 아이디 변경 및 값을 세팅합니다.
   */
  RowData.prototype.setChangeIdValue = function() {
    var idx = arguments[0];
    var id = arguments[1];
    var value = arguments[2];
    var href = "";
    if(arguments.length == 4) href = arguments[3];
    
    this.data.find("#"+id).prop("id" , id + idx );
    var obj = this.getObj(id + idx);
    if(obj.prop('tagName') == "INPUT") {
      if (obj.prop('type') == "checkbox") {
        obj.filter("[value='"+value+"']").prop("checked", true);
        obj.val(value);
      } else if(obj.prop('type') == "radio") {
        obj.filter("[value='"+value+"']").prop("checked", true);
        obj.val(value);
      } else {
        obj.val(value);
      }
    } else if(obj.prop('tagName') == "SELECT") {
      obj.val(value).prop("selected", true);
    }else {
      obj.html(value);
      if(obj.prop('tagName') == "A") {
        var prefix = obj.prop("href");
        if(href  != "") {
          obj.prop("href" , href);  
        } else {
          obj.prop("href" , prefix + value);
        }
      }
    }
  };
  RowData.prototype.setChangeId = function() {
    var idx = arguments[0];
    var id = arguments[1];
    var obj = this.getObj(id);
    if(obj.length > 0  && obj.prop('type') == "checkbox") { 
      obj.prop("id", id + idx );
      if(this.data.find("label[for="+id+"]").length > 0 ) {
        this.data.find("label[for="+id+"]").prop("for", id + idx );  
      }
    } else {
      obj.prop("id" , id + idx );  
    } 
  };
  RowData.prototype.getObj = function(id) {
    if (this.data.find("#"+id).length > 0) {
      return this.data.find("#"+id);
    } else if (this.data.find("[name="+id+"]").length > 0) {
      return this.data.find("[name="+id+"]");
    }
  };
  RowData.prototype.setValue = function() {
    var id = arguments[0];
    var value = arguments[1];
    var obj = this.getObj(id);
    if (obj.prop('tagName') == "INPUT" ) {
      if (obj.prop('type') == "checkbox") {
        obj.filter("[value='"+value+"']").prop("checked", true);
      } else if(obj.prop('type') == "radio") {
        obj.filter("[value='"+value+"']").prop("checked", true);
      } else {
        obj.val(value);
      }
    } else if(obj.prop('tagName') == "SELECT") {
      obj.val(value).prop("selected", true);
    } else if(obj.prop('tagName') == "TEXTAREA") {      
      return obj.val(value);      
    }else {
      obj.html(value);
    }
  };
  RowData.prototype.getValue = function() {
    var id = arguments[0] ? arguments[0] : null;
    var obj = this.getObj(id);
    if(obj.prop('tagName') == "INPUT") {
      if (obj.prop('type') == "checkbox") {
        return obj.filter(":checked").val();
      } else if(obj.prop('type') == "radio") {
        return obj.filter(":checked").val();
      } else {
        return obj.val();
      }
    } else if(obj.prop('tagName') == "SELECT") {
      return obj.children("option:selected").val();
    } else if(obj.prop('tagName') == "TEXTAREA") {      
      return obj.val();
    }else {
      return obj.html();
    }
  };
  RowData.prototype.toObject = function() {
    return this.data;
  };
  return RowData;
})();



var cmUtl = {};

/**
* ajax 서버 호출.
* @param   type : POST, GET, PUT, DELETE
* @param   url : url or param
* @param   data : request JSON object.
* @param   _cbFuncName : callback Function

* @example var req_str  = {searchParam: {clCode: "111", codeNm: "2222"}};
      cmUtl.exeAjax( "POST", "/adapterDetail", req_str, "_cbAfterFunc");
*/
cmUtl.exeAjax = function( type, url, data, _cbFuncName) {
  var resObj = "";
  if(type == "GET") {
    $.ajax({
      type : type,
      contentType : "application/json",
      url : url,
      dataType : 'json',
      param : data,
      cache : false,
      timeout : 600000,
      success : function(data) {
        resObj = data;
        if ( _cbFuncName != "" ) {
          try { eval( _cbFuncName+"(data)" ); } catch(e){}
        }
      },
      error : function(e) {
        console.log (e.responseText );
        if ( _cbFuncName != "" ) {
          var data = "";
          try {
            data = JSON.parse(e.responseText);;
          } catch (e) {
            data = e.responseText;
          }
          try { eval( _cbFuncName+"(data )" ); } catch(e){}
        }
      }
    });  
  } else {
    $.ajax({
      type : type,
      contentType : "application/json",
      url : url,
      dataType : 'json',
      data : data,
      cache : false,
      timeout : 600000,
      success : function(data) {
        resObj = data;
        if ( _cbFuncName != "" ) {
          try { eval( _cbFuncName+"(data)" ); } catch(e){}
        }
      },
      error : function(e) {
        console.log (e.responseText );
        if ( _cbFuncName != "" ) {
          var data = "";
          try {
            data = JSON.parse(e.responseText);;
          } catch (e) {
            data = e.responseText;
          }
          try { eval( _cbFuncName+"(data )" ); } catch(e){}
        }
      }
    });
  } 
  return resObj;
};


cmUtl.exeAjaxDynamic = function( type, url,param, data, _cbFuncName) {
  var resObj = "";
  $.ajax({
    type : type,
    contentType : "application/json",
    url : url,
    dataType : 'json',
    param : param,
    data : data,
    cache : false,
    timeout : 600000,
    success : function(data) {
      resObj = data;
      if ( _cbFuncName != "" ) {
        try { eval( _cbFuncName+"(data)" ); } catch(e){}
      }
    },
    error : function(e) {
      if ( _cbFuncName != "" ) {
        var data = "";
        try {
          data = JSON.parse(e.responseText);;
        } catch (e) {
          data = e.responseText;
        }
        try { eval( _cbFuncName+"(data )" ); } catch(e){}
      }
    }
  });
  return resObj;
};

cmUtl.showEmptyLine = function(objId, isShow) {
  if (isShow) {
    var emptyInsEtc = '<tr class="emptyInsEtc" style="padding-left: 0px;text-align: center;background-color: ffffff;">';
    if (arguments.length == 3) {
      emptyInsEtc += '<td>' + arguments[2] + '</td>';
    } else {
      emptyInsEtc += '<td>데이터가 존재하지 않습니다.</td>';
    }
    emptyInsEtc += '</tr>';
    
    
    var colspan = $('#'+objId).find('thead > tr').children().length;
    
    if ($('#'+objId).find('tfoot').length == 0 ) {
      $('<tfoot>').appendTo("#"+ objId);
    }
    $('#'+objId).find('tfoot > tr').empty();
    $('#'+objId).find('tfoot:last').append(emptyInsEtc);
    $('#'+objId).find('tfoot > tr').children().attr("colspan",colspan);
  } else {
    $('#'+objId).find('.emptyInsEtc').closest('tr').remove();
  }
};



/*
 * cmUtl.tableAutoMerge("#itemList", 0);
 */
cmUtl.tableAutoMerge = function(tableId, rowSpanIndex) {
  var rowspan_td = false;
  var rowspan_column_name = false;
  var rowspan_count = 0;
  var rows = $('tr', table);
  $.each(rows, function () {
    var This = $('td', this)[rowSpanIndex];
    var text = $(This).text();
    if (rowspan_td == false) {
      rowspan_td = This;
      rowspan_column_name = text;
      rowspan_count = 1;
    } else if (rowspan_column_name != text) {
      $(rowspan_td).attr('rowSpan', rowspan_count);
      rowspan_td = This;
      rowspan_column_name = text;
      rowspan_count = 1;
    } else {
      $(This).remove();
      rowspan_count++;
    }
  }); // 반복 종료 후 마지막 rowspan 적용 
  $(rowspan_td).attr('rowSpan', rowspan_count); 
};


cmUtl.openLayerPopup = function(url, param, title) {
  param.title = title;
  if (arguments.length == 4) {
    param._cbFunc = arguments[3];
  }
  $.post(url, param, function(data, status) {
    $( "#divLayerPopup" ).append(data);
  });
};


/* 
 * 같은 값이 있는 열을 병합함
 * 사용법 : $('#테이블 ID').rowspan(0);
 */
$.fn.rowspan = function (colIdx, isStats) {
  return this.each(function () {
    var that;
    $('tr', this).each(function (row) {
      $('td:eq(' + colIdx + ')', this).filter(':visible').each(function (col) {

        if ($(this).html() == $(that).html() &&
          (!isStats ||
            isStats && $(this).prev().html() == $(that).prev().html()
          )
        ) {
          rowspan = $(that).attr("rowspan") || 1;
          rowspan = Number(rowspan) + 1;

          $(that).attr("rowspan", rowspan);
          // do your action for the colspan cell here            
          $(this).hide();
          //$(this).remove(); 
          // do your action for the old cell here
        } else {
          that = this;
        }
        // set the that if not already set
        that = (that == null) ? this : that;
      });
    });
  });
};


/* 
 * 같은 값이 있는 행을 병합함
 * 사용법 : $('#테이블 ID').colspan (0);
 */
$.fn.colspan = function (rowIdx) {
  return this.each(function () {
    var that;
    $('tr', this).filter(":eq(" + rowIdx + ")").each(function (row) {
      $(this).find('th').filter(':visible').each(function (col) {
        if ($(this).html() == $(that).html()) {
          colspan = $(that).attr("colSpan") || 1;
          colspan = Number(colspan) + 1;

          $(that).attr("colSpan", colspan);
          $(this).hide(); // .remove();
        } else {
          that = this;
        }
        // set the that if not already set
        that = (that == null) ? this : that;

      });
    });
  });
}

var tableEmptyLine = '<tr class="tableEmptyLine" style="padding-left: 0px;text-align: center;background-color: ffffff;">';
tableEmptyLine += '<td>데이터가 존재하지 않습니다.</td>';
tableEmptyLine += '</tr>';

$.fn.appendBody = function(data) {
  this.find('tbody').append(data);
};

$.fn.clearBody = function() {
  this.find("tbody").empty();
};

$.fn.procEmptyLine = function() {
  if(this.find("tbody > tr").length == 0 ) {
    var colspan = this.find('thead > tr').children().length;
    if (this.find('tfoot').length == 0 ) {
      $('<tfoot>').appendTo(this);
    }
    this.find('tfoot:last').append(tableEmptyLine);
    this.find('tfoot > tr').children().attr("colspan",colspan);
  } else {
    this.find('.tableEmptyLine').closest('tr').remove();
  }
};

$.fn.getRow = function(idx) {
  return this.find("tbody > tr").eq(idx);
};

$.fn.getLength = function() {
  return this.find("tbody > tr").length;
};


