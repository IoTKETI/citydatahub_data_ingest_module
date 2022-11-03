
-- 성남시 주차장 DML
INSERT INTO public.agent_info (agent_id, agent_nm, ip_addr, port_number, agent_div, sink_module, adapter_reg_num, etc_note, use_yn, first_create_dt,  last_update_dt) VALUES (
'AgentMqttt', 'MQTTAgent', NULL, 8080, NULL, NULL, NULL, '성남시 주차장 에이전트', 'Y', now(), now());

INSERT INTO public.adapter_info (adapter_id, adapter_nm, agent_id, target_platform_type, inout_div, use_yn, first_create_dt,  last_update_dt) VALUES (
'mqttOffstreet', '성남시 주차장 어댑터', 'AgentMqttt', 'I1120', NULL, 'Y', now(), now());

INSERT INTO public.instance_info (instance_id, instance_nm, adapter_id,datamodel_conv_div, adapter_type_id, use_yn, etc_note) VALUES (
'mqttOffStreet_001', 'mqttOffStreet', 'mqttOffstreet', 'N', 'M000000002', 'Y', '성남시 주차장');

INSERT INTO public.instance_detail_conf (instance_id, item, value, item_described, display_seq, sector) VALUES ('mqttOffStreet_001', 'MODEL_ID', 'OffStreetParking,ParkingSpot', 'model id', 1, 'b');
INSERT INTO public.instance_detail_conf (instance_id, item, value, item_described, display_seq, sector) VALUES ('mqttOffStreet_001', 'DATASET_ID', ' pocOffStreetParking,pocParkingSpot', 'dataset id', 2, 'b');
INSERT INTO public.instance_detail_conf (instance_id, item, value, item_described, display_seq, sector) VALUES ('mqttOffStreet_001', 'INVOKE_CLASS', 'com.cityhub.adapter.convex.ConvParkingOneM2M', '', 3, 'b');
INSERT INTO public.instance_detail_conf (instance_id, item, value, item_described, display_seq, sector) VALUES ('mqttOffStreet_001', 'REQ_PREFIX', '/oneM2M/req/Mobius2/', '', 4, 'b');
INSERT INTO public.instance_detail_conf (instance_id, item, value, item_described, display_seq, sector) VALUES ('mqttOffStreet_001', 'RESP_PREFIX', '/oneM2M/resp/Mobius2/', '', 5, 'b');
INSERT INTO public.instance_detail_conf (instance_id, item, value, item_described, display_seq, sector) VALUES ('mqttOffStreet_001', 'TOPIC', 'SlotYatopParking', '', 6, 'b');
INSERT INTO public.instance_detail_conf (instance_id, item, value, item_described, display_seq, sector) VALUES ('mqttOffStreet_001', 'META_INFO', 'http://203.253.128.164:7579/Mobius/sync_parking_raw', '', 7, 'b');
INSERT INTO public.instance_detail_conf (instance_id, item, value, item_described, display_seq, sector) VALUES ('mqttOffStreet_001', 'URL_ADDR', 'tcp://203.253.128.164:1883', '', 8, 'b');


-- 기상관측 DML
INSERT INTO public.agent_info (agent_id, agent_nm, ip_addr, port_number, etc_note, use_yn, first_create_dt,  last_update_dt) VALUES (
'AgentWeatherObserved', 'AgentWeatherObserved', NULL, 8080, '성남시 기상관측 에이전트', 'Y', now(), now());

INSERT INTO public.adapter_info (adapter_id, adapter_nm, agent_id, target_platform_type, use_yn, first_create_dt,  last_update_dt) VALUES (
'weatherObserved', '성남시 기상관측 어댑터', 'AgentWeatherObserved', 'I1110', 'Y', now(), now());

