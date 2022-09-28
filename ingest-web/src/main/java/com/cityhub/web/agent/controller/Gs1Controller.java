package com.cityhub.web.agent.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.cityhub.utils.JsonUtil;
import com.cityhub.web.agent.service.Gs1Service;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings({"rawtypes","unchecked"})
@Slf4j
@RestController
public class Gs1Controller {

	public static final String FIELD_HEX = (char) 0xD1 + "";

	@Autowired
	Gs1Service gs;

	@GetMapping({ "/gs1List" })
	public ModelAndView gs1ListView() {
		log.debug("----- Gs1Controller.gs1ListView() -----");
		return new ModelAndView("gs1List");
	}

	@GetMapping("/gs1ListData")
	  public ResponseEntity<List<Map>> gs1ListData(){
		  log.debug("----- Gs1troller.gs1ListData() -----");
		  List<Map> result = new ArrayList<>();
		  try {
			 result = gs.getGs1ListData();
		  } catch (Exception e) {
			  log.error("Exception : " + ExceptionUtils.getStackTrace(e));
		  }
		  return new ResponseEntity<>(result, HttpStatus.OK);
	  }

	@GetMapping({ "/gs1Model" })
	public ModelAndView gs1ModelView() throws Exception {
		log.debug("----- Gs1Controller.gs1ModelView() -----");
		return new ModelAndView("gs1Model");
	}

