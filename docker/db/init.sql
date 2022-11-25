--CREATE DATABASE ingest TEMPLATE template0 LC_COLLATE 'C';

-- \connect ingest

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';
SET default_table_access_method = heap;


CREATE TABLE public.adapter_info (
  adapter_id varchar(20) NOT NULL,
  adapter_nm varchar(200) NULL,
  agent_id varchar(20) NULL,
  target_platform_type varchar(5) NULL,
  inout_div varchar(5) NULL,
  use_yn bpchar(1) NULL,
  first_create_dt timestamp NULL,
  first_create_id varchar(20) NULL,
  last_update_dt timestamp NULL,
  last_update_id varchar(20) NULL,
  PRIMARY KEY("adapter_id")
);
COMMENT ON TABLE "public"."adapter_info" IS 'Adaptor정보';
COMMENT ON COLUMN "public"."adapter_info"."adapter_id" IS 'Adaptor_ID';
COMMENT ON COLUMN "public"."adapter_info"."adapter_nm" IS 'Adaptor명';
COMMENT ON COLUMN "public"."adapter_info"."agent_id" IS 'Agent_ID';
COMMENT ON COLUMN "public"."adapter_info"."target_platform_type" IS 'Adaptor유형';
COMMENT ON COLUMN "public"."adapter_info"."inout_div" IS '입출력구분';
COMMENT ON COLUMN "public"."adapter_info"."use_yn" IS '사용여부';
COMMENT ON COLUMN "public"."adapter_info"."first_create_dt" IS '최초_생성일시';
COMMENT ON COLUMN "public"."adapter_info"."first_create_id" IS '최초_생성자ID';
COMMENT ON COLUMN "public"."adapter_info"."last_update_dt" IS '최종_수정일시';
COMMENT ON COLUMN "public"."adapter_info"."last_update_id" IS '최종_수정자ID';

CREATE TABLE public.adapter_type_detail_conf (
  adapter_type_id varchar(10) NOT NULL,
  item varchar(200) NOT NULL,
  value varchar(4000) NULL,
  essential_yn bpchar(1) NULL,
  item_described varchar(1000) NULL,
  value_type varchar(5) NULL,
  display_seq int4 NULL,
  first_create_dt timestamp NULL,
  first_create_id varchar(20) NULL,
  last_update_dt timestamp NULL,
  last_update_id varchar(20) NULL,
  setup_method varchar(5) NULL,
  change_able_yn bpchar(1) NULL,
  use_purpose varchar(5) NULL,
  sector varchar(10) NULL DEFAULT 'a'::character varying,
  PRIMARY KEY("adapter_type_id","item")
);
CREATE TABLE public.adapter_type_info (
  adapter_type_id varchar(10) NOT NULL,
  adapter_type_nm varchar(200) NULL,
  adapter_type_div varchar(5) NULL,
  use_yn bpchar(1) NULL,
  first_create_dt timestamp NULL,
  first_create_id varchar(20) NULL,
  last_update_dt timestamp NULL,
  last_update_id varchar(20) NULL,
  PRIMARY KEY("adapter_type_id")
);
CREATE TABLE public.agent_info (
  agent_id varchar(20) NOT NULL,
  agent_nm varchar(200) NULL,
  ip_addr varchar(50) NULL,
  port_number int4 NULL,
  agent_div varchar(5) NULL,
  sink_module varchar(5) NULL,
  adapter_reg_num int4 NULL,
  etc_note varchar(200) NULL,
  use_yn bpchar(1) NULL,
  first_create_dt timestamp NULL,
  first_create_id varchar(20) NULL,
  last_update_dt timestamp NULL,
  last_update_id varchar(20) NULL,
  PRIMARY KEY("agent_id")
);

COMMENT ON TABLE "public"."agent_info" IS 'Agent 정보';
COMMENT ON COLUMN "public"."agent_info"."agent_id" IS 'Agent_ID';
COMMENT ON COLUMN "public"."agent_info"."agent_nm" IS 'Agent명';
COMMENT ON COLUMN "public"."agent_info"."ip_addr" IS 'IP';
COMMENT ON COLUMN "public"."agent_info"."port_number" IS 'Port';
COMMENT ON COLUMN "public"."agent_info"."agent_div" IS 'Agent구분';
COMMENT ON COLUMN "public"."agent_info"."sink_module" IS '싱크모듈';
COMMENT ON COLUMN "public"."agent_info"."adapter_reg_num" IS 'Adaptor등록수';
COMMENT ON COLUMN "public"."agent_info"."etc_note" IS '비고';
COMMENT ON COLUMN "public"."agent_info"."use_yn" IS '사용여부';
COMMENT ON COLUMN "public"."agent_info"."first_create_dt" IS '최초_생성일시';
COMMENT ON COLUMN "public"."agent_info"."first_create_id" IS '최초_생성자ID';
COMMENT ON COLUMN "public"."agent_info"."last_update_dt" IS '최종_수정일시';
COMMENT ON COLUMN "public"."agent_info"."last_update_id" IS '최종_수정자ID';
CREATE TABLE public.comm_code (
  code_type_id varchar(5) NOT NULL,
  code_id varchar(5) NOT NULL,
  code_nm varchar(200) NULL,
  use_yn bpchar(1) NULL,
  first_create_dt timestamp NULL,
  first_create_id varchar(20) NULL,
  last_update_dt timestamp NULL,
  last_update_id varchar(20) NULL,
  PRIMARY KEY("code_type_id","code_id")
);
COMMENT ON TABLE "public"."comm_code" IS '코드';
COMMENT ON COLUMN "public"."comm_code"."code_type_id" IS '코드유형_ID';
COMMENT ON COLUMN "public"."comm_code"."code_id" IS '코드_ID';
COMMENT ON COLUMN "public"."comm_code"."code_nm" IS '코드명';
COMMENT ON COLUMN "public"."comm_code"."use_yn" IS '사용여부';
COMMENT ON COLUMN "public"."comm_code"."first_create_dt" IS '최초_생성일시';
COMMENT ON COLUMN "public"."comm_code"."first_create_id" IS '최초_생성자ID';
COMMENT ON COLUMN "public"."comm_code"."last_update_dt" IS '최종_수정일시';
COMMENT ON COLUMN "public"."comm_code"."last_update_id" IS '최종_수정자ID';
CREATE TABLE public.comm_type (
  code_type_id varchar(5) NOT NULL,
  code_type_nm varchar(200) NULL,
  use_yn bpchar(1) NULL,
  first_create_dt timestamp NULL,
  first_create_id varchar(20) NULL,
  last_update_dt timestamp NULL,
  last_update_id varchar(20) NULL,
  PRIMARY KEY("code_type_id")
);