INSERT INTO public.instance_info (instance_id, instance_nm, adapter_id,datamodel_conv_div, adapter_type_id, use_yn, etc_note) VALUES (
'weatherObserved_001', 'weatherObserved', 'weatherObserved', 'N', 'M000000003', 'Y', '성남시 기상관측');

INSERT INTO public.instance_detail_conf (instance_id, item, value, item_described, display_seq, sector) VALUES ('weatherObserved_001', 'MODEL_ID', 'WeatherObserved', 'model id', 1, 'b');
INSERT INTO public.instance_detail_conf (instance_id, item, value, item_described, display_seq, sector) VALUES ('weatherObserved_001', 'DATASET_ID', ' pocWeatherObserved', 'dataset id', 2, 'b');
INSERT INTO public.instance_detail_conf (instance_id, item, value, item_described, display_seq, sector) VALUES ('weatherObserved_001', 'INVOKE_CLASS', 'com.cityhub.adapter.convex.ConvWeatherObserved', '', 3, 'b');
INSERT INTO public.instance_detail_conf (instance_id, item, value, item_described, display_seq, sector) VALUES ('weatherObserved_001', 'url_addr', 'http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst?dataType=json&numOfRows=1000&serviceKey=xxu2gHkHh5PrWLXUSnk%2BqICJc2%2FwsENQLJnapmbP0S52Jg7FxIFohMk3FfhI5mkp5Dz7ir%2FuocdMHrnGEP9ZBQ%3D%3D&nx=63&ny=124', '', 4, 'a');
INSERT INTO public.instance_detail_conf (instance_id, item, value, item_described, display_seq, sector) VALUES ('weatherObserved_001', 'ParamVariable', 'base_date,base_time', '', 5, 'a');
INSERT INTO public.instance_detail_conf (instance_id, item, value, item_described, display_seq, sector) VALUES ('weatherObserved_001', 'base_date', 'yyyyMMdd,MINUTE,-40', '', 6, 'a');
INSERT INTO public.instance_detail_conf (instance_id, item, value, item_described, display_seq, sector) VALUES ('weatherObserved_001', 'base_time', 'HHmm,MINUTE,-40', '', 7, 'a');
INSERT INTO public.instance_detail_conf (instance_id, item, value, item_described, display_seq, sector) VALUES ('weatherObserved_001', 'gs1Code', 'urn:datahub:WeatherObserved:14858', '', 8, 'a');
INSERT INTO public.instance_detail_conf (instance_id, item, value, item_described, display_seq, sector) VALUES ('weatherObserved_001', 'addressCountry', 'KR', '', 9, 'a');
INSERT INTO public.instance_detail_conf (instance_id, item, value, item_described, display_seq, sector) VALUES ('weatherObserved_001', 'addressRegion', '경기도', '', 10, 'a');
INSERT INTO public.instance_detail_conf (instance_id, item, value, item_described, display_seq, sector) VALUES ('weatherObserved_001', 'addressLocality', '성남시', '', 11, 'a');
INSERT INTO public.instance_detail_conf (instance_id, item, value, item_described, display_seq, sector) VALUES ('weatherObserved_001', 'addressTown', '수정구', '', 12, 'a');
INSERT INTO public.instance_detail_conf (instance_id, item, value, item_described, display_seq, sector) VALUES ('weatherObserved_001', 'streetAddress', '경기도 성남시 수정구 수정로 319', '', 13, 'a');
INSERT INTO public.instance_detail_conf (instance_id, item, value, item_described, display_seq, sector) VALUES ('weatherObserved_001', 'location', '[127.14858, 37.4557691]', '', 14, 'a');




# 성남시 주차장(conf file) 생성
# curl --location --request GET 'http://localhost:8080/restApi/pushConf/AgentMqttt/mqttOffstreet' --header 'Content-Type: application/json' 

# 성남시 기상관측(conf file) 생성
# curl --location --request GET 'http://localhost:8080/restApi/pushConf/AgentWeatherObserved/weatherObserved' --header 'Content-Type: application/json' 