	@GetMapping({ "/gs1ModelData" })
	public ResponseEntity<List<Map>> gs1ModelData() throws Exception {
		log.debug("----- Gs1Controller.gs1ModelData() -----");
		List<Map> result = new ArrayList<>();
	    try {
	      result = gs.getGs1ModelList();
	    } catch (Exception e) {
	      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
	    }
	    return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@GetMapping("/gs1ModelDetail")
	public ModelAndView gs1ModelDetail() {
		log.debug("----- Gs1Controller.gs1ModelDetail() -----");
		return new ModelAndView("gs1ModelDetail");
	}

	@GetMapping("/gs1ModelDetailData/{gs1_code_id}")
	public ModelAndView gs1ModelDetailData(@PathVariable("gs1_code_id") String gs1_code_id) {
		log.debug("----- Gs1Controller.gs1ModelDetailData() -----");
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("gs1ModelDetailData");
		modelAndView.addObject("gs1_code_id", gs1_code_id);

		return modelAndView;
	}

	@GetMapping("/getgs1codecategory/{gs1_code_id}")
	public ResponseEntity<List<Map>> getgs1codecategory(@PathVariable("gs1_code_id") String gs1_code_id) throws Exception {
		log.debug("----- Gs1Controller.getgs1codecategory() -----");
		List<Map> result = new ArrayList<>();
		try {
			result = gs.getgs1codecategory(gs1_code_id);
		} catch (Exception e) {
			log.error("Exception : " + ExceptionUtils.getStackTrace(e));
		}
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@GetMapping("/gs1Detail/{id}")
	public ModelAndView gs1DetailView(@PathVariable("id") String id) throws Exception {
		log.debug("----- Gs1Controller.gs1DetailView() -----");
		ModelAndView modelAndView = new ModelAndView();

		List<Map> list = new ArrayList<>();
		list = gs.getGs1Code(id);
		log.debug("result =" + list);

		modelAndView.addObject("list", list);
		modelAndView.setViewName("gs1Detail");
		modelAndView.addObject("id", id);
		return modelAndView;
	}

	@GetMapping("/gs1DetailData/{id}")
	public ResponseEntity<List<Map>> gs1DetailData(@PathVariable("id") String id) throws Exception {
		log.debug("----- Gs1Controller.gs1DetailData() -----");
		List<Map> result = new ArrayList<>();
	    try {
	      result = gs.getGs1Code(id);
	    } catch (Exception e) {
	      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
	    }
	    return new ResponseEntity<>(result, HttpStatus.OK);
	}

	  @PostMapping("/gs1TypeSave")
	  public ResponseEntity<Map> commTypeSave(@RequestBody Map param) {
	    log.debug("----- Gs1troller.Gs1TypeSave() -----");
	    try {
	    	if(param.get("gs1_type_data") == null)
	    		return new ResponseEntity<>(JsonUtil.toMap(HttpStatus.NOT_FOUND), HttpStatus.NOT_FOUND);
	    	else
	    		gs.insertGs1Type(param);
	    } catch (Exception e) {
	    	log.error("Exception : " + ExceptionUtils.getStackTrace(e));
	    }
	    return new ResponseEntity<>(JsonUtil.toMap(HttpStatus.CREATED), HttpStatus.CREATED);
	  }

	  @PostMapping("/gs1modelSave")
	  public ResponseEntity<Map> gs1modelSave(@RequestBody Map param) {
	    log.debug("----- Gs1troller.gs1modelSave() -----");
	    try {
	    	if(param.get("gs1_type_data") == null)
	    		return new ResponseEntity<>(JsonUtil.toMap(HttpStatus.NOT_FOUND), HttpStatus.NOT_FOUND);
	    	else
	    		gs.insertGs1model(param);

	    } catch (Exception e) {
	      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
	    }
	    return new ResponseEntity<>(JsonUtil.toMap(HttpStatus.CREATED), HttpStatus.CREATED);
	  }

	  @PostMapping("/gs1modelUpdate")
	  public ResponseEntity<Map> gs1modelUpdate(@RequestBody Map param) {
	    log.debug("----- Gs1troller.gs1modelUpdate() -----");
	    try {
	      gs.UpdateGs1model(param);

	    } catch (Exception e) {
	      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
	    }
	    return new ResponseEntity<>(JsonUtil.toMap(HttpStatus.CREATED), HttpStatus.CREATED);
	  }

	  @PostMapping("/gs1ModelDelete")
	  public ResponseEntity<Map> gs1ModelDelete(@RequestBody Map param){
		  log.debug("----- Gs1troller.Gs1ModelDelete() -----");
		  try {
			  gs.deleteGs1Model(param);
		  } catch (Exception e) {
			  log.error("Exception : " + ExceptionUtils.getStackTrace(e));
		  }
		  return new ResponseEntity<>(JsonUtil.toMap(HttpStatus.CREATED), HttpStatus.CREATED);
	  }

	  @PostMapping("/gs1ModelModify")
	  public ResponseEntity<Map> gs1ModelModify(@RequestBody Map param){
		  log.debug("----- Gs1troller.Gs1ModelModify() -----");
		  try {
			  gs.ModifyGs1Model(param);
		  } catch (Exception e) {
			  log.error("Exception : " + ExceptionUtils.getStackTrace(e));
		  }
		  return new ResponseEntity<>(JsonUtil.toMap(HttpStatus.CREATED), HttpStatus.CREATED);
	  }

	  @GetMapping("/gs1category")
	  public ResponseEntity<List<Map>> gs1category(){
		  log.debug("----- Gs1troller.gs1category() -----");
		  List<Map> result = new ArrayList<>();
		  try {
			 result = gs.getGs1Categorym();
		  } catch (Exception e) {
			  log.error("Exception : " + ExceptionUtils.getStackTrace(e));
		  }
		  return new ResponseEntity<>(result, HttpStatus.OK);
	  }

	  @GetMapping("/gs1category/{m}")
	  public ResponseEntity<List<Map>> gs1category_c(@PathVariable("m") String m){
		  log.debug("----- Gs1troller.gs1category_c() -----");
		  List<Map> result = new ArrayList<>();
		  try {
			 result = gs.getGs1Categorym(m);
		  } catch (Exception e) {
			  log.error("Exception : " + ExceptionUtils.getStackTrace(e));
		  }
		  return new ResponseEntity<>(result, HttpStatus.OK);
	  }

	  @GetMapping("/gs1category_s/{c_nm}")
	  public ResponseEntity<List<Map>> gs1category_s(@PathVariable("c_nm") String c_nm){
		  log.debug("----- Gs1troller.gs1category_s() -----");
		  List<Map> result = new ArrayList<>();
		  try {
			 result = gs.getGs1Categorym_s(c_nm);
		  } catch (Exception e) {
			  log.error("Exception : " + ExceptionUtils.getStackTrace(e));
		  }
		  return new ResponseEntity<>(result, HttpStatus.OK);
	  }

	  @GetMapping("/getcommgs1code")
	  public ResponseEntity<List<Map>> getcommgs1code(){		//gs1시스템 코드에서 공통코드 리스트로 받아옴
		  log.debug("----- Gs1troller.getcommgs1code() -----");
		  List<Map> result = new ArrayList<>();

		  try {
			  result = gs.getcommgs1code();
		  } catch (Exception e) {
			  log.error("Exception : " + ExceptionUtils.getStackTrace(e));
		  }
		  return new ResponseEntity<>(result, HttpStatus.OK);
	  }

	  @GetMapping("/getgcodenm/{gs_code}")
	  public ResponseEntity<List<Map>> getgcodenm(@PathVariable("gs_code") String gs_code){		//gcode, g_code_nm 리스트 받아옴(코드와 코드명)
		  log.debug("----- Gs1troller.getgcodenm() -----");
		  List<Map> result = new ArrayList<>();
		  List<Map> code_nm = new ArrayList<>();
		  result.addAll(code_nm);
		  try {
			  code_nm = gs.findcodenm(gs_code);
			  result = gs.getgcodenm(gs_code);
			  result.addAll(code_nm);
		  } catch (Exception e) {
			  log.error("Exception : " + ExceptionUtils.getStackTrace(e));
		  }
		  return new ResponseEntity<>(result, HttpStatus.OK);
	  }

}