COMMENT ON TABLE "public"."comm_type" IS '코드유형';
COMMENT ON COLUMN "public"."comm_type"."code_type_id" IS '코드유형_ID';
COMMENT ON COLUMN "public"."comm_type"."code_type_nm" IS '코드유형명';
COMMENT ON COLUMN "public"."comm_type"."use_yn" IS '사용여부';
COMMENT ON COLUMN "public"."comm_type"."first_create_dt" IS '최초_생성일시';
COMMENT ON COLUMN "public"."comm_type"."first_create_id" IS '최초_생성자ID';
COMMENT ON COLUMN "public"."comm_type"."last_update_dt" IS '최종_수정일시';
COMMENT ON COLUMN "public"."comm_type"."last_update_id" IS '최종_수정자ID';
CREATE TABLE public.dm_transform_conf (
  datamodel_tf_id varchar(200) NOT NULL,
  datamodel_tf_seq int4 NOT NULL,
  st_datamodel_seq int4 NULL,
  ob_datamodel_seqs varchar(1000) NULL,
  datamodel_tf_script varchar(1000) NULL,
  first_create_dt timestamp NULL,
  first_create_id varchar(20) NULL,
  last_update_dt timestamp NULL,
  last_update_id varchar(20) NULL,
  PRIMARY KEY("datamodel_tf_id","datamodel_tf_seq")
);
COMMENT ON TABLE "public"."dm_transform_conf" IS '데이터모델변환상세구성정보';
COMMENT ON COLUMN "public"."dm_transform_conf"."datamodel_tf_id" IS '데이터모델변환_ID';
COMMENT ON COLUMN "public"."dm_transform_conf"."datamodel_tf_seq" IS '데이터모델변환_순번';
COMMENT ON COLUMN "public"."dm_transform_conf"."st_datamodel_seq" IS '표준데이터모델_순번';
COMMENT ON COLUMN "public"."dm_transform_conf"."ob_datamodel_seqs" IS '대상데이터모델_순번집합';
COMMENT ON COLUMN "public"."dm_transform_conf"."datamodel_tf_script" IS '데이터모델변환_스크립트';
COMMENT ON COLUMN "public"."dm_transform_conf"."first_create_dt" IS '최초_생성일시';
COMMENT ON COLUMN "public"."dm_transform_conf"."first_create_id" IS '최초_생성자ID';
COMMENT ON COLUMN "public"."dm_transform_conf"."last_update_dt" IS '최종_수정일시';
COMMENT ON COLUMN "public"."dm_transform_conf"."last_update_id" IS '최종_수정자ID';
CREATE TABLE public.dm_transform_info (
  datamodel_tf_id varchar(200) NOT NULL,
  ob_datamodel_id varchar(200) NULL,
  st_datamodel_id varchar(200) NULL,
  use_yn bpchar(1) NULL,
  first_create_dt timestamp NULL,
  first_create_id varchar(20) NULL,
  last_update_dt timestamp NULL,
  last_update_id varchar(20) NULL,
  PRIMARY KEY("datamodel_tf_id")
);
COMMENT ON TABLE "public"."dm_transform_info" IS '데이터모델변환정보';
COMMENT ON COLUMN "public"."dm_transform_info"."datamodel_tf_id" IS '데이터모델변환_ID';
COMMENT ON COLUMN "public"."dm_transform_info"."ob_datamodel_id" IS '대상데이터모델_ID';
COMMENT ON COLUMN "public"."dm_transform_info"."st_datamodel_id" IS '표준데이터모델_ID';
COMMENT ON COLUMN "public"."dm_transform_info"."use_yn" IS '사용여부';
COMMENT ON COLUMN "public"."dm_transform_info"."first_create_dt" IS '최초_생성일시';
COMMENT ON COLUMN "public"."dm_transform_info"."first_create_id" IS '최초_생성자ID';
COMMENT ON COLUMN "public"."dm_transform_info"."last_update_dt" IS '최종_수정일시';
COMMENT ON COLUMN "public"."dm_transform_info"."last_update_id" IS '최종_수정자ID';
CREATE TABLE public.instance_detail_conf (
  instance_id varchar(30) NOT NULL,
  item varchar(200) NOT NULL,
  value varchar(4000) NULL,
  item_described varchar(1000) NULL,
  display_seq int4 NOT NULL,
  first_create_dt timestamp NULL,
  first_create_id varchar(20) NULL,
  last_update_dt timestamp NULL,
  last_update_id varchar(20) NULL,
  sector varchar(10) NULL DEFAULT 'a'::character varying,
  PRIMARY KEY("instance_id","item")
);
CREATE TABLE public.instance_info (
  instance_id varchar(30) NOT NULL,
  instance_nm varchar(200) NULL,
  adapter_id varchar(20) NULL,
  image_extra_use_yn bpchar(1) NULL,
  video_extra_use_yn bpchar(1) NULL,
  gs1_use_yn bpchar(1) NULL,
  datamodel_conv_div varchar(5) NULL,
  datamodel_class_path varchar(500) NULL,
  target_addr varchar(500) NULL,
  target_port_number int4 NULL,
  exe_cycle varchar(100) NULL,
  data_format_type varchar(5) NULL,
  ref_doc_path varchar(500) NULL,
  datamodel_tf_id varchar(20) NULL,
  etc_note varchar(1000) NULL,
  use_yn bpchar(1) NULL,
  first_create_dt timestamp NULL,
  first_create_id varchar(20) NULL,
  last_update_dt timestamp NULL,
  last_update_id varchar(20) NULL,
  adapter_type_id varchar(10) NULL,
  PRIMARY KEY("instance_id")
);
COMMENT ON TABLE "public"."instance_info" IS '인스턴스정보';
COMMENT ON COLUMN "public"."instance_info"."instance_id" IS 'Instance_ID';
COMMENT ON COLUMN "public"."instance_info"."instance_nm" IS 'Instance명';
COMMENT ON COLUMN "public"."instance_info"."adapter_id" IS 'Adaptor_ID';
COMMENT ON COLUMN "public"."instance_info"."image_extra_use_yn" IS '이미지속성추출_사용여부';
COMMENT ON COLUMN "public"."instance_info"."video_extra_use_yn" IS '동영상속성추출_사용여부';
COMMENT ON COLUMN "public"."instance_info"."gs1_use_yn" IS 'GS1_사용여부';
COMMENT ON COLUMN "public"."instance_info"."datamodel_conv_div" IS '데이터모델_변환구분';
COMMENT ON COLUMN "public"."instance_info"."datamodel_class_path" IS '데이터모델_Class경로';
COMMENT ON COLUMN "public"."instance_info"."target_addr" IS '목적지_주소';
COMMENT ON COLUMN "public"."instance_info"."target_port_number" IS '목적지_Port';
COMMENT ON COLUMN "public"."instance_info"."exe_cycle" IS '실행주기';
COMMENT ON COLUMN "public"."instance_info"."data_format_type" IS '데이터포맷유형';
COMMENT ON COLUMN "public"."instance_info"."ref_doc_path" IS '참고문서경로';
COMMENT ON COLUMN "public"."instance_info"."datamodel_tf_id" IS '데이터모델변환_ID';
COMMENT ON COLUMN "public"."instance_info"."etc_note" IS '비고';
COMMENT ON COLUMN "public"."instance_info"."use_yn" IS '사용여부';
COMMENT ON COLUMN "public"."instance_info"."first_create_dt" IS '최초_생성일시';
COMMENT ON COLUMN "public"."instance_info"."first_create_id" IS '최초_생성자ID';
COMMENT ON COLUMN "public"."instance_info"."last_update_dt" IS '최종_수정일시';
COMMENT ON COLUMN "public"."instance_info"."last_update_id" IS '최종_수정자ID';

