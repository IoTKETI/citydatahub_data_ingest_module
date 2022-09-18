package com.cityhub.web.agent.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@SuppressWarnings("rawtypes")
@Mapper
public interface Gs1Mapper {
	
	List<Map> getGs1Code(String gs1code);
	List<Map> getGs1ModelList();
	List<Map> getGs1ListData();
	List<Map> getGs1Categorym();
	List<Map> getGs1Categorym_c(String m);
	List<Map> getGs1Categorym_s(String c_nm);
	List<Map> getgs1codecategory(String gs1_code_id);
	List<Map> getcommgs1code();
	List<Map> getgcodenm(String gs_code);
	List<Map> findcodenm(String gs_code);
	
	int isContained(Map param);
	int isContainedModel(Map param);
	
	int insertGs1Code(Map param);
	int updateGs1Code(Map param);
	int ModifyGs1Model(Map param);
	int updateGs1Model(Map param);
	int UpdateGs1model(Map param);
	
	int insertGs1model(Map param);
	int deleteGs1model(Map param);
}
