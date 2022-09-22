package com.cityhub.adapter.convex;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.cityhub.core.AbstractConvert;
import com.cityhub.environment.Constants;
import com.cityhub.exception.CoreException;
import com.cityhub.utils.CommonUtil;
import com.cityhub.utils.DataCoreCode.ErrorCode;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.JsonUtil;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvWaterQualityIndustry_PublicDataPortal extends AbstractConvert {
	private ObjectMapper objectMapper;
	private String gettime;

	@Override
	public void init(JSONObject ConfItem, JSONObject templateItem) {
		super.setup(ConfItem, templateItem);
		this.objectMapper = new ObjectMapper();
		this.objectMapper.setSerializationInclusion(Include.NON_NULL);
		this.objectMapper.setDateFormat(new SimpleDateFormat(Constants.CONTENT_DATE_FORMAT));
		this.objectMapper.setTimeZone(TimeZone.getTimeZone(Constants.CONTENT_DATE_TIMEZONE));
	}

	@Override
	public String doit() throws CoreException {

		List<Map<String, Object>> rtnList = new LinkedList<>();
		String rtnStr = "";

		try {
			JSONArray svcList = ConfItem.getJSONArray("serviceList");

			for (int i = 0; i < svcList.length(); i++) {
				JSONObject iSvc = svcList.getJSONObject(i);
				id = iSvc.getString("gs1Code");
				JsonUtil ju = new JsonUtil((JSONObject) CommonUtil.getData(iSvc));
				Thread.sleep(300);
				if (ju.getObj("response.body.items").toString().equals("")) {
					log(SocketCode.SOCKET_CONNECT_FAIL, id);
					continue;
				} else {
					log(SocketCode.DATA_RECEIVE, id, ju.toString().getBytes());
					JSONArray arr = new JSONArray();
					Integer arrlength;
					boolean JsonArrayAs = ju.getObj("response.body.items.item") instanceof JSONArray;

					if (JsonArrayAs) {
						arr = (JSONArray) ju.getObj("response.body.items.item");
						arrlength = arr.length();
					} else if (ju.getObj("response.body.items.item") instanceof JSONObject) {
						arrlength = 1;
					} else {
						log(SocketCode.SOCKET_CONNECT_FAIL, id);
						continue;
					}

					for (int j = 0; j < arrlength; j++) {

						JSONObject item;
						if (JsonArrayAs) {
							item = arr.getJSONObject(j);
						} else {
							item = (JSONObject) ju.getObj("response.body.items.item");
						}

						if (item != null) {

							Map<String, Object> tMap = objectMapper.readValue(
									templateItem.getJSONObject(ConfItem.getString("modelId")).toString(),
									new TypeReference<Map<String, Object>>() {
									});
							Map<String, Object> wMap;

							gettime = item.optString("mesurede");

							if (item.has("item1"))
								Find_wMap(tMap, "temperatureOfSource").put("value", item.optInt("item1", 0));
							else
								tMap.remove(tMap, "temperatureOfSource");

							if (item.has("item2"))
								Find_wMap(tMap, "temperatureOfPrecipitation").put("value", item.optInt("item2", 0));
							else
								tMap.remove(tMap, "temperatureOfPrecipitation");

							if (item.has("item3"))
								Find_wMap(tMap, "hydrogenIndexOfSource").put("value", item.optDouble("item3", 0.0d));
							else
								tMap.remove(tMap, "hydrogenIndexOfSource");

							if (item.has("item4"))
								Find_wMap(tMap, "hydrogenIndexOfPrecipitation").put("value",
										item.optDouble("item4", 0.0d));
							else
								tMap.remove(tMap, "hydrogenIndexOfPrecipitation");

							if (item.has("item5"))
								Find_wMap(tMap, "turbidityOfSource").put("value", item.optDouble("item5", 0.0d));
							else
								tMap.remove(tMap, "turbidityOfSource");

							if (item.has("item6"))
								Find_wMap(tMap, "turbidityOfPrecipitation").put("value", item.optDouble("item6", 0.0d));
							else
								tMap.remove(tMap, "turbidityOfPrecipitation");

							if (item.has("item7"))
								Find_wMap(tMap, "conductivityOfSource").put("value", item.optInt("item7", 0));
							else
								tMap.remove(tMap, "conductivityOfSource");

							if (item.has("item8"))
								Find_wMap(tMap, "conductivityOfPrecipitation").put("value", item.optInt("item8", 0));
							else
								tMap.remove(tMap, "conductivityOfPrecipitation");

							if (item.has("item9"))
								Find_wMap(tMap, "alkaliOfSource").put("value", item.optInt("item9", 0));
							else
								tMap.remove(tMap, "alkaliOfSource");

							if (item.has("item10"))
								Find_wMap(tMap, "alkaliOfPrecipitation").put("value", item.optInt("item10", 0));
							else
								tMap.remove(tMap, "alkaliOfPrecipitation");


							wMap = (Map) tMap.get("dataProvider");
							wMap.put("value", iSvc.optString("dataProvider", "http://www.kwater.or.kr"));

							wMap = (Map) tMap.get("globalLocationNumber");
							wMap.put("value", id);

							Map<String, Object> addrValue = (Map) ((Map) tMap.get("address")).get("value");
							addrValue.put("addressCountry", iSvc.optString("addressCountry"));
							addrValue.put("addressRegion", iSvc.optString("addressRegion"));
							addrValue.put("addressLocality", iSvc.optString("addressLocality"));
							addrValue.put("addressTown", iSvc.optString("addressTown"));
							addrValue.put("streetAddress", iSvc.optString("streetAddress"));

							Map<String, Object> locMap = (Map) tMap.get("location");
							locMap.put("observedAt", DateUtil.getTime());
							Map<String, Object> locValueMap = (Map) locMap.get("value");
							locValueMap.put("coordinates", iSvc.getJSONArray("location"));

							tMap.put("id", id);

							rtnList.add(tMap);
							String str = objectMapper.writeValueAsString(tMap);
							log(SocketCode.DATA_CONVERT_SUCCESS, id, str.getBytes());
						}
					}
				}
			}
			rtnStr = objectMapper.writeValueAsString(rtnList);
			if (rtnStr.length() < 10) {
				throw new CoreException(ErrorCode.NORMAL_ERROR);
			}
		} catch (CoreException e) {
			if ("!C0099".equals(e.getErrorCode())) {
				log(SocketCode.DATA_CONVERT_FAIL, id, e.getMessage());
			}
		} catch (Exception e) {
			log(SocketCode.DATA_CONVERT_FAIL, id, e.getMessage());
			throw new CoreException(ErrorCode.NORMAL_ERROR, e.getMessage() + "'" + id, e);
		}

		return rtnStr;
	}

	Map<String, Object> Find_wMap(Map<String, Object> tMap, String Name) {
		Map<String, Object> ValueMap = (Map) tMap.get(Name);
		Calendar cal = Calendar.getInstance();
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		Date date = null;
		try {
			date = df.parse(gettime);
		} catch (ParseException e) {
		  log.error("Exception : "+ExceptionUtils.getStackTrace(e));
		}
		cal.setTime(date);
		DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss,SSSXXX");
		ValueMap.put("observedAt", df2.format(cal.getTime()));
		return ValueMap;
	}

	void Delete_wMap(Map<String, Object> tMap, String Name) {
		tMap.remove(Name);
	}

} // end of class