CREATE TABLE public.keyword_info (
    item character varying(200) NOT NULL,
    item_described character varying(1000),
    relation_code_id character varying(5),
    first_create_dt timestamp without time zone,
    first_create_id character varying(20),
    last_update_dt timestamp without time zone,
    last_update_id character varying(5)
);


COMMENT ON TABLE public.keyword_info IS '예약어_정보';
COMMENT ON COLUMN public.keyword_info.item IS '항목';
COMMENT ON COLUMN public.keyword_info.item_described IS '항목_설명';
COMMENT ON COLUMN public.keyword_info.relation_code_id IS '관련_코드_ID';
COMMENT ON COLUMN public.keyword_info.first_create_dt IS '최초_생성일시';
COMMENT ON COLUMN public.keyword_info.first_create_id IS '최초_생성자ID';
COMMENT ON COLUMN public.keyword_info.last_update_dt IS '최종_수정일시';
COMMENT ON COLUMN public.keyword_info.last_update_id IS '최종_수정자ID';



CREATE TABLE public.ob_datamodel (
  ob_datamodel_id varchar(20) NOT NULL,
  ob_datamodel_nm varchar(200) NULL,
  ob_datamodel_phy_nm varchar(200) NULL,
  ob_datamodel_div varchar(5) NULL,
  ob_system_nm varchar(200) NULL,
  described varchar(1000) NULL,
  ownership varchar(200) NULL,
  ob_datamodel_format varchar(5) NULL,
  use_yn bpchar(1) NULL,
  first_create_dt timestamp NULL,
  first_create_id varchar(20) NULL,
  last_update_dt timestamp NULL,
  last_update_id varchar(20) NULL,
  PRIMARY KEY("ob_datamodel_id")
);
COMMENT ON TABLE "public"."ob_datamodel" IS '대상데이터모델정보';
COMMENT ON COLUMN "public"."ob_datamodel"."ob_datamodel_id" IS '대상데이터모델_ID';
COMMENT ON COLUMN "public"."ob_datamodel"."ob_datamodel_nm" IS '대상데이터모델_명';
COMMENT ON COLUMN "public"."ob_datamodel"."ob_datamodel_phy_nm" IS '대상데이터모델_물리명';
COMMENT ON COLUMN "public"."ob_datamodel"."ob_datamodel_div" IS '대상데이터모델_구분';
COMMENT ON COLUMN "public"."ob_datamodel"."ob_system_nm" IS '대상시스템_명';
COMMENT ON COLUMN "public"."ob_datamodel"."described" IS '설명';
COMMENT ON COLUMN "public"."ob_datamodel"."ownership" IS '소유권';
COMMENT ON COLUMN "public"."ob_datamodel"."ob_datamodel_format" IS '대상데이터모델_포맷';
COMMENT ON COLUMN "public"."ob_datamodel"."use_yn" IS '사용여부';
COMMENT ON COLUMN "public"."ob_datamodel"."first_create_dt" IS '최초_생성일시';
COMMENT ON COLUMN "public"."ob_datamodel"."first_create_id" IS '최초_생성자ID';
COMMENT ON COLUMN "public"."ob_datamodel"."last_update_dt" IS '최종_수정일시';
COMMENT ON COLUMN "public"."ob_datamodel"."last_update_id" IS '최종_수정자ID';
CREATE TABLE public.ob_datamodel_conf (
  ob_datamodel_id varchar(20) NOT NULL,
  ob_datamodel_seq int4 NOT NULL,
  property_nm varchar(200) NULL,
  property varchar(200) NULL,
  "type" varchar(5) NULL,
  "option" varchar(5) NULL,
  described varchar(1000) NULL,
  first_create_dt timestamp NULL,
  first_create_id varchar(20) NULL,
  last_update_dt timestamp NULL,
  last_update_id varchar(20) NULL,
  property_path varchar(200) NULL,
  property_structure varchar(200) NULL,
  PRIMARY KEY("ob_datamodel_id","ob_datamodel_seq")
);
COMMENT ON TABLE "public"."ob_datamodel_conf" IS '대상데이터모델상세정보';
COMMENT ON COLUMN "public"."ob_datamodel_conf"."ob_datamodel_id" IS '대상데이터모델_ID';
COMMENT ON COLUMN "public"."ob_datamodel_conf"."ob_datamodel_seq" IS '대상데이터모델_순번';
COMMENT ON COLUMN "public"."ob_datamodel_conf"."property_nm" IS 'property_명';
COMMENT ON COLUMN "public"."ob_datamodel_conf"."property" IS 'Property';
COMMENT ON COLUMN "public"."ob_datamodel_conf"."type" IS 'Type';
COMMENT ON COLUMN "public"."ob_datamodel_conf"."option" IS 'Option';
COMMENT ON COLUMN "public"."ob_datamodel_conf"."described" IS '설명';
COMMENT ON COLUMN "public"."ob_datamodel_conf"."first_create_dt" IS '최초_생성일시';
COMMENT ON COLUMN "public"."ob_datamodel_conf"."first_create_id" IS '최초_생성자ID';
COMMENT ON COLUMN "public"."ob_datamodel_conf"."last_update_dt" IS '최종_수정일시';
COMMENT ON COLUMN "public"."ob_datamodel_conf"."last_update_id" IS '최종_수정자ID';



