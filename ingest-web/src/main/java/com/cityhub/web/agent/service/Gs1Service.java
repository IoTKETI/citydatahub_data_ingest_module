package com.cityhub.web.agent.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cityhub.web.agent.mapper.Gs1Mapper;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings({ "rawtypes", "unchecked" })
@Slf4j
@Service
public class Gs1Service {

  @Autowired
  Gs1Mapper mapper;

  public List<Map> getGs1Code(String gs1code) throws Exception {
    return mapper.getGs1Code(gs1code);
  }

  @Transactional
  public Map insertGs1Type(Map param) throws Exception {

    List<?> list = new ArrayList<>();
    list = (List) param.get("gs1_type_data");
    if (list != null && list.size() > 0) {
      for (int i = 0; i < list.size(); i++) {
        Map obj = (Map) list.get(i);
        String code = (String) obj.get("code");

        int flag = isContained(obj);

        if (flag == 0) {
          mapper.insertGs1Code(obj);
        } else {
          mapper.updateGs1Code(obj);
        }
      }
    }
    return param;
  }

  @Transactional
  public Map insertGs1model(Map param) throws Exception {
    // TODO=>중복검사
    List<?> list = new ArrayList<>();
    list = (List) param.get("gs1_type_data");
    if (list != null && list.size() > 0) {
      for (int i = 0; i < list.size(); i++) {
        Map obj = (Map) list.get(i);
        int flag = isContainedModel(obj);

        if (flag == 0) {
          mapper.insertGs1model(obj);
        } else {
          mapper.updateGs1Model(obj);
        }
      }
    }

    return param;
  }

  @Transactional
  public Map UpdateGs1model(Map param) throws Exception {
    List<?> list = new ArrayList<>();
    list = (List) param.get("gs1_type_data");
    if (list != null && list.size() > 0) {
      for (int i = 0; i < list.size(); i++) {
        Map obj = (Map) list.get(i);
        mapper.UpdateGs1model(obj);
      }
    }

    return param;
  }

  public Integer isContainedModel(Map param) throws Exception {
    int flag = 0;
    flag = mapper.isContainedModel(param);
    return flag;
  }

  public Integer isContained(Map param) throws Exception {
    int flag = 0;
    flag = mapper.isContained(param);
    return flag;
  }

  public List<Map> getGs1ModelList() throws Exception {
    List<Map> list = mapper.getGs1ModelList();
    return mapper.getGs1ModelList();
  }

  public List<Map> getgs1codecategory(String gs1_code_id) throws Exception {
    return mapper.getgs1codecategory(gs1_code_id);
  }

  @Transactional
  public Map deleteGs1Model(Map param) throws Exception {

    List<?> list = new ArrayList<>();
    list = (List) param.get("gs1_model_data");
    if (list != null && list.size() > 0) {
      for (int i = 0; i < list.size(); i++) {
        Map obj = (Map) list.get(i);
        mapper.deleteGs1model(obj);
      }
    }
    return param;
  }

  public List<Map> getGs1ListData() throws Exception {
    List<Map> list = mapper.getGs1ListData();
    return list;
  }

  public Map ModifyGs1Model(Map param) throws Exception {
    List<?> list = new ArrayList<>();
    list = (List) param.get("gs1_model_data");
    if (list != null && list.size() > 0) {
      for (int i = 0; i < list.size(); i++) {
        Map obj = (Map) list.get(i);
        mapper.ModifyGs1Model(obj);
      }
    }
    return param;
  }

  public List<Map> getGs1Categorym() throws Exception {
    List<Map> list = mapper.getGs1Categorym();
    return mapper.getGs1Categorym();
  }

  public List<Map> getGs1Categorym(String m) throws Exception {
    m += "_";
    List<Map> list = mapper.getGs1Categorym_c(m);
    return mapper.getGs1Categorym_c(m);
  }

  public List<Map> getGs1Categorym_s(String c_nm) throws Exception {
    c_nm += "___";
    return mapper.getGs1Categorym_s(c_nm);
  }

  public List<Map> getcommgs1code() throws Exception {
    return mapper.getcommgs1code();
  }

  public List<Map> getgcodenm(String gs_code) throws Exception {

    String t = mapper.getgcodenm(gs_code).toString();
    return mapper.getgcodenm(gs_code);
  }

  public List<Map> findcodenm(String gs_code) throws Exception {
    return mapper.findcodenm(gs_code);
  }
}
