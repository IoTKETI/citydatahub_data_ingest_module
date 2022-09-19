/**
 *
 * Copyright 2021 PINE C&I CO., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cityhub.web.agent.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.message.BasicHeader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.cityhub.environment.Constants;
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.HttpResponse;
import com.cityhub.utils.JsonUtil;
import com.cityhub.utils.UrlUtil;
import com.cityhub.web.agent.service.MainService;
import com.cityhub.web.config.ConfigEnv;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings({ "rawtypes", "unchecked" })
@Slf4j
@RestController
public class MainController {

	public static final String FIELD_HEX = (char) 0xD1 + "";

	@Autowired
	MainService svc;

	@Autowired
	ConfigEnv configEnv;

	////////////////////////////////////////////////////////////////////////////

  @Value("${ingest.interfaceApiUrl}")
  public String interfaceApiUrl;


	@Value("${ingest.yn:N}")
  public String ingestYn;

	private ObjectMapper objectMapper;

	@PostMapping("/HealthMeasurement/{path}")
	public ResponseEntity<String> HealthMeasurement(@PathVariable("path") String path, @RequestBody Map param,
			HttpServletResponse response) {

		this.objectMapper = new ObjectMapper();
		this.objectMapper.setSerializationInclusion(Include.NON_NULL);
		this.objectMapper.setDateFormat(new SimpleDateFormat(Constants.CONTENT_DATE_FORMAT));
		this.objectMapper.setTimeZone(TimeZone.getTimeZone(Constants.CONTENT_DATE_TIMEZONE));


		List<Map<String, Object>> rtnList = new LinkedList<>();
		String bodyStr = "";

		try {
			if (path.equals("VivaInnovation")) {

				Map<String, Object> tMap = new LinkedHashMap<>();
				tMapSet(tMap);
				log.info("param : " + param);
				log.info("tMap : " + tMap);

				if (param.containsKey("height")) {
					Map<String, Object> height = (Map) tMap.get("height");
					height.put("value", param.get("height"));
					height.put("observedAt", DateUtil.getTime());
				}else {
					tMap.remove("height");
				}

				if (param.containsKey("weight")) {
					Map<String, Object> weight = (Map) tMap.get("weight");
					weight.put("value", param.get("weight"));
					weight.put("observedAt", DateUtil.getTime());
				}else {
					tMap.remove("weight");
				}

				if (param.containsKey("fat_rate")) {
					Map<String, Object> fatRate = (Map) tMap.get("fatRate");
					fatRate.put("value", param.get("fat_rate"));
					fatRate.put("observedAt", DateUtil.getTime());
				}else {
					tMap.remove("fatRate");
				}

				if (param.containsKey("muscle")) {
					Map<String, Object> muscle = (Map) tMap.get("muscle");
					muscle.put("value", param.get("muscle"));
					muscle.put("observedAt", DateUtil.getTime());
				}else {
					tMap.remove("muscle");
				}

				if (param.containsKey("fat_level")) {
					Map<String, Object> fatLevel = (Map) tMap.get("fatLevel");
					fatLevel.put("value", param.get("fat_level"));
					fatLevel.put("observedAt", DateUtil.getTime());
				}else {
					tMap.remove("fatLevel");
				}

				if (param.containsKey("kcal")) {
					Map<String, Object> kcal = (Map) tMap.get("kcal");
					kcal.put("value", param.get("kcal"));
					kcal.put("observedAt", DateUtil.getTime());
				}else {
					tMap.remove("kcal");
				}

				if (param.containsKey("bone_vol")) {
					Map<String, Object> boneVol = (Map) tMap.get("boneVolume");
					boneVol.put("value", param.get("bone_vol"));
					boneVol.put("observedAt", DateUtil.getTime());
				}else {
					tMap.remove("boneVolume");
				}

				if (param.containsKey("water")) {
					Map<String, Object> water = (Map) tMap.get("water");
					water.put("value", param.get("water"));
					water.put("observedAt", DateUtil.getTime());
				}else {
					tMap.remove("water");
				}

				if (param.containsKey("water_rate")) {
					Map<String, Object> waterRate = (Map) tMap.get("waterRate");
					waterRate.put("value", param.get("water_rate"));
					waterRate.put("observedAt", DateUtil.getTime());
				}else {
					tMap.remove("waterRate");
				}

				if (param.containsKey("bmi")) {
					Map<String, Object> bmi = (Map) tMap.get("bmi");
					bmi.put("value", param.get("bmi"));
					bmi.put("observedAt", DateUtil.getTime());
				}else {
					tMap.remove("bmi");
				}

				if (param.containsKey("fat")) {
					Map<String, Object> fat = (Map) tMap.get("fat");
					fat.put("value", param.get("fat"));
					fat.put("observedAt", DateUtil.getTime());
				}else {
					tMap.remove("fat");
				}

				if (param.containsKey("abdominal_obesity_rate")) {
					Map<String, Object> abdominalOR = (Map) tMap.get("abdominalObesityRate");
					abdominalOR.put("value", param.get("abdominal_obesity_rate"));
					abdominalOR.put("observedAt", DateUtil.getTime());
				}else {
					tMap.remove("abdominalObesityRate");
				}

				if (param.containsKey("waist")) {
					Map<String, Object> waist = (Map) tMap.get("waist");
					waist.put("value", param.get("waist"));
					waist.put("observedAt", DateUtil.getTime());
				}else {
					tMap.remove("waist");
				}

				if (param.containsKey("weight_ctrl")) {
					Map<String, Object> weightCtrl = (Map) tMap.get("weightControl");
					weightCtrl.put("value", param.get("weight_ctrl"));
					weightCtrl.put("observedAt", DateUtil.getTime());
				}else {
					tMap.remove("weightControl");
				}

				if (param.containsKey("fat_ctrl")) {
					Map<String, Object> fatCtrl = (Map) tMap.get("fatControl");
					fatCtrl.put("value", param.get("fat_ctrl"));
					fatCtrl.put("observedAt", DateUtil.getTime());
				}else {
					tMap.remove("fatControl");
				}

				if (param.containsKey("muscle_ctrl")) {
					Map<String, Object> muscleCtrl = (Map) tMap.get("muscleControl");
					muscleCtrl.put("value", param.get("muscle_ctrl"));
					muscleCtrl.put("observedAt", DateUtil.getTime());
				}else {
					tMap.remove("muscleControl");
				}

				if (param.containsKey("physical_dev")) {
					Map<String, Object> physicalDev = (Map) tMap.get("physicalDevelopment");
					physicalDev.put("value", param.get("physical_dev"));
					physicalDev.put("observedAt", DateUtil.getTime());
				}else {
					tMap.remove("physicalDevelopment");
				}

				if (param.containsKey("max_blood_pressure")) {
					Map<String, Object> maxBlood = (Map) tMap.get("maxBloodPressure");
					maxBlood.put("value", param.get("max_blood_pressure"));
					maxBlood.put("observedAt", DateUtil.getTime());
				}else {
					tMap.remove("maxBloodPressure");
				}

				if (param.containsKey("min_blood_pressure")) {
					Map<String, Object> minBlood = (Map) tMap.get("minBloodPressure");
					minBlood.put("value", param.get("min_blood_pressure"));
					minBlood.put("observedAt", DateUtil.getTime());
				}else {
					tMap.remove("minBloodPressure");
				}

				if (param.containsKey("pulse")) {
					Map<String, Object> pulse = (Map) tMap.get("pulse");
					pulse.put("value", param.get("pulse"));
					pulse.put("observedAt", DateUtil.getTime());
				}else {
					tMap.remove("pulse");
				}


				if (param.containsKey("before_blood_sugar")) {
					Map<String, Object> beforeBlood = (Map) tMap.get("beforeBloodSugar");
					beforeBlood.put("value", param.get("before_blood_sugar"));
					beforeBlood.put("observedAt", DateUtil.getTime());
				}else {
					tMap.remove("beforeBloodSugar");
				}


				if (param.containsKey("after_blood_suger")) {
					Map<String, Object> afterBlood = (Map) tMap.get("afterBloodSugar");
					afterBlood.put("value", param.get("after_blood_suger"));
					afterBlood.put("observedAt", DateUtil.getTime());
				}else {
					tMap.remove("afterBloodSugar");
				}


				if (param.containsKey("cholesterol")) {
					Map<String, Object> cholesterol = (Map) tMap.get("cholesterol");
					cholesterol.put("value", param.get("cholesterol"));
					cholesterol.put("observedAt", DateUtil.getTime());
				}else {
					tMap.remove("cholesterol");
				}


				if (param.containsKey("triglyceride")) {
					Map<String, Object> triglyceride = (Map) tMap.get("triglyceride");
					triglyceride.put("value", param.get("triglyceride"));
					triglyceride.put("observedAt", DateUtil.getTime());
				}else {
					tMap.remove("triglyceride");
				}


				if (param.containsKey("hdl_c")) {
					Map<String, Object> hdlC = (Map) tMap.get("hdlc");
					hdlC.put("value", param.get("hdl_c"));
					hdlC.put("observedAt", DateUtil.getTime());
				}else {
					tMap.remove("hdlc");
				}


				if (param.containsKey("ldl_c")) {
					Map<String, Object> ldlC = (Map) tMap.get("ldlc");
					ldlC.put("value", param.get("ldl_c"));
					ldlC.put("observedAt", DateUtil.getTime());
				}else {
					tMap.remove("ldlc");
				}


				if (param.containsKey("stress_score")) {
					Map<String, Object> stressScore = (Map) tMap.get("stressScore");
					stressScore.put("value", param.get("stress_score"));
					stressScore.put("observedAt", DateUtil.getTime());
				}else {
					tMap.remove("stressScore");
				}


				if (param.containsKey("physical_stress")) {
					Map<String, Object> physicalStress = (Map) tMap.get("physicalStress");
					physicalStress.put("value", param.get("physical_stress"));
					physicalStress.put("observedAt", DateUtil.getTime());
				}else {
					tMap.remove("physicalStress");
				}


				if (param.containsKey("mental_stress")) {
					Map<String, Object> mentalStress = (Map) tMap.get("mentalStress");
					mentalStress.put("value", param.get("mental_stress"));
					mentalStress.put("observedAt", DateUtil.getTime());
				}else {
					tMap.remove("mentalStress");
				}


				if (param.containsKey("stress_management")) {
					Map<String, Object> stressManagement = (Map) tMap.get("stressManagemenet");
					stressManagement.put("value", param.get("stress_management"));
					stressManagement.put("observedAt", DateUtil.getTime());
				}else {
					tMap.remove("stressManagemenet");
				}


				if (param.containsKey("vascular_phase")) {
					Map<String, Object> vascularPhase = (Map) tMap.get("vascularPhase");
					vascularPhase.put("value", param.get("vascular_phase"));
					vascularPhase.put("observedAt", DateUtil.getTime());
				}else {
					tMap.remove("vascularPhase");
				}


				if (param.containsKey("cardiac_output")) {
					Map<String, Object> cardiacOutput = (Map) tMap.get("cardiacOutput");
					cardiacOutput.put("value", param.get("cardiac_output"));
					cardiacOutput.put("observedAt", DateUtil.getTime());
				}else {
					tMap.remove("cardiacOutput");
				}


				if (param.containsKey("vascular_elasticity")) {
					Map<String, Object> vascularE = (Map) tMap.get("vascularElasticity");
					vascularE.put("value", param.get("vascular_elasticity"));
					vascularE.put("observedAt", DateUtil.getTime());
				}else {
					tMap.remove("vascularElasticity");
				}


				if (param.containsKey("remained_blood_volume")) {
					Map<String, Object> remainedBlood = (Map) tMap.get("remainedBloodVolume");
					remainedBlood.put("value", param.get("remained_blood_volume"));
					remainedBlood.put("observedAt", DateUtil.getTime());
				}else {
					tMap.remove("remainedBloodVolume");
				}


				Map<String, Object> dataProvider = (Map) tMap.get("dataProvider");
				dataProvider.put("value", "https://lg-electronics.adoc-solution.com");

				Map<String, Object> globalLocationNumber = (Map) tMap.get("globalLocationNumber");
				globalLocationNumber.put("value", "urn:epc:id:giai:880969104.143000");

				tMap.put("id", "urn:epc:id:giai:880969104.143000" + "." + param.get("uid"));

				tMap.put("type", "HealthMeasurement");

//				Map<String, Object> name = new LinkedHashMap<>();
//				name.put("value", "건강 측정");
//				name.put("type", "Property");
//				tMap.put("name", name);

				Map<String, Object> addrValue = (Map) ((Map) tMap.get("address")).get("value");
				addrValue.put("addressCountry", param.get("addressCountry"));
				addrValue.put("addressRegion", param.get("addressRegion"));
				addrValue.put("addressLocality", param.get("addressLocality"));
				addrValue.put("addressTown", param.get("addressTown"));
				addrValue.put("streetAddress", param.get("streetAddress"));

				Map<String, Object> locMap = (Map) tMap.get("location");
				locMap.put("observedAt", DateUtil.getTime());
				Map<String, Object> locValueMap = (Map) locMap.get("value");
				locValueMap.put("coordinates", param.get("location"));


				rtnList.add(tMap);

				String rtnStr = objectMapper.writeValueAsString(rtnList);

				List<Map<String, Object>> entities = objectMapper.readValue(rtnStr,
						new TypeReference<List<Map<String, Object>>>() {
						});
				Map<String, Object> body = new LinkedHashMap<>();
				body = new LinkedHashMap<>();

				body.put("datasetId", "DS_HealthMeasurement_LivingLab_Siheung");

				body.put("entities", entities);

				bodyStr = objectMapper.writeValueAsString(body);
				if("Y".equalsIgnoreCase(ingestYn)) {
				  log.info("postData : " + postData(interfaceApiUrl, bodyStr));
				}

			}
		} catch (Exception e) {
			log.error("Exception : " + ExceptionUtils.getStackTrace(e));
		}

		try {
		  String rtn = "";
      if("Y".equalsIgnoreCase(ingestYn)) {
        rtn = postData(interfaceApiUrl, bodyStr).toString();
      }

			return new ResponseEntity<>(rtn, HttpStatus.OK);
		} catch (Exception e) {
			log.error("Exception : " + ExceptionUtils.getStackTrace(e));
			return new ResponseEntity<>(JsonUtil.toMap(HttpStatus.NOT_FOUND).toString(), HttpStatus.NOT_FOUND);
		}
	}

	public static Object postData(String url, String body) throws Exception {
		Object obj = null;
		log.info("+++url:{}", url);
		log.info("+++body:{}", body);

		Header[] headers = new Header[] { new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json") };
		HttpResponse resp = UrlUtil.post(url, headers, body);
		log.info("+++resp:{}", resp);
		String payload = resp.getPayload();
		log.info("+++payload:{}", payload);
		if (resp.getStatusCode() >= 200 && resp.getStatusCode() < 301) {
			if (payload.startsWith("{")) {
				obj = new JSONObject(resp.getPayload());
			} else if (payload.startsWith("[")) {
				obj = new JSONArray(resp.getPayload());
			} else if (payload.toLowerCase().startsWith("<")) {
				obj = XML.toJSONObject(resp.getPayload());
			} else {
				obj = resp.getPayload();
			}
		} else {
			throw new Exception(resp.getStatusName());
		}
		return obj;
	}

	Map<String, Object> Find_wMap(String Property, Double value) {
		Map<String, Object> wMap = new LinkedHashMap<>();
		wMap.put("type", Property);
		wMap.put("value", value);
		wMap.put("observedAt", DateUtil.getTime());
		return wMap;
	}

	Map<String, Object> Find_wMap(String Property, String value) {
		Map<String, Object> wMap = new LinkedHashMap<>();
		wMap.put("type", Property);
		wMap.put("value", value);
		wMap.put("observedAt", DateUtil.getTime());
		return wMap;
	}

	Map<String, Object> tMapSet(Map<String, Object> tMap) {
		tMap.put("triglyceride", Find_wMap("Property", 0.0d));
		tMap.put("mentalStress", Find_wMap("Property", 0.0d));
		tMap.put("vascularPhase", Find_wMap("Property", 0.0d));
		tMap.put("hdlc", Find_wMap("Property", 0.0d));
		tMap.put("stressScore", Find_wMap("Property", 0.0d));
		tMap.put("vascularElasticity", Find_wMap("Property", 0.0d));
		tMap.put("abdominalObesityRate", Find_wMap("Property", 0.0d));
		tMap.put("minBloodPressure", Find_wMap("Property", 0.0d));

		tMap.put("type", "HealthMeasurement");

		tMap.put("remainedBloodVolume", Find_wMap("Property", 0.0d));
		tMap.put("boneVolume", Find_wMap("Property", 0.0d));
		tMap.put("fatRate", Find_wMap("Property", 0.0d));
		tMap.put("physicalStress", Find_wMap("Property", 0.0d));
		tMap.put("muscle", Find_wMap("Property", 0.0d));
		tMap.put("fat", Find_wMap("Property", 0.0d));
		tMap.put("weightControl", Find_wMap("Property", 0.0d));
		tMap.put("cholesterol", Find_wMap("Property", 0.0d));
		tMap.put("physicalDevelopment", Find_wMap("Property", 0.0d));

		tMap.put("id", null);

		tMap.put("ldlc", Find_wMap("Property", 0.0d));
		tMap.put("height", Find_wMap("Property", 0.0d));

		Map<String, Object> addressValue = new LinkedHashMap<>();
		addressValue.put("addressCountry", "");
		addressValue.put("addressLocality", "");
		addressValue.put("addressRegion", "");
		addressValue.put("addressTown", "");
		addressValue.put("streetAddress", "");
		Map<String, Object> address = new LinkedHashMap<>();
		address.put("value", addressValue);
		address.put("type", "Property");
		tMap.put("address", address);

		tMap.put("globalLocationNumber", Find_wMap("Property", 0.0d));
		tMap.put("stressManagemenet", Find_wMap("Property", 0.0d));
		tMap.put("beforeBloodSugar", Find_wMap("Property", 0.0d));
		tMap.put("afterBloodSugar", Find_wMap("Property", 0.0d));
		tMap.put("weight", Find_wMap("Property", 0.0d));
		tMap.put("waterRate", Find_wMap("Property", 0.0d));

		ArrayList<String> context = new ArrayList<>();
		context.add("http://uri.etsi.org/ngsi-ld/core-context.jsonld");
		context.add("http://citydatahub.siheung.kr/ngsi-ld/healthcare.jsonld");
		tMap.put("@context", context);

		tMap.put("fatControl", Find_wMap("Property", 0.0d));
		tMap.put("water", Find_wMap("Property", 0.0d));
		tMap.put("fatLevel", Find_wMap("Property", 0.0d));
		tMap.put("muscleControl", Find_wMap("Property", 0.0d));
		tMap.put("maxBloodPressure", Find_wMap("Property", 0.0d));
		tMap.put("kcal", Find_wMap("Property", 0.0d));
		tMap.put("pulse", Find_wMap("Property", 0.0d));
		tMap.put("cardiacOutput", Find_wMap("Property", 0.0d));

		Map<String, Object> locationValue = new LinkedHashMap<>();
		locationValue.put("type", "Point");
		ArrayList<Float> coordinates2 = new ArrayList<>();
		coordinates2.add(0.0f);
		coordinates2.add(0.0f);
		locationValue.put("coordinates", coordinates2);
		Map<String, Object> location = new LinkedHashMap<>();
		location.put("observedAt", null);
		location.put("type", "GeoProperty");
		location.put("value", locationValue);
		tMap.put("location", location);

		tMap.put("waist", Find_wMap("Property", 0.0d));

		Map<String, Object> dataProvider3 = new LinkedHashMap<>();
		dataProvider3.put("type", "Property");
		dataProvider3.put("value", "www");
		tMap.put("dataProvider", dataProvider3);

		tMap.put("bmi", Find_wMap("Property", 0.0d));
		return tMap;
	}

	////////////////////////////////////////////////////////////////////////////

	@GetMapping({ "/sample" })
	public ResponseEntity<List<Map>> getAll() {
		try {
			List<Map> result = svc.getAll();
			log.debug("result : {}", result);
			if (result == null) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			} else {
				return new ResponseEntity<>(result, HttpStatus.OK);
			}
		} catch (Exception e) {
			log.error("Exception : " + ExceptionUtils.getStackTrace(e));
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

	}

	@GetMapping("/sample/{id}")
	public ResponseEntity<Map> get(@PathVariable("id") String id) {
		try {
			Map result = svc.selectAllAgentId(id);
			log.debug("result : {}", result);
			if (result == null) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			} else {
				return new ResponseEntity<>(result, HttpStatus.OK);
			}
		} catch (Exception e) {
			log.error("Exception : " + ExceptionUtils.getStackTrace(e));
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@PostMapping("/sample")
	public ResponseEntity<Map> create(@RequestBody Map param, UriComponentsBuilder ucBuilder) {
		try {
			if (svc.isExist(param.get("agent_id") + "")) {
				return new ResponseEntity<>(JsonUtil.toMap(HttpStatus.CONFLICT), HttpStatus.CONFLICT);
			} else {
				svc.insert(param);
				return new ResponseEntity<>(JsonUtil.toMap(HttpStatus.CREATED), HttpStatus.CREATED);
			}
		} catch (Exception e) {
			log.error("Exception : " + ExceptionUtils.getStackTrace(e));
			return new ResponseEntity<>(JsonUtil.toMap(HttpStatus.CREATED), HttpStatus.CREATED);
		}

	}

	@PutMapping(value = "/sample/{id}")
	public ResponseEntity<?> update(@PathVariable("id") String id, @RequestBody Map param) {
		log.debug("param : {}, {}", id, param);
		try {
			if (svc.isExist(id)) {
				param.put("agent_id", id);
				svc.update(id, param);
				return new ResponseEntity<>(JsonUtil.toMap(HttpStatus.OK, param), HttpStatus.OK);
			} else {
				return new ResponseEntity<>(JsonUtil.toMap(HttpStatus.NOT_FOUND), HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			log.error("Exception : " + ExceptionUtils.getStackTrace(e));
			return new ResponseEntity<>(JsonUtil.toMap(HttpStatus.NOT_FOUND), HttpStatus.NOT_FOUND);
		}

	}

	@DeleteMapping(value = "/sample/{id}")
	public ResponseEntity<Map> delete(@PathVariable("id") String id) {
		try {
			if (svc.isExist(id)) {
				if (!svc.delete(id)) {
					return new ResponseEntity<>(JsonUtil.toMap(HttpStatus.NOT_FOUND), HttpStatus.NOT_FOUND);
				} else {
					return new ResponseEntity<>(JsonUtil.toMap(HttpStatus.OK), HttpStatus.OK);
				}
			} else {
				return new ResponseEntity<>(JsonUtil.toMap(HttpStatus.NOT_FOUND), HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			log.error("Exception : " + ExceptionUtils.getStackTrace(e));
			return new ResponseEntity<>(JsonUtil.toMap(HttpStatus.NOT_FOUND), HttpStatus.NOT_FOUND);
		}

	}

	@PostMapping(value = { "/logger/tail", "/agents/{agentId}/adaptors/{adaptorId}/logs" })
	public ResponseEntity<String> tail(@RequestBody Map param) {

		HttpResponse resp = null;
		try {
			JSONObject body = new JSONObject();
			body.put("sourceName", param.get("sourceName"));
			body.put("preEndPoint", param.get("preEndPoint"));

			Header[] headers = new Header[] { new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json") };
			resp = UrlUtil.post(configEnv.getLogUrl() + "", headers, body.toString());

		} catch (Exception e) {
			log.error("Exception : " + ExceptionUtils.getStackTrace(e));
		}

		return new ResponseEntity<>(resp.getPayload(), HttpStatus.OK);
	}

	@PostMapping({ "/api/connectivity/agents/{agentId}/adaptors/{adaptorId}/logs" })
	public String create2(@PathVariable("agentId") String agentId, @PathVariable("adaptorId") String adaptorId,
			@RequestBody Map param, HttpServletRequest request) {
		log.debug("----- AgentController.agents -----");
		String temp = "";
		try {
			JSONObject jo = new JSONObject(param);
			temp = httpConnection(request, "/api/connectivity/agents/" + agentId + "/adaptors/" + adaptorId + "/logs",
					"POST", jo.toString());

		} catch (Exception e) {
			log.error("Exception : " + ExceptionUtils.getStackTrace(e));
		}
		return temp;
	}

	@PostMapping(value = { "/agent/start", "/restApi/agent/start" })
	public ResponseEntity<Map> start(@RequestBody Map param) {

		HttpResponse resp = null;
		try {
			JSONObject body = new JSONObject();
			body.put("id", param.get("id"));

			Header[] headers = new Header[] { new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json") };
			log.debug(":::::::::::::::" + configEnv.getAgentUrl());
			resp = UrlUtil.post(configEnv.getAgentUrl() + "/start", headers, body.toString());
			param.put("result", resp.getPayload());
		} catch (Exception e) {
			log.error("Exception : " + ExceptionUtils.getStackTrace(e));
		}

		return new ResponseEntity<>(param, HttpStatus.OK);
	}

	@PostMapping(value = { "/agent/stop", "/restApi/agent/stop" })
	public ResponseEntity<Map> stop(@RequestBody Map param) {

		HttpResponse resp = null;
		try {
			JSONObject body = new JSONObject();
			body.put("id", param.get("id"));

			Header[] headers = new Header[] { new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json") };
			resp = UrlUtil.post(configEnv.getAgentUrl() + "/stop", headers, body.toString());
			param.put("result", resp.getPayload());
		} catch (Exception e) {
			log.error("Exception : " + ExceptionUtils.getStackTrace(e));
		}

		return new ResponseEntity<>(param, HttpStatus.OK);
	}

	@PostMapping(value = { "/agent/restart", "/restApi/agent/restart" })
	public ResponseEntity<Map> restart(@RequestBody Map param) {

		HttpResponse resp = null;
		try {
			JSONObject body = new JSONObject();
			body.put("id", param.get("id"));

			Header[] headers = new Header[] { new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json") };
			resp = UrlUtil.post(configEnv.getAgentUrl() + "/restart", headers, body.toString());
			param.put("result", resp.getPayload());
		} catch (Exception e) {
			log.error("Exception : " + ExceptionUtils.getStackTrace(e));
		}

		return new ResponseEntity<>(param, HttpStatus.OK);
	}

	@PostMapping(value = { "/agent/status", "/restApi/agent/status" })
	public ResponseEntity<Map> status(@RequestBody Map param) {

		HttpResponse resp = null;
		try {
			JSONObject body = new JSONObject();
			body.put("id", param.get("id"));
			Header[] headers = new Header[] { new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json") };
			resp = UrlUtil.post(configEnv.getAgentUrl() + "/status", headers, body.toString());
			param.put("result", resp.getPayload());
		} catch (Exception e) {
			log.error("Exception : " + ExceptionUtils.getStackTrace(e));
		}

		return new ResponseEntity<>(param, HttpStatus.OK);
	}


	public String httpConnection(HttpServletRequest request, String targetUrl, String method, String jsonBody) {
		URL url = null;
		HttpURLConnection conn = null;
		String result = "";
		JSONObject json = null;
		String jsonData = "";
		BufferedReader br = null;
		StringBuffer sb = null;
		String returnText = "";
		HttpSession session = request.getSession();

		try {

			String stoken = (String) session.getAttribute("token");
			if (stoken != null) {
				if (stoken.length() > 0) {
					if ("{".equals(stoken.substring(0, 1))) {
						json = new JSONObject(stoken);
					}
				}
			}

			String token = (String) json.get("access_token");
			log.debug(jsonBody);
			/*
       * TODO 게이트웨이 주소
       */
      String gatewayUrl = "";
			url = new URL(gatewayUrl + targetUrl);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestProperty("Accept", "application/json");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestMethod(method);
			conn.setRequestProperty("Authorization", "Bearer " + token);

			if (!"".equals(jsonBody)) {
				conn.setDoOutput(true);
				OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
				wr.write(jsonBody);
				wr.flush();
			}


			br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

			sb = new StringBuffer();

			while ((jsonData = br.readLine()) != null) {
				sb.append(jsonData);
			}
			returnText = sb.toString();


		} catch (IOException e) {
			log.error("Exception : " + ExceptionUtils.getStackTrace(e));
		} finally {
		}
		return returnText;
	}


}