CREATE SEQUENCE public.seq_connectivity_log
   INCREMENT BY 1
   MINVALUE 1
   MAXVALUE 9223372036854775807
   START 1;

CREATE TABLE public.connectivity_log (
  id int8 NOT NULL DEFAULT nextval('public.seq_connectivity_log'::regclass),
  log_dt timestamp NOT NULL,
  adapter_id varchar(100) NOT NULL,
  step varchar(4) NOT NULL,
  payload varchar(1000) NOT NULL,
  st_datamodel varchar(60) NULL,
  data_id varchar(200) NULL,
  log_desc varchar(1000) NULL,
  length numeric NULL,
  adapter_type varchar(20) NULL,
  first_create_dt timestamp NOT NULL,
  CONSTRAINT pk_connectivity_log_id PRIMARY KEY (id)
);
CREATE INDEX connectivity_log_log_dt_idx ON public.connectivity_log USING btree (log_dt, adapter_id, step);
CREATE INDEX connectivity_log_log_dt_idx2 ON public.connectivity_log USING btree (log_dt);

  
INSERT INTO public.comm_code (code_type_id,code_id,code_nm,use_yn,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('I07','I0701','string','Y','2019-08-05 00:45:59.836','system','2019-08-05 00:45:59.836','system'),
   ('I07','I0702','string(multi)','Y','2019-08-05 00:46:01.097','system','2019-08-05 00:46:01.097','system'),
   ('I07','I0703','integer','Y','2019-08-05 00:46:03.165','system','2019-08-05 00:46:03.165','system'),
   ('I07','I0704','array','Y','2019-08-05 00:46:05.388','system','2019-08-05 00:46:05.388','system'),
   ('I07','I0705','float','Y','2019-08-05 00:46:07.502','system','2019-08-05 00:46:07.502','system'),
   ('I07','I0706','datatime','Y','2019-08-05 00:46:09.908','system','2019-08-05 00:46:09.908','system'),
   ('I08','I0809','logger','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system'),
   ('I05','I0599','기타','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system'),
   ('I05','I0506','공공데이터','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system'),
   ('I05','I0505','에너지','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system');
INSERT INTO public.comm_code (code_type_id,code_id,code_nm,use_yn,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('I05','I0504','안전','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system'),
   ('I05','I0503','생활복지','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system'),
   ('I05','I0502','교통','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system'),
   ('I05','I0501','환경','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system'),
   ('I04','I0402','XML','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system'),
   ('I04','I0401','JSON','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system'),
   ('I03','I0303','미변환','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system'),
   ('I03','I0302','변환 Class','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system'),
   ('I03','I0301','속성간 맵핑','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system'),
   ('I02','I0203','데이터 송수신','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system');
INSERT INTO public.comm_code (code_type_id,code_id,code_nm,use_yn,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('I02','I0202','데이터 송신','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system'),
   ('I02','I0201','데이터 수신','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system'),
   ('I08','I0808','mysql','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system'),
   ('I08','I0807','oracle','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system'),
   ('I08','I0806','postgresql','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system'),
   ('I08','I0805','mongo DB','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system'),
   ('I08','I0804','ElasticSearch','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system'),
   ('I08','I0803','HDFS','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system'),
   ('I08','I0802','kfaka','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system'),
   ('I08','I0801','avro','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system');
INSERT INTO public.comm_code (code_type_id,code_id,code_nm,use_yn,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('I09','I0927','일자(yyyy-mm-dd)','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system'),
   ('I09','I0926','테라바이트(TB)','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system'),
   ('I09','I0925','기가바이트(GB)','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system'),
   ('I09','I0924','메가바이트(MB)','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system'),
   ('I09','I0923','킬로바이트(KB)','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system'),
   ('I09','I0922','바이트(byte)','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system'),
   ('I09','I0921','비트(bit)','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system'),
   ('I09','I0920','km/I(연비)','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system'),
   ('I09','I0919','km/h','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system'),
   ('I09','I0918','m/s','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system');
INSERT INTO public.comm_code (code_type_id,code_id,code_nm,use_yn,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('I09','I0917','파스칼','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system'),
   ('I09','I0916','기압','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system'),
   ('I09','I0915','화씨온도','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system'),
   ('I09','I0914','섭씨온도','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system'),
   ('I09','I0913','톤(t)','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system'),
   ('I09','I0912','킬로그램(kg)','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system'),
   ('I09','I0911','그램(g)','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system'),
   ('I09','I0910','밀리그램(mg)','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system'),
   ('I09','I0909','평','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system'),
   ('I09','I0908','제곱미터','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system');
INSERT INTO public.comm_code (code_type_id,code_id,code_nm,use_yn,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('I09','I0907','해리','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system'),
   ('I09','I0906','피트','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system'),
   ('I09','I0905','인치','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system'),
   ('I09','I0904','km','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system'),
   ('I09','I0903','m','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system'),
   ('I09','I0902','cm','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system'),
   ('I09','I0901','mm','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system'),
   ('I10','I1003','Read/Write','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system'),
   ('I10','I1002','WriteOnly','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system'),
   ('I10','I1001','ReadOnly','Y','2019-08-05 00:45:59','system','2019-08-05 00:45:59','system');
INSERT INTO public.comm_code (code_type_id,code_id,code_nm,use_yn,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('I11','I1120','OneM2M','Y','2019-08-26 09:37:33.402',NULL,'2019-08-26 09:37:33.402',NULL),
   ('I11','I1110','Open API','Y','2019-08-26 09:37:33.402',NULL,'2019-08-26 09:37:33.402',NULL),
   ('I11','I1130','U-City Platform','Y','2019-08-26 09:37:33.402',NULL,'2019-08-26 09:37:33.402',NULL),
   ('I11','I1140','Legacy Platform','Y','2019-08-26 09:37:33.402',NULL,'2019-08-26 09:37:33.402',NULL),
   ('I11','I1150','도시통합 Platform','Y','2019-08-26 09:37:33.402',NULL,'2019-08-26 09:37:33.402',NULL),
   ('I11','I1190','기타','Y','2019-08-26 09:37:33.402',NULL,'2019-08-26 09:37:33.402',NULL),
   ('I13','I1301','parameter(URL)','Y','2019-08-26 12:29:02.616',NULL,'2019-08-26 12:29:02.616',NULL),
   ('I13','I1309','부가정보','Y','2019-08-26 12:29:02.616',NULL,'2019-08-26 12:29:02.616',NULL),
   ('I12','I1201','입력(single line)','Y','2019-08-30 13:22:53.244',NULL,'2019-08-30 13:22:53.244',NULL),
   ('I12','I1202','입력(multi line / popup)','Y','2019-08-30 13:22:53.244',NULL,'2019-08-30 13:22:53.244',NULL);
INSERT INTO public.comm_code (code_type_id,code_id,code_nm,use_yn,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('I12','I1203','선택(Combo Box)','Y','2019-08-30 13:22:53.244',NULL,'2019-08-30 13:22:53.244',NULL),
   ('I12','I1220','jdbc 연결','Y','2019-08-26 12:29:24',NULL,'2019-08-26 12:29:24',NULL),
   ('I12','I1221','SQL','Y','2019-08-26 12:29:24',NULL,'2019-08-26 12:29:24',NULL),
   ('I01','I0101','송수신용 Agent','Y','2021-02-09 17:09:20.877546',NULL,'2021-02-09 17:09:20.877546',NULL),
   ('I01','I0102','연결용 Agent','Y','2021-02-09 17:09:20.877546',NULL,'2021-02-09 17:09:20.877546',NULL),
   ('I01','I0103','연결용 Agent','Y','2021-02-09 17:09:20.877546',NULL,'2021-02-09 17:09:20.877546',NULL);

INSERT INTO public.comm_code (code_type_id,code_id,code_nm,use_yn,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('S01','S0101','환경:S1','Y','2019-08-05 00:46:01','system','2019-08-05 00:46:01','system');
   
INSERT INTO public.comm_code (code_type_id,code_id,code_nm,use_yn,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('S01','S0102','에너지:S2','Y','2019-08-05 00:46:01','system','2019-08-05 00:46:01','system'),
   ('S01','S0103','생활:S3','Y','2019-08-05 00:46:01','system','2019-08-05 00:46:01','system'),
   ('S01','S0104','공공:S4','Y','2019-08-05 00:46:01','system','2019-08-05 00:46:01','system'),
   ('S01','S0105','시흥:S5','Y','2019-08-05 00:46:01','system','2019-08-05 00:46:01','system'),
   ('S01','S0106','자유과제:S6','Y','2019-08-05 00:46:01','system','2019-08-05 00:46:01','system'),
   ('I11','I1160','Fiware','Y','2021-10-13 11:08:03.038959',NULL,'2021-10-13 11:08:03.038959',NULL);

INSERT INTO public.comm_type (code_type_id,code_type_nm,use_yn,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('I01','Agent구분','Y','2019-08-21 16:06:42.959',NULL,'2021-02-10 16:28:08.339166',NULL),
   ('I02','입출력구분','Y','2019-08-21 16:07:11.412',NULL,'2021-02-10 16:28:08.339166',NULL),
   ('I03','데이터모델변환구분','Y','2019-08-21 16:07:32.779',NULL,'2021-02-10 16:28:08.339166',NULL),
   ('I04','데이터포맷유형','Y','2019-08-21 16:07:47.964',NULL,'2021-02-10 16:28:08.339166',NULL),
   ('I05','표준데이터모델구분','Y','2019-08-21 16:08:05.276',NULL,'2021-02-10 16:28:08.339166',NULL),
   ('I06','대상데이터모델구분','Y','2019-08-21 16:08:25.895',NULL,'2021-02-10 16:28:08.339166',NULL),
   ('I07','속성타입','Y','2019-08-21 14:33:41.904',NULL,'2021-02-10 16:28:08.339166',NULL),
   ('I08','싱크모듈구분','Y','2019-08-21 16:08:41.738',NULL,'2021-02-10 16:28:08.339166',NULL),
   ('I09','단위','Y','2019-08-21 16:08:53.196',NULL,'2021-02-10 16:28:08.339166',NULL);
INSERT INTO public.comm_type (code_type_id,code_type_nm,use_yn,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('I10','데이터접근모드','Y','2019-08-21 16:09:07.444',NULL,'2021-02-10 16:28:08.339166',NULL),
   ('I11','대상 플랫폼 연계유형','Y',NULL,NULL,'2021-02-10 16:28:08.339166',NULL),
   ('I12','설정방법','Y','2019-08-26 12:21:30.344',NULL,'2021-02-10 16:28:08.339166',NULL),
   ('I13','사용용도','Y','2019-08-26 12:21:43.89',NULL,'2021-02-10 16:28:08.339166',NULL);
   
INSERT INTO public.comm_type (code_type_id,code_type_nm,use_yn,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('S02','교통','Y','2021-06-10 14:17:13','SYSTEM','2021-06-10 14:17:13','SYSTEM'),
   ('S03','안전','Y','2021-06-10 14:17:13','SYSTEM','2021-06-10 14:17:13','SYSTEM'),
   ('S04','환경','Y','2021-06-10 14:17:13','SYSTEM','2021-06-10 14:17:13','SYSTEM'),
   ('S05','에너지','Y','2021-06-10 14:17:13','SYSTEM','2021-06-10 14:17:13','SYSTEM'),
   ('S06','생활/복지','Y','2021-06-10 14:17:13','SYSTEM','2021-06-10 14:17:13','SYSTEM'),
   ('S07','문화','Y','2021-06-10 14:17:13','SYSTEM','2021-06-10 14:17:13','SYSTEM'),
   ('S08','기타','Y','2021-06-10 14:17:13','SYSTEM','2021-06-10 14:17:13','SYSTEM'),
   ('S01','행정','Y','2021-06-10 14:17:13','SYSTEM','2021-06-10 14:17:13','SYSTEM');
  

  
INSERT INTO public.keyword_info (item, item_described, relation_code_id) VALUES ('MODEL_ID', '데이터의 모델 아이디', 'I1110');
INSERT INTO public.keyword_info (item, item_described, relation_code_id) VALUES ('CONN_TERM', '갱신주기(초)', 'I1110');
INSERT INTO public.keyword_info (item, item_described, relation_code_id) VALUES ('INVOKE_CLASS', '수집 변환할 클래스', 'I1110');
INSERT INTO public.keyword_info (item, item_described, relation_code_id) VALUES ('DATASET_ID', '데이터코어의 데이터셋아이디', 'I1110');

INSERT INTO public.keyword_info (item, item_described, relation_code_id) VALUES ('URL_ADDR', NULL, 'I1120');
INSERT INTO public.keyword_info (item, item_described, relation_code_id) VALUES ('TOPIC', 'OneM2M의 토픽', 'I1120');
INSERT INTO public.keyword_info (item, item_described, relation_code_id) VALUES ('REQ_PREFIX', '요청', 'I1120');
INSERT INTO public.keyword_info (item, item_described, relation_code_id) VALUES ('RESP_PREFIX', '응답', 'I1120');
INSERT INTO public.keyword_info (item, item_described, relation_code_id) VALUES ('MODEL_ID', '데이터의 모델 아이디', 'I1120');
INSERT INTO public.keyword_info (item, item_described, relation_code_id) VALUES ('DATASET_ID', '데이터코어의 데이터셋아이디', 'I1120');
INSERT INTO public.keyword_info (item, item_described, relation_code_id) VALUES ('META_INFO', '메타정보', 'I1120');
INSERT INTO public.keyword_info (item, item_described, relation_code_id) VALUES ('INVOKE_CLASS', '수집 변환할 클래스', 'I1120');

INSERT INTO public.keyword_info (item, item_described, relation_code_id) VALUES ('URL_ADDR', NULL, 'I1130');
INSERT INTO public.keyword_info (item, item_described, relation_code_id) VALUES ('MODEL_ID', '데이터의 모델 아이디', 'I1130');
INSERT INTO public.keyword_info (item, item_described, relation_code_id) VALUES ('CONN_TERM', '갱신주기(초)', 'I1130');
INSERT INTO public.keyword_info (item, item_described, relation_code_id) VALUES ('INVOKE_CLASS', '수집 변환할 클래스', 'I1130');
INSERT INTO public.keyword_info (item, item_described, relation_code_id) VALUES ('DATASET_ID', '데이터코어의 데이터셋아이디', 'I1130');

INSERT INTO public.keyword_info (item,item_described,relation_code_id) values ('MODEL_ID','데이터의 모델 아이디','I1140');
INSERT INTO public.keyword_info (item,item_described,relation_code_id) values ('CONN_TERM','갱신주기(초)','I1140');
INSERT INTO public.keyword_info (item,item_described,relation_code_id) values ('INVOKE_CLASS','수집 변환할 클래스','I1140');
INSERT INTO public.keyword_info (item,item_described,relation_code_id) values ('DATASET_ID','데이터코어의 데이터셋아이디','I1140');
INSERT INTO public.keyword_info (item,item_described,relation_code_id) values ('DB_USERNAME','레가시 DB 접속아이디','I1140');
INSERT INTO public.keyword_info (item,item_described,relation_code_id) values ('DB_PASSWORD','레가시 DB 접속 암호','I1140');
INSERT INTO public.keyword_info (item,item_described,relation_code_id) values ('DB_DRIVER_CLASS_NAME','레가시 DB 클래스 네임','I1140');
INSERT INTO public.keyword_info (item,item_described,relation_code_id) values ('DB_JDBC_URL','레가시 JDBC_URL','I1140');

INSERT INTO public.keyword_info (item, item_described, relation_code_id) VALUES ('URL_ADDR', NULL, 'I1150');
INSERT INTO public.keyword_info (item, item_described, relation_code_id) VALUES ('MODEL_ID', '데이터의 모델 아이디', 'I1150');
INSERT INTO public.keyword_info (item, item_described, relation_code_id) VALUES ('CONN_TERM', '갱신주기(초)', 'I1150');
INSERT INTO public.keyword_info (item, item_described, relation_code_id) VALUES ('INVOKE_CLASS', '수집 변환할 클래스', 'I1150');
INSERT INTO public.keyword_info (item, item_described, relation_code_id) VALUES ('DATASET_ID', '데이터코어의 데이터셋아이디', 'I1150');

INSERT INTO public.keyword_info (item, item_described, relation_code_id) VALUES ('URL_ADDR', NULL, 'I1160');
INSERT INTO public.keyword_info (item, item_described, relation_code_id) VALUES ('MODEL_ID', '데이터의 모델 아이디', 'I1160');
INSERT INTO public.keyword_info (item, item_described, relation_code_id) VALUES ('DATASET_ID', '데이터코어의 데이터셋아이디', 'I1160');
INSERT INTO public.keyword_info (item, item_described, relation_code_id) VALUES ('CONN_TERM', '갱신주기(초)', 'I1160');
INSERT INTO public.keyword_info (item, item_described, relation_code_id) VALUES ('INVOKE_CLASS', '수집 변환할 클래스', 'I1160');

INSERT INTO public.keyword_info (item, item_described, relation_code_id) VALUES ('URL_ADDR', NULL, 'I1190');
INSERT INTO public.keyword_info (item, item_described, relation_code_id) VALUES ('MODEL_ID', '데이터의 모델 아이디', 'I1190');
INSERT INTO public.keyword_info (item, item_described, relation_code_id) VALUES ('CONN_TERM', '갱신주기(초)', 'I1190');
INSERT INTO public.keyword_info (item, item_described, relation_code_id) VALUES ('INVOKE_CLASS', '수집 변환할 클래스', 'I1190');
INSERT INTO public.keyword_info (item, item_described, relation_code_id) VALUES ('DATASET_ID', '데이터코어의 데이터셋아이디', 'I1190');

  

INSERT INTO public.adapter_type_info (adapter_type_id,adapter_type_nm,adapter_type_div,use_yn) VALUES ('A000000001','기상청 날씨 예보','I1110','Y'); 
INSERT INTO public.adapter_type_info (adapter_type_id,adapter_type_nm,adapter_type_div,use_yn) VALUES ('A000000002','기상청 날씨 측정','I1110','Y');
INSERT INTO public.adapter_type_info (adapter_type_id,adapter_type_nm,adapter_type_div,use_yn) VALUES ('A000000003','성남시 기상관측','I1110','Y');
INSERT INTO public.adapter_type_info (adapter_type_id,adapter_type_nm,adapter_type_div,use_yn) VALUES ('A000000004','성남시 주차장','I1120','Y'); 
INSERT INTO public.adapter_type_info (adapter_type_id,adapter_type_nm,adapter_type_div,use_yn) VALUES ('A000000005','oneM2M 주차장 정보','I1120','Y'); 


INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000001','MODEL_ID','','Y','','I0701',1,'I1201','Y','I1301','b');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000001','DATASET_ID','','Y','','I0701',3,'I1201','Y','I1301','b');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000001','INVOKE_CLASS','','Y','','I0701',4,'I1201','Y','I1301','b');  
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000001','CONN_TERM','3600','Y','','I0701',5,'I1201','Y','I1301','b');

INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000001','url_addr','','Y','','I0701',6,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000001','ParamVariable','','Y','','I0701',9,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000001','gs1Code','','Y','','I0701',10,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000001','addressCountry','','Y','','I0701',11,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000001','addressRegion','','Y','','I0701',12,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000001','addressTown','','Y','','I0701',13,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000001','addressLocality','','Y','','I0701',14,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000001','streetAddress','','Y','','I0701',15,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000001','location','','Y','','I0701',16,'I1201','Y','I1301','a');


INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000002','MODEL_ID','','Y','','I0701',1,'I1201','Y','I1301','b');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000002','DATASET_ID','','Y','','I0701',3,'I1201','Y','I1301','b');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000002','INVOKE_CLASS','','Y','','I0701',4,'I1201','Y','I1301','b');  
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000002','CONN_TERM','3600','Y','','I0701',5,'I1201','Y','I1301','b');

INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000002','url_addr','','Y','','I0701',6,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000002','ParamVariable','','Y','','I0701',9,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000002','gs1Code','','Y','','I0701',10,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000002','addressCountry','KR','Y','','I0701',11,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000002','addressRegion','','Y','','I0701',12,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000002','addressTown','','Y','','I0701',13,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000002','addressLocality','','Y','','I0701',14,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000002','streetAddress','','Y','','I0701',15,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000002','location','','Y','','I0701',16,'I1201','Y','I1301','a');


INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000003','MODEL_ID','WeatherObserved','Y','성남시 기상측정 모델아이디','I0701',1,'I1201','Y','I1301','b');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000003','DATASET_ID','pocWeatherObserved','Y','성남시 기상측정 데이터셋아이디','I0701',3,'I1201','Y','I1301','b');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000003','INVOKE_CLASS','com.cityhub.adapter.convert.ConvWeatherObserved','Y','성남시 기상측정 변환클래스','I0701',4,'I1201','Y','I1301','b');  
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000003','CONN_TERM','3600','Y','갱신주기','I0701',5,'I1201','Y','I1301','b');

INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000003','url_addr','http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst?dataType=json&numOfRows=1000&serviceKey=xxu2gHkHh5PrWLXUSnk%2BqICJc2%2FwsENQLJnapmbP0S52Jg7FxIFohMk3FfhI5mkp5Dz7ir%2FuocdMHrnGEP9ZBQ%3D%3D&nx=63&ny=124','Y','공공데이터포털의 성남시 기상측정 가져오기','I0701',6,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000003','ParamVariable','base_date,base_time','Y','가변데이터정보','I0701',9,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000003','base_date','yyyyMMdd,MINUTE,-40','Y','','I0701',12,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000003','base_time','HHmm,MINUTE,-40','Y','','I0701',13,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000003','gs1Code','urn:datahub:WeatherObserved:14858','Y','','I0701',14,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000003','addressCountry','KR','Y','','I0701',15,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000003','addressRegion','경기도','Y','','I0701',16,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000003','addressLocality','성남시','Y','','I0701',18,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000003','addressTown','수정구','Y','','I0701',18,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000003','streetAddress','경기도 성남시 수정구 수정로 319','Y','','I0701',19,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000003','location','[127.14858, 37.4557691]','Y','','I0701',20,'I1201','Y','I1301','a');


INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000004','MODEL_ID','OffStreetParking,ParkingSpot','Y','성남시 주차장의 모델아이디','I0701',1,'I1201','Y','I1301','b');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000004','DATASET_ID','pocOffStreetParking,pocParkingSpot','Y','성남시 주차장의 데이터셋아이디','I0701',3,'I1201','Y','I1301','b');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000004','INVOKE_CLASS','com.cityhub.adapter.convert.ConvParkingOneM2M','Y','성남시 주차장의 변환클래스','I0701',4,'I1201','Y','I1301','b');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000004','CONN_TERM','3600','Y','갱신주기','I0701',10,'I1201','Y','I1301','b');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000004','META_INFO','http://203.253.128.164:7579/Mobius/sync_parking_raw','Y','성남시 주차장의 구독정보확인','I0701',9,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000004','REQ_PREFIX','/oneM2M/req/Mobius2/','Y','성남시 주차장의 요청접두사','I0701',6,'I1201','Y','I1301','b');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000004','RESP_PREFIX','/oneM2M/resp/Mobius2/','Y','성남시 주차장의 응답접두사','I0701',7,'I1201','Y','I1301','b');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000004','URL_ADDR','tcp://203.253.128.164:1883','Y','성남시 주차장의 주소','I0701',5,'I1201','Y','I1301','b');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000004','TOPIC','SlotYatopParking','Y','성남시 주차장의 토픽','I0701',8,'I1201','Y','I1301','b');


INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000005','MODEL_ID','','Y','','I0701',1,'I1201','Y','I1301','b');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000005','DATASET_ID','','Y','','I0701',3,'I1201','Y','I1301','b');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000005','INVOKE_CLASS','','Y','','I0701',4,'I1201','Y','I1301','b');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000005','CONN_TERM','3600','Y','','I0701',10,'I1201','Y','I1301','b');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000005','META_INFO','','Y','','I0701',9,'I1201','Y','I1301','b');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000005','REQ_PREFIX','','Y','','I0701',6,'I1201','Y','I1301','b');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000005','RESP_PREFIX','','Y','','I0701',7,'I1201','Y','I1301','b');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000005','TOPIC','','Y','','I0701',8,'I1201','Y','I1301','b');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000005','URL_ADDR','','Y','','I0701',5,'I1201','Y','I1301','b');


