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


CREATE TABLE public.lang_data (
  lang_id serial4 NOT NULL,
  lang_code varchar(2) NOT NULL,
  lang_key varchar(30) NOT NULL,
  lang_value varchar(100) NOT NULL,
  first_create_dt timestamp NULL,
  first_create_id varchar(20) NULL,
  last_update_dt timestamp NULL,
  last_update_id varchar(20) NULL,
  CONSTRAINT lang_data_pkey PRIMARY KEY (lang_id)
);

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



INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','popupDashLog_0021','구분',NULL,NULL,NULL,NULL),
   ('KR','popupDashLog_0022','시작일',NULL,NULL,NULL,NULL),
   ('KR','popupDashLog_0023','종료일',NULL,NULL,NULL,NULL),
   ('KR','adaptor_0001','어댑터등록',NULL,NULL,NULL,NULL),
   ('KR','agentList_0001','Agent 설정',NULL,NULL,NULL,NULL),
   ('KR','agentList_0002','순번',NULL,NULL,NULL,NULL),
   ('KR','agentList_0003','Agent ID',NULL,NULL,NULL,NULL),
   ('KR','popupDashLog_0024','총건수',NULL,NULL,NULL,NULL),
   ('KR','agentList_0004','Agent 명',NULL,NULL,NULL,NULL),
   ('KR','agentList_0005','IP/PORT',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','agentList_0006','어댑터 수',NULL,NULL,NULL,NULL),
   ('KR','agentList_0007','사용여부',NULL,NULL,NULL,NULL),
   ('KR','agentList_0010','신규추가',NULL,NULL,NULL,NULL),
   ('KR','agentList_0008','비고',NULL,NULL,NULL,NULL),
   ('KR','agentList_0009','등록일',NULL,'',NULL,NULL),
   ('KR','navi_0001','모니터링',NULL,NULL,NULL,NULL),
   ('KR','navi_0002','수집통계 및 장애현황',NULL,NULL,NULL,NULL),
   ('KR','navi_0003','Agent 관리',NULL,NULL,NULL,NULL),
   ('KR','navi_0004','Agent',NULL,NULL,NULL,NULL),
   ('KR','navi_0005','Adapter',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','navi_0006','Agent 설정',NULL,NULL,NULL,NULL),
   ('KR','btn_0001','닫기',NULL,NULL,NULL,NULL),
   ('KR','adapterDetail_0013','항목',NULL,NULL,NULL,NULL),
   ('KR','adapterDetail_0014','값 유형',NULL,NULL,NULL,NULL),
   ('KR','adapterDetail_0015','변경가능여부',NULL,NULL,NULL,NULL),
   ('KR','adapterDetail_0016','값',NULL,NULL,NULL,NULL),
   ('KR','adapterDetail_0017','입력방법',NULL,NULL,NULL,NULL),
   ('KR','validation_0001','은(는) 필수 선택입니다.',NULL,NULL,NULL,NULL),
   ('KR','validation_0002','은(는) 적어도 1개 이상은 선택해 주세요.',NULL,NULL,NULL,NULL),
   ('KR','errorMsg_0001','이미 등록된 ID가 있습니다.',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','errorMsg_0002','리소스 식별 실패',NULL,NULL,NULL,NULL),
   ('KR','errorMsg_0003','해당 자원을 참조하는 인스턴스가 존재합니다.',NULL,NULL,NULL,NULL),
   ('KR','popupDashLog_0025','성공건수',NULL,NULL,NULL,NULL),
   ('KR','popupDashLog_0026','실패건수',NULL,NULL,NULL,NULL),
   ('KR','popupDashLog_0027','CARD',NULL,NULL,NULL,NULL),
   ('KR','popupDashLog_0028','MOBILE',NULL,NULL,NULL,NULL),
   ('KR','popupDashLog_0029','년월일(20210101)을 입력하세요',NULL,NULL,NULL,NULL),
   ('KR','popupDashLog_0030','데이터 에러',NULL,NULL,NULL,NULL),
   ('EN','popupDashLog_0021','Division',NULL,NULL,NULL,NULL),
   ('EN','popupDashLog_0022','Start date',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('EN','popupDashLog_0023','End date',NULL,NULL,NULL,NULL),
   ('EN','popupDashLog_0024','Total count',NULL,NULL,NULL,NULL),
   ('EN','popupDashLog_0025','Success count',NULL,NULL,NULL,NULL),
   ('EN','popupDashLog_0026','Failure count',NULL,NULL,NULL,NULL),
   ('EN','popupDashLog_0027','CARD',NULL,NULL,NULL,NULL),
   ('EN','popupDashLog_0028','MOBILE',NULL,NULL,NULL,NULL),
   ('EN','popupDashLog_0029','Enter the year, month and day (20210101)',NULL,NULL,NULL,NULL),
   ('EN','popupDashLog_0030','data error desc',NULL,NULL,NULL,NULL),
   ('EN','dashView_0018','Date Parsing Error',NULL,NULL,NULL,NULL),
   ('EN','dashView_0019','Date Empty Error',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('EN','dashView_0020','Date Range Error',NULL,NULL,NULL,NULL),
   ('EN','dashView_0021','Other error',NULL,NULL,NULL,NULL),
   ('KR','dashView_0018','Date Parsing Error',NULL,NULL,NULL,NULL),
   ('KR','dashView_0019','Date Empty Error',NULL,NULL,NULL,NULL),
   ('KR','dashView_0020','Date Range Error',NULL,NULL,NULL,NULL),
   ('KR','dashView_0021','Other error',NULL,NULL,NULL,NULL),
   ('KR','popupLog_001','로그 상세',NULL,NULL,NULL,NULL),
   ('EN','popupLog_001','Log Detail',NULL,NULL,NULL,NULL),
   ('KR','adapterDetail_0018','필수여부',NULL,NULL,NULL,NULL),
   ('KR','adapterDetail_0019','사용용도',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','adapterDetail_0020','항목설명',NULL,NULL,NULL,NULL),
   ('KR','monitoring_0002','※Agent ID 클릭 : 상세화면 이동',NULL,NULL,NULL,NULL),
   ('KR','monitoring_0003','순번',NULL,NULL,NULL,NULL),
   ('KR','monitoring_0004','Agent ID',NULL,NULL,NULL,NULL),
   ('KR','monitoring_0005','Agent 명',NULL,NULL,NULL,NULL),
   ('KR','monitoring_0006','IP/PORT',NULL,NULL,NULL,NULL),
   ('KR','monitoring_0007','어댑터 수',NULL,NULL,NULL,NULL),
   ('KR','monitoring_0008','사용여부',NULL,NULL,NULL,NULL),
   ('KR','monitoring_0009','비고',NULL,NULL,NULL,NULL),
   ('KR','commDetail_0002','공통코드',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','commDetail_0003','공통코드 상세',NULL,NULL,NULL,NULL),
   ('KR','commDetail_0004','선택',NULL,NULL,NULL,NULL),
   ('KR','commDetail_0005','공통코드',NULL,NULL,NULL,NULL),
   ('KR','commDetail_0006','상세코드명',NULL,NULL,NULL,NULL),
   ('KR','commDetail_0007','사용여부',NULL,NULL,NULL,NULL),
   ('KR','commDetail_0008','대분류코드',NULL,NULL,NULL,NULL),
   ('KR','commDetail_0009','공통코드 대분류 명',NULL,NULL,NULL,NULL),
   ('KR','commDetail_0010','최종 수정일자',NULL,NULL,NULL,NULL),
   ('KR','popupDashLog_0001','로그 조회 설정',NULL,NULL,NULL,NULL),
   ('KR','popupDashLog_0002','어댑터 ID',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','popupDashLog_0003','어댑터 유형',NULL,NULL,NULL,NULL),
   ('KR','adapterDetail_0021','원천 데이터모델 관리 - 목록',NULL,NULL,NULL,NULL),
   ('KR','commList_0008','공통코드 관리 - 목록',NULL,NULL,NULL,NULL),
   ('KR','commDetail_0011','공통코드 관리 - 상세',NULL,NULL,NULL,NULL),
   ('KR','popupDashLog_0020','모니터링',NULL,NULL,NULL,NULL),
   ('KR','obDetail_0020','원천 데이터모델 관리 - 목록',NULL,NULL,NULL,NULL),
   ('KR','popupItemValue_0001','모니터링',NULL,NULL,NULL,NULL),
   ('KR','dashView_0001','수집통계 및 장애현황 ',NULL,NULL,NULL,NULL),
   ('KR','dashView_0002','기준시간',NULL,NULL,NULL,NULL),
   ('KR','dashView_0003','시간대별 수집 현황',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','dashView_0004','어댑터 유형별 건수',NULL,NULL,NULL,NULL),
   ('KR','dashView_0005','어댑터 유형별 성공/실패',NULL,NULL,NULL,NULL),
   ('KR','dashView_0006','실패 유형별 %',NULL,NULL,NULL,NULL),
   ('KR','dashView_0007','로그보기',NULL,NULL,NULL,NULL),
   ('KR','dashView_0008','수집시도',NULL,NULL,NULL,NULL),
   ('KR','dashView_0009','건',NULL,NULL,NULL,NULL),
   ('KR','dashView_0010','성공',NULL,NULL,NULL,NULL),
   ('KR','dashView_0011','실패',NULL,NULL,NULL,NULL),
   ('KR','dashView_0012','9001:데이터 변환 실패',NULL,NULL,NULL,NULL),
   ('KR','msg_0040','데이터가 존재하지 않습니다.',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','validation_0003','은(는) 필수입력입니다.',NULL,NULL,NULL,NULL),
   ('KR','main_0001','알림메세지',NULL,NULL,NULL,NULL),
   ('KR','main_0002','Smart City Hub',NULL,NULL,NULL,NULL),
   ('KR','main_0003','주메뉴',NULL,NULL,NULL,NULL),
   ('KR','main_0004','환영합니다.',NULL,NULL,NULL,NULL),
   ('KR','main_0005','Agent 관리',NULL,NULL,NULL,NULL),
   ('KR','main_0006','Agent 설정',NULL,NULL,NULL,NULL),
   ('KR','main_0007','원천 데이터모델 관리',NULL,NULL,NULL,NULL),
   ('KR','main_0008','원천 데이터모델 목록',NULL,NULL,NULL,NULL),
   ('KR','main_0009','Adapter 유형 관리',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','main_0010','Adapter 유형별 파라미터 관리',NULL,NULL,NULL,NULL),
   ('KR','main_0011','oneM2M 구독관리',NULL,NULL,NULL,NULL),
   ('KR','main_0012','시스템관리',NULL,NULL,NULL,NULL),
   ('KR','monitoring_0011','모니터링',NULL,NULL,NULL,NULL),
   ('KR','popupMonitorAdapter_0001','모니터링',NULL,NULL,NULL,NULL),
   ('KR','agentList_0014','Agent 관리 - 목록',NULL,NULL,NULL,NULL),
   ('KR','agentDetail_0020','Agent 관리 - 추가 등록 / 변경',NULL,NULL,NULL,NULL),
   ('KR','instanceDetail_0030','Instance 관리 - 추가 등록 / 변경',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_01_0020','1단계 - 원천 데이터모델 선택',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_02_0019','2단계 - 표준 데이터모델 선택',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','dataConvert_03_0021','3단계 - 반복 호출 설정',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_04_0017','데이터 변환 설정',NULL,NULL,NULL,NULL),
   ('KR','navi_0007','Agent 목록',NULL,NULL,NULL,NULL),
   ('KR','navi_0008','Agent 상세',NULL,NULL,NULL,NULL),
   ('KR','navi_0009','Instance 관리',NULL,NULL,NULL,NULL),
   ('KR','navi_0010','데이터모델 변환 관리',NULL,NULL,NULL,NULL),
   ('KR','navi_0011','원천 데이터 모델 선택',NULL,NULL,NULL,NULL),
   ('KR','navi_0012','표준 데이터 모델 선택',NULL,NULL,NULL,NULL),
   ('KR','navi_0013','반복 호출 설정',NULL,NULL,NULL,NULL),
   ('KR','navi_0014','변환 클래스 작성',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','navi_0015','원천 데이터모델 관리',NULL,NULL,NULL,NULL),
   ('KR','navi_0016','원천 데이터모델 목록',NULL,NULL,NULL,NULL),
   ('KR','navi_0017','원천 데이터모델 상세',NULL,NULL,NULL,NULL),
   ('KR','navi_0018','Adapter 유형 관리',NULL,NULL,NULL,NULL),
   ('KR','navi_0019','Adapter 유형별 파라미터 관리',NULL,NULL,NULL,NULL),
   ('KR','navi_0020','oneM2M 구독관리',NULL,NULL,NULL,NULL),
   ('KR','navi_0021','시스템관리',NULL,NULL,NULL,NULL),
   ('KR','navi_0022','공통코드 관리',NULL,NULL,NULL,NULL),
   ('KR','navi_0023','공통코드 상세',NULL,NULL,NULL,NULL),
   ('KR','msg_0001','성공',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','msg_0002','실패',NULL,NULL,NULL,NULL),
   ('KR','msg_0003','오류 : 채우고자 하는 문자열이 요청 길이보다 큽니다',NULL,NULL,NULL,NULL),
   ('KR','msg_0004','모니터링 로그팝업',NULL,NULL,NULL,NULL),
   ('KR','msg_0005','사용',NULL,NULL,NULL,NULL),
   ('KR','msg_0035','공통코드 목록을 저장 후 추가해 주세요',NULL,NULL,NULL,NULL),
   ('KR','select_0015','선택',NULL,NULL,NULL,NULL),
   ('KR','select_0016','사용',NULL,NULL,NULL,NULL),
   ('KR','select_0017','미사용',NULL,NULL,NULL,NULL),
   ('KR','select_0018','가능',NULL,NULL,NULL,NULL),
   ('KR','select_0019','불가능',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','select_0020','필수',NULL,NULL,NULL,NULL),
   ('KR','text_0001','검색어를 입력하세요.',NULL,NULL,NULL,NULL),
   ('KR','text_0002','사용',NULL,NULL,NULL,NULL),
   ('KR','text_0003','사용안함',NULL,NULL,NULL,NULL),
   ('KR','text_0004','변환',NULL,NULL,NULL,NULL),
   ('KR','text_0005','미변환',NULL,NULL,NULL,NULL),
   ('KR','text_0006','선택한 항목에 대하여',NULL,NULL,NULL,NULL),
   ('KR','text_0007','개의 반복 설정을',NULL,NULL,NULL,NULL),
   ('KR','text_0008','항목값',NULL,NULL,NULL,NULL),
   ('KR','text_0009','번째 반복 항목 작성',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','obDetail_0012','원천 데이터모델 상세정보',NULL,NULL,NULL,NULL),
   ('KR','obDetail_0013','선택',NULL,NULL,NULL,NULL),
   ('KR','obDetail_0014','경로',NULL,NULL,NULL,NULL),
   ('KR','obDetail_0015','데이터구조',NULL,NULL,NULL,NULL),
   ('KR','obDetail_0016','항목명(영문)',NULL,NULL,NULL,NULL),
   ('KR','obDetail_0017','항목명(국문)',NULL,NULL,NULL,NULL),
   ('KR','obDetail_0018','항목구분',NULL,NULL,NULL,NULL),
   ('KR','obDetail_0019','항목설명',NULL,NULL,NULL,NULL),
   ('KR','adapterDetail_0001','어댑터 유형별 파라미터 관리',NULL,NULL,NULL,NULL),
   ('KR','adapterDetail_0002','어댑터 유형 관리',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','adapterDetail_0003','순번',NULL,NULL,NULL,NULL),
   ('KR','adapterDetail_0004','어댑터 유형 ID',NULL,NULL,NULL,NULL),
   ('KR','monitoring_0010','등록일',NULL,NULL,NULL,NULL),
   ('KR','monitoring_adapter_0001','모니터링',NULL,NULL,NULL,NULL),
   ('KR','monitoring_adapter_0002','Agent 상세정보',NULL,NULL,NULL,NULL),
   ('KR','monitoring_adapter_0004','Agent 명',NULL,NULL,NULL,NULL),
   ('KR','monitoring_adapter_0005','IP',NULL,NULL,NULL,NULL),
   ('KR','monitoring_adapter_0006','Port',NULL,NULL,NULL,NULL),
   ('KR','monitoring_adapter_0007','상세목록',NULL,NULL,NULL,NULL),
   ('KR','monitoring_adapter_0008','순번',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','agentDetail_0015','인스턴스 수',NULL,NULL,NULL,NULL),
   ('KR','agentDetail_0016','Adaptor 기본정보 추가 등록 / 변경',NULL,NULL,NULL,NULL),
   ('KR','agentDetail_0017','Adaptor ID',NULL,NULL,NULL,NULL),
   ('KR','agentDetail_0018','Adaptor 명',NULL,NULL,NULL,NULL),
   ('KR','agentDetail_0019','Platform 유형',NULL,NULL,NULL,NULL),
   ('KR','instanceDetail_0001','Instance 관리',NULL,NULL,NULL,NULL),
   ('KR','instanceDetail_0002','Adaptor 정보',NULL,NULL,NULL,NULL),
   ('KR','instanceDetail_0003','Adaptor ID',NULL,NULL,NULL,NULL),
   ('KR','instanceDetail_0004','Adaptor 명',NULL,NULL,NULL,NULL),
   ('KR','instanceDetail_0005','어댑터 유형',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','instanceDetail_0006','사용여부',NULL,NULL,NULL,NULL),
   ('KR','instanceDetail_0007','인스턴스 수',NULL,NULL,NULL,NULL),
   ('KR','instanceDetail_0008','Instance 목록',NULL,NULL,NULL,NULL),
   ('KR','instanceDetail_0009','순번',NULL,NULL,NULL,NULL),
   ('KR','main_0013','공통코드 관리',NULL,NULL,NULL,NULL),
   ('KR','main_0014','본문',NULL,NULL,NULL,NULL),
   ('KR','main_0015','주메뉴 확장/축소 하기',NULL,NULL,NULL,NULL),
   ('KR','main_0016','관리자',NULL,NULL,NULL,NULL),
   ('KR','main_0017','Home',NULL,NULL,NULL,NULL),
   ('KR','main_0018','주소(04513) 서울시 송파구 잠실동',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','main_0019','대표번호 : 031-0000-0000    고객센터 : 02-0000-0000 (월~금 09:00 ~ 18:00, 공휴일 제외)',NULL,NULL,NULL,NULL),
   ('KR','main_0020','Copyright©Datahub. All rights reserved.',NULL,NULL,NULL,NULL),
   ('KR','main_0021','알림창',NULL,NULL,NULL,NULL),
   ('KR','main_0022','최소화',NULL,NULL,NULL,NULL),
   ('KR','main_0023','신청서가 있습니다.',NULL,NULL,NULL,NULL),
   ('KR','fileUpload_0001','대상자 데이터 등록',NULL,NULL,NULL,NULL),
   ('KR','fileUpload_0002','요청 ID가 유효하지 않습니다.',NULL,NULL,NULL,NULL),
   ('KR','fileUpload_0003','확진자 ID가 유효하지 않습니다.',NULL,NULL,NULL,NULL),
   ('KR','fileUpload_0004','데이터 유형을 선택해 주세요.',NULL,NULL,NULL,NULL),
   ('KR','fileUpload_0005','통신사를 선택해 주세요.',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','fileUpload_0006','업로드할 파일을 선택해 주세요.',NULL,NULL,NULL,NULL),
   ('KR','fileUpload_0007','대상자 데이터 등록',NULL,NULL,NULL,NULL),
   ('KR','fileUpload_0008','* 등록된 데이터는 검증후에 등록완료 됩니다.',NULL,NULL,NULL,NULL),
   ('KR','fileUpload_0009','요청서 ID',NULL,NULL,NULL,NULL),
   ('KR','fileUpload_0010','대상자 ID',NULL,NULL,NULL,NULL),
   ('KR','fileUpload_0011','대상자명',NULL,NULL,NULL,NULL),
   ('KR','fileUpload_0012','데이터 유형',NULL,NULL,NULL,NULL),
   ('KR','fileUpload_0013','이동경로(통신사)',NULL,NULL,NULL,NULL),
   ('KR','fileUpload_0014','카드사용내역',NULL,NULL,NULL,NULL),
   ('KR','fileUpload_0015','통신사',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','fileUpload_0016','통합서식',NULL,NULL,NULL,NULL),
   ('KR','fileUpload_0017','SKT',NULL,NULL,NULL,NULL),
   ('KR','fileUpload_0018','KT',NULL,NULL,NULL,NULL),
   ('KR','fileUpload_0019','LG(구)',NULL,NULL,NULL,NULL),
   ('KR','fileUpload_0020','LG(신)',NULL,NULL,NULL,NULL),
   ('KR','fileUpload_0021','파일명',NULL,NULL,NULL,NULL),
   ('KR','fileUpload_0022','파일선택',NULL,NULL,NULL,NULL),
   ('KR','fileUpload_0023','json 파싱과정에서 오류가 발생했습니다.',NULL,NULL,NULL,NULL),
   ('KR','fileUpload_0024','파일안에 요청하지 않은 확진자가 존재합니다',NULL,NULL,NULL,NULL),
   ('KR','fileUpload_0025','파일',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','fileUpload_0026','오류내용',NULL,NULL,NULL,NULL),
   ('KR','popupDashLog_0006','LegacyPlatform',NULL,NULL,NULL,NULL),
   ('KR','popupDashLog_0007','기타',NULL,NULL,NULL,NULL),
   ('KR','popupDashLog_0008','표준모델',NULL,NULL,NULL,NULL),
   ('KR','popupDashLog_0009','진행단계',NULL,NULL,NULL,NULL),
   ('KR','fileUpload_0027','업로드 가능한 파일은 엑셀  (확장자 : xlsx , xls , csv) 입니다.',NULL,NULL,NULL,NULL),
   ('KR','fileUpload_0028','빈  제목',NULL,NULL,NULL,NULL),
   ('KR','msg_0036','최종 수정일자',NULL,NULL,NULL,NULL),
   ('KR','btn_0002','조회',NULL,NULL,NULL,NULL),
   ('KR','btn_0003','시작',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','btn_0004','재시작',NULL,NULL,NULL,NULL),
   ('KR','btn_0005','종료',NULL,NULL,NULL,NULL),
   ('KR','popupDashLog_0010','로그 내용',NULL,NULL,NULL,NULL),
   ('KR','btn_0006','어댑터 시작',NULL,NULL,NULL,NULL),
   ('KR','btn_0007','어댑터 종료',NULL,NULL,NULL,NULL),
   ('KR','btn_0008','어댑터 재시작',NULL,NULL,NULL,NULL),
   ('KR','btn_0009','로그 시작',NULL,NULL,NULL,NULL),
   ('KR','text_0010','소스코드 첨가부분 - 시작',NULL,NULL,NULL,NULL),
   ('KR','text_0011','소스코드 첨가부분 - 종료',NULL,NULL,NULL,NULL),
   ('KR','msg_0020','첫번째 값, 두번째 값, 세번째 값('',''로 구분해서 세개의 데이터를 입력 하세요)',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','obList_0001','원천 데이터모델 관리',NULL,NULL,NULL,NULL),
   ('KR','obList_0002','순번',NULL,NULL,NULL,NULL),
   ('KR','obList_0003','Source Model ID',NULL,NULL,NULL,NULL),
   ('KR','obList_0004','Source Model 명',NULL,NULL,NULL,NULL),
   ('KR','obList_0005','데이터 유형',NULL,NULL,NULL,NULL),
   ('KR','obList_0006','등록일',NULL,NULL,NULL,NULL),
   ('KR','obList_0007','비고',NULL,NULL,NULL,NULL),
   ('KR','obList_0008','사용여부',NULL,NULL,NULL,NULL),
   ('KR','obDetail_0001','원천 데이터모델 관리',NULL,NULL,NULL,NULL),
   ('KR','obDetail_0002','원천 데이터모델 기본정보',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','obDetail_0003','Source Model ID',NULL,NULL,NULL,NULL),
   ('KR','obDetail_0004','사용여부',NULL,NULL,NULL,NULL),
   ('KR','obDetail_0005','사용',NULL,NULL,NULL,NULL),
   ('KR','monitoring_adapter_0003','Agent ID',NULL,NULL,NULL,NULL),
   ('KR','monitoring_adapter_0009','Adaptor 명',NULL,NULL,NULL,NULL),
   ('KR','monitoring_adapter_0010','시작/종료',NULL,NULL,NULL,NULL),
   ('KR','monitoring_adapter_0011','인스턴스 수',NULL,NULL,NULL,NULL),
   ('KR','monitoring_adapter_0012','상태',NULL,NULL,NULL,NULL),
   ('KR','monitoring_adapter_0013','Messages in / hour',NULL,NULL,NULL,NULL),
   ('KR','monitoring_adapter_0014','Messages out / hour',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','monitoring_adapter_0015','Bytes in / hour',NULL,NULL,NULL,NULL),
   ('KR','monitoring_adapter_0016','Bytes out / hour',NULL,NULL,NULL,NULL),
   ('KR','monitoring_adapter_0017','Failure Messages / hour',NULL,NULL,NULL,NULL),
   ('KR','agentDetail_0001','Agent 관리',NULL,NULL,NULL,NULL),
   ('KR','agentDetail_0002','Agent 추가 등록/변경',NULL,NULL,NULL,NULL),
   ('KR','instanceDetail_0010','Instance ID',NULL,NULL,NULL,NULL),
   ('KR','instanceDetail_0011','Instance 명',NULL,NULL,NULL,NULL),
   ('KR','instanceDetail_0012','인스턴스 상세정보 추가 등록 / 변경',NULL,NULL,NULL,NULL),
   ('KR','instanceDetail_0013','인스턴스 명',NULL,NULL,NULL,NULL),
   ('KR','instanceDetail_0014','이미지속성 추출',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','instanceDetail_0015','동영상속성 추출',NULL,NULL,NULL,NULL),
   ('KR','instanceDetail_0016','데이터모델 변환',NULL,NULL,NULL,NULL),
   ('KR','instanceDetail_0017','변환',NULL,NULL,NULL,NULL),
   ('KR','instanceDetail_0018','미변환',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_01_0005','원천 데이터 모델',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_01_0006','표준 데이터 모델',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_01_0007','원천 데이터 모델 선택',NULL,NULL,NULL,NULL),
   ('KR','popupDashLog_0011','로그 목록',NULL,NULL,NULL,NULL),
   ('KR','popupDashLog_0012','순번',NULL,NULL,NULL,NULL),
   ('KR','popupDashLog_0013','로그발생일시',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','popupDashLog_0014','어댑터 ID',NULL,NULL,NULL,NULL),
   ('KR','popupDashLog_0017','표준모델',NULL,NULL,NULL,NULL),
   ('KR','popupDashLog_0018','Byte',NULL,NULL,NULL,NULL),
   ('KR','popupDashLog_0019','로그내용',NULL,NULL,NULL,NULL),
   ('KR','monitoring_adapter_0018','Adaptor ID',NULL,NULL,NULL,NULL),
   ('KR','monitoring_adapter_0019','어댑터 유형',NULL,NULL,NULL,NULL),
   ('KR','obList_0009','원천 데이터모델 관리 - 목록',NULL,NULL,NULL,NULL),
   ('KR','dashView_0013','9002:데이터 모델 없음',NULL,NULL,NULL,NULL),
   ('KR','dashView_0014','9003:소켓 연결 실패',NULL,NULL,NULL,NULL),
   ('KR','dashView_0015','9800:일반에러',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','dashView_0016','9900:시스템에러',NULL,NULL,NULL,NULL),
   ('KR','dashView_0017','모니터링',NULL,NULL,NULL,NULL),
   ('KR','btn_0027','모니터링',NULL,NULL,NULL,NULL),
   ('KR','fileUpload_0029','컬럼',NULL,NULL,NULL,NULL),
   ('KR','fileUpload_0030','필수값입니다.',NULL,NULL,NULL,NULL),
   ('KR','fileUpload_0031','요청일자에서 벗어난 데이터입니다.',NULL,NULL,NULL,NULL),
   ('KR','fileUpload_0032','올바른 날짜 형식이 아닙니다.',NULL,NULL,NULL,NULL),
   ('KR','fileUpload_0033','파일의 엑셀서식에 오류가 있습니다.',NULL,NULL,NULL,NULL),
   ('KR','fileUpload_0034','파일 업로드 과정에서 오류가 발생하였습니다.',NULL,NULL,NULL,NULL),
   ('KR','fileUpload_0035','그래도 계속 진행하시려면 [확인] 버튼을 누르세요.',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','fileUpload_0036','빈 파일입니다.',NULL,NULL,NULL,NULL),
   ('KR','fileUpload_0037','파일이 업로드 되었습니다.',NULL,NULL,NULL,NULL),
   ('KR','fileUpload_0038','업로드 과정에서 오류가 발생하였습니다.',NULL,NULL,NULL,NULL),
   ('KR','fileUpload_0039','논리명칭',NULL,NULL,NULL,NULL),
   ('KR','fileUpload_0040','물리명칭',NULL,NULL,NULL,NULL),
   ('KR','fileUpload_0041','필수여부',NULL,NULL,NULL,NULL),
   ('KR','fileUpload_0042','행',NULL,NULL,NULL,NULL),
   ('EN','fileUpload_0001','Register target data',NULL,NULL,NULL,NULL),
   ('EN','fileUpload_0002','Request ID is not valid.',NULL,NULL,NULL,NULL),
   ('EN','fileUpload_0003','Confirmed ID is not valid.',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('EN','fileUpload_0004','Please select a data type.',NULL,NULL,NULL,NULL),
   ('EN','fileUpload_0005','Please select a telecommunications company.',NULL,NULL,NULL,NULL),
   ('EN','fileUpload_0006','Please select a file to upload.',NULL,NULL,NULL,NULL),
   ('EN','fileUpload_0007','The registration data.',NULL,NULL,NULL,NULL),
   ('EN','fileUpload_0008','* Registered data is registered after verification.',NULL,NULL,NULL,NULL),
   ('EN','fileUpload_0009','Information provision request ID',NULL,NULL,NULL,NULL),
   ('EN','fileUpload_0010','Target ID',NULL,NULL,NULL,NULL),
   ('EN','fileUpload_0011','Target name',NULL,NULL,NULL,NULL),
   ('EN','fileUpload_0012','Type of Data',NULL,NULL,NULL,NULL),
   ('KR','btn_0021','검색',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','btn_0022','증가',NULL,NULL,NULL,NULL),
   ('KR','btn_0023','감소',NULL,NULL,NULL,NULL),
   ('KR','btn_0024','컴파일 확인',NULL,NULL,NULL,NULL),
   ('KR','btn_0025','json 가져오기',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_02_0001','2/3 단계 - 표준 데이터모델 선택',NULL,NULL,NULL,NULL),
   ('EN','fileUpload_0013','a moving route(Telecommunications company)',NULL,NULL,NULL,NULL),
   ('EN','fileUpload_0014','Card usage details',NULL,NULL,NULL,NULL),
   ('EN','fileUpload_0015','Telecommunications company',NULL,NULL,NULL,NULL),
   ('EN','fileUpload_0016','Integrated form',NULL,NULL,NULL,NULL),
   ('EN','fileUpload_0017','SKT',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','btn_0026','적용',NULL,NULL,NULL,NULL),
   ('EN','fileUpload_0018','KT',NULL,NULL,NULL,NULL),
   ('EN','fileUpload_0019','LG(OLD)',NULL,NULL,NULL,NULL),
   ('EN','fileUpload_0020','LG(NEW)',NULL,NULL,NULL,NULL),
   ('EN','fileUpload_0021','Filename',NULL,NULL,NULL,NULL),
   ('EN','fileUpload_0022','Select File ',NULL,NULL,NULL,NULL),
   ('EN','fileUpload_0023','An error occurred during json parsing.',NULL,NULL,NULL,NULL),
   ('KR','select_0001','시간대별 수집 현황',NULL,NULL,NULL,NULL),
   ('KR','select_0002','어댑터 유형별 건수',NULL,NULL,NULL,NULL),
   ('KR','select_0003','어댑터 유형별 성공/실패',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','select_0004','실패 유형별 %',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_01_0009','변환 클래스 작성',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_01_0010','원천 데이터모델 검색',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_01_0011','제목',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_01_0012','원천 데이터모델 검색 결과',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_01_0013','선택',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_01_0014','순번',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_01_0015','Source Model ID',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_01_0016','Source Model 명',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_01_0017','데이터 유형',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','dataConvert_01_0018','등록일',NULL,NULL,NULL,NULL),
   ('EN','fileUpload_0024','Unrequested authenticator exists in the file',NULL,NULL,NULL,NULL),
   ('KR','btn_0010','로그 중지',NULL,NULL,NULL,NULL),
   ('KR','btn_0011','clear',NULL,NULL,NULL,NULL),
   ('KR','btn_0012','신규추가',NULL,NULL,NULL,NULL),
   ('KR','btn_0013','삭제',NULL,NULL,NULL,NULL),
   ('KR','btn_0014','수정',NULL,NULL,NULL,NULL),
   ('KR','btn_0015','저장',NULL,NULL,NULL,NULL),
   ('KR','btn_0016','목록',NULL,NULL,NULL,NULL),
   ('KR','btn_0017','데이터모델 변환 관리',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','btn_0018','추가',NULL,NULL,NULL,NULL),
   ('KR','btn_0019','저장하고 다음단계로',NULL,NULL,NULL,NULL),
   ('KR','btn_0020','다음단계로',NULL,NULL,NULL,NULL),
   ('KR','select_0005','[2001] 데이터를 받았습니다.',NULL,NULL,NULL,NULL),
   ('KR','select_0006','[2002] 데이터 변환 요청 중입니다.',NULL,NULL,NULL,NULL),
   ('KR','select_0007','[2003] 데이터를 변환을 성공했습니다.',NULL,NULL,NULL,NULL),
   ('KR','select_0008','[2004] 데이터 적재를 요청합니다.',NULL,NULL,NULL,NULL),
   ('KR','select_0009','[2005]데이터를 적재했습니다.',NULL,NULL,NULL,NULL),
   ('KR','select_0010','[9001] 데이터를 변환을 실패했습니다.',NULL,NULL,NULL,NULL),
   ('KR','select_0011','[9002] 데이터 모델이 없습니다.',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','select_0012','[9003] 소켓 연결에 실패하였습니다.',NULL,NULL,NULL,NULL),
   ('KR','select_0013','[9800] 일반에러',NULL,NULL,NULL,NULL),
   ('EN','fileUpload_0025','File',NULL,NULL,NULL,NULL),
   ('EN','fileUpload_0026','Error content',NULL,NULL,NULL,NULL),
   ('EN','fileUpload_0027','Uploadable files are Excel (extensions: xlsx, xls, csv).',NULL,NULL,NULL,NULL),
   ('EN','fileUpload_0028','Empty Title',NULL,NULL,NULL,NULL),
   ('EN','fileUpload_0029','Column',NULL,NULL,NULL,NULL),
   ('KR','select_0014','[9900] 시스템에러',NULL,NULL,NULL,NULL),
   ('KR','obDetail_0006','사용안함',NULL,NULL,NULL,NULL),
   ('KR','obDetail_0007','Source Model 명',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','obDetail_0008','비고',NULL,NULL,NULL,NULL),
   ('KR','obDetail_0009','데이터포맷',NULL,NULL,NULL,NULL),
   ('KR','obDetail_0010','JSON',NULL,NULL,NULL,NULL),
   ('KR','obDetail_0011','XML',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_01_0019','사용여부',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_02_0002','데이터모델 변환 기본 정보',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_02_0003','Instance ID',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_02_0004','Instance 명',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_02_0005','원천 데이터 모델',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_02_0006','표준 데이터 모델',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','dataConvert_02_0007','원천 데이터 모델 선택',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_02_0008','표준 데이터 모델 선택',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_02_0009','변환 클래스 작성',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_02_0010','표준 데이터모델 검색',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_02_0011','제목',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_02_0012','표준 데이터모델 검색 결과',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_02_0013','선택',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_02_0014','순번',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_02_0015','표준 Model ID',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_02_0016','표준 Model 명',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','dataConvert_02_0017','등록일',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_03_0002','데이터모델 변환 기본 정보',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_03_0003','Instance ID',NULL,NULL,NULL,NULL),
   ('KR','commList_0004','대분류 코드',NULL,NULL,NULL,NULL),
   ('KR','commList_0005','공통코드 대분류 명',NULL,NULL,NULL,NULL),
   ('EN','fileUpload_0030','Required value.',NULL,NULL,NULL,NULL),
   ('EN','fileUpload_0031','Data out of date of request.',NULL,NULL,NULL,NULL),
   ('EN','fileUpload_0032','This is not a valid date format.',NULL,NULL,NULL,NULL),
   ('EN','fileUpload_0033','There is an error in the Excel format of the file.',NULL,NULL,NULL,NULL),
   ('KR','commList_0006','사용여부',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','commList_0007','신규 추가',NULL,NULL,NULL,NULL),
   ('KR','agentList_0013','테스트2',NULL,NULL,NULL,NULL),
   ('KR','commDetail_0001','시스템 관리',NULL,NULL,NULL,NULL),
   ('KR','popupDashLog_0016','진행단계',NULL,NULL,NULL,NULL),
   ('EN','fileUpload_0034','There was an error uploading the file.',NULL,NULL,NULL,NULL),
   ('EN','fileUpload_0035','If you still want to proceed, press the OK button.',NULL,NULL,NULL,NULL),
   ('EN','fileUpload_0036','This file is empty.',NULL,NULL,NULL,NULL),
   ('EN','fileUpload_0037','Your file has been uploaded.',NULL,NULL,NULL,NULL),
   ('EN','fileUpload_0038','An error occurred during the upload process.',NULL,NULL,NULL,NULL),
   ('EN','fileUpload_0039','Logical name',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('EN','fileUpload_0040','Physical name',NULL,NULL,NULL,NULL),
   ('KR','msg_0006','미사용',NULL,NULL,NULL,NULL),
   ('KR','msg_0007','삭제하였습니다.',NULL,NULL,NULL,NULL),
   ('KR','msg_0008','어댑터가 존재하는 에이전트는 삭제할 수 없습니다.',NULL,NULL,NULL,NULL),
   ('KR','msg_0009','인스턴스가 존재하는 어댑터는 삭제할 수 없습니다.',NULL,NULL,NULL,NULL),
   ('KR','msg_0010','삭제할 어댑터를 선택해 주세요.',NULL,NULL,NULL,NULL),
   ('KR','msg_0011','삭제하시겠습니까?',NULL,NULL,NULL,NULL),
   ('KR','msg_0012','저장되었습니다.',NULL,NULL,NULL,NULL),
   ('KR','msg_0013','저장하였습니다.',NULL,NULL,NULL,NULL),
   ('KR','msg_0014','수정할 어댑터를 선택해 주세요.',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','msg_0015','수정하시겠습니까?',NULL,NULL,NULL,NULL),
   ('KR','msg_0016','저장하시겠습니까?',NULL,NULL,NULL,NULL),
   ('KR','msg_0017','항목 값 설정',NULL,NULL,NULL,NULL),
   ('KR','msg_0018','작동중인 인스턴스는 삭제할 수 없습니다.',NULL,NULL,NULL,NULL),
   ('KR','msg_0021','에러에러',NULL,NULL,NULL,NULL),
   ('KR','msg_0022','클릭하시면 팝업창이 나타납니다.',NULL,NULL,NULL,NULL),
   ('KR','msg_0023','세부항목에 수정된 데이터가 있습니다.\n저장하지 않은 데이터는 초기화됩니다.\n진행 하시겠습니까?',NULL,NULL,NULL,NULL),
   ('EN','fileUpload_0041','Required status',NULL,NULL,NULL,NULL),
   ('EN','fileUpload_0042','Row',NULL,NULL,NULL,NULL),
   ('KR','msg_0024','값 유형이 string인 경우 첫번째 값은 숫자가 들어올 수 없습니다.',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','msg_0025','어댑터 유형을 저장 후 추가해 주세요',NULL,NULL,NULL,NULL),
   ('KR','msg_0026','신규 어댑터 유형을 저장해 주세요',NULL,NULL,NULL,NULL),
   ('KR','msg_0027','세부항목',NULL,NULL,NULL,NULL),
   ('KR','msg_0028','정보 가져오기를 해주세요.',NULL,NULL,NULL,NULL),
   ('KR','msg_0029','구독할 컨테이너를 선택해 주세요.',NULL,NULL,NULL,NULL),
   ('KR','msg_0030','구독하시겠습니까?',NULL,NULL,NULL,NULL),
   ('KR','msg_0031','공통코드항목에 수정된 데이터가 있습니다.\n저장하지 않은 데이터는 초기화됩니다.\n진행 하시겠습니까?',NULL,NULL,NULL,NULL),
   ('KR','adapterDetail_0005','대상 플랫폼 연계 유형',NULL,NULL,NULL,NULL),
   ('KR','adapterDetail_0006','어댑터 유형',NULL,NULL,NULL,NULL),
   ('KR','adapterDetail_0007','사용여부',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','adapterDetail_0008','신규 추가',NULL,NULL,NULL,NULL),
   ('KR','adapterDetail_0009','저장시 자동으로 생성됩니다.',NULL,NULL,NULL,NULL),
   ('KR','adapterDetail_0010','어댑터 파라미터 관리',NULL,NULL,NULL,NULL),
   ('KR','adapterDetail_0011','선택',NULL,NULL,NULL,NULL),
   ('KR','adapterDetail_0012','순번',NULL,NULL,NULL,NULL),
   ('KR','monitoring_0001','Agent관리',NULL,NULL,NULL,NULL),
   ('KR','agentDetail_0003','Agent ID',NULL,NULL,NULL,NULL),
   ('KR','agentDetail_0004','Agent 명',NULL,NULL,NULL,NULL),
   ('KR','agentDetail_0005','IP',NULL,NULL,NULL,NULL),
   ('KR','agentDetail_0006','Port',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','agentDetail_0007','사용여부',NULL,NULL,NULL,NULL),
   ('KR','agentDetail_0008','비고',NULL,NULL,NULL,NULL),
   ('KR','agentDetail_0009','Adaptor 추가 등록 / 변경',NULL,NULL,NULL,NULL),
   ('KR','agentDetail_0010','선택',NULL,NULL,NULL,NULL),
   ('KR','agentDetail_0011','순번',NULL,NULL,NULL,NULL),
   ('KR','agentDetail_0012','Adaptor ID',NULL,NULL,NULL,NULL),
   ('KR','agentDetail_0013','Adaptor 명',NULL,NULL,NULL,NULL),
   ('KR','agentDetail_0014','Platform 유형',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_01_0008','표준 데이터 모델 선택',NULL,NULL,NULL,NULL),
   ('KR','instanceDetail_0029','사용자지정',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','text_0012','컴파일에 성공했습니다.',NULL,NULL,NULL,NULL),
   ('KR','msg_0032','대분류코드',NULL,NULL,NULL,NULL),
   ('KR','msg_0033','공통코드 대분류 명',NULL,NULL,NULL,NULL),
   ('KR','msg_0034','사용여부',NULL,NULL,NULL,NULL),
   ('KR','instanceDetail_0019','Adaptor 유형',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_01_0001','1/3 단계 - 원천 데이터모델 선택',NULL,NULL,NULL,NULL),
   ('KR','instanceDetail_0020','비고',NULL,NULL,NULL,NULL),
   ('KR','instanceDetail_0021','인스턴스 추가 옵션 등록 / 변경',NULL,NULL,NULL,NULL),
   ('KR','instanceDetail_0022','선택',NULL,NULL,NULL,NULL),
   ('KR','instanceDetail_0023','필수여부',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','dataConvert_04_0006','표준 데이터 모델',NULL,NULL,NULL,NULL),
   ('KR','instanceDetail_0024','값 유형  ',NULL,NULL,NULL,NULL),
   ('KR','instanceDetail_0025','항목명(key)',NULL,NULL,NULL,NULL),
   ('KR','instanceDetail_0026','변경여부',NULL,NULL,NULL,NULL),
   ('KR','instanceDetail_0027','항목값(value)  ',NULL,NULL,NULL,NULL),
   ('KR','instanceDetail_0028','항목설명',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_01_0002','데이터모델 변환 기본 정보',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_01_0003','Instance ID',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_01_0004','Instance 명',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_02_0018','사용여부',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','dataConvert_03_0001','3/4 단계 - 반복 호출 설정',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_03_0004','Instance 명',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_03_0005','원천 데이터 모델',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_03_0006','표준 데이터 모델',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_03_0007','원천 데이터 모델 선택',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_03_0008','표준 데이터 모델 선택',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_03_0009','반복 호출 설정',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_03_0010','변환 클래스 작성',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_03_0011','반복 호출에 사용될 항목 선택',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_03_0012','선택',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','dataConvert_03_0013','필수여부',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_03_0014','값 유형',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_03_0015','항목명(key)',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_03_0016','항목값(value)',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_03_0017','항목설명',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_03_0018','선택한 항목에 대하여',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_03_0019','개의 반복 설정을',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_03_0020','번째 반복 항목 작성',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_04_0001','데이터 모델 변환',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_04_0002','데이터모델 변환 기본 정보',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','dataConvert_04_0003','Instance ID',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_04_0004','Instance 명',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_04_0005','원천 데이터 모델',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_04_0007','원천 데이터 모델 선택',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_04_0008','표준 데이터 모델 선택',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_04_0009','변환 클래스 작성',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_04_0010','원천 데이터모델 상세정보',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_04_0011','항목명(영문)',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_04_0012','항목명(국문)',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_04_0013','항목구분',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','dataConvert_04_0014','항목설명',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_04_0015','경로',NULL,NULL,NULL,NULL),
   ('KR','dataConvert_04_0016','데이터 구조',NULL,NULL,NULL,NULL),
   ('KR','mqtt_subscribe_0001','oneM2M Subscription Information',NULL,NULL,NULL,NULL),
   ('KR','mqtt_subscribe_0002','oneM2M HTTP protocol setting',NULL,NULL,NULL,NULL),
   ('KR','mqtt_subscribe_0003','HTTP server IP/port',NULL,NULL,NULL,NULL),
   ('KR','mqtt_subscribe_0004','Discovery Target',NULL,NULL,NULL,NULL),
   ('KR','mqtt_subscribe_0005','Discovery Filters',NULL,NULL,NULL,NULL),
   ('KR','mqtt_subscribe_0006','Header',NULL,NULL,NULL,NULL),
   ('KR','mqtt_subscribe_0007','oneM2M MQTT protocol setting',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','mqtt_subscribe_0008','MQTT broker IP/port',NULL,NULL,NULL,NULL),
   ('KR','mqtt_subscribe_0009','Originator-ID',NULL,NULL,NULL,NULL),
   ('KR','mqtt_subscribe_0010','Receiver ID',NULL,NULL,NULL,NULL),
   ('KR','mqtt_subscribe_0011','Resoure List',NULL,NULL,NULL,NULL),
   ('KR','popupDashLog_0015','어댑터 유형',NULL,NULL,NULL,NULL),
   ('KR','mqtt_subscribe_0012','Resoure ID',NULL,NULL,NULL,NULL),
   ('KR','mqtt_subscribe_0013','Result',NULL,NULL,NULL,NULL),
   ('KR','mqtt_subscribe_0014','하위 컨테이너 검색',NULL,NULL,NULL,NULL),
   ('KR','mqtt_subscribe_0015','구독',NULL,NULL,NULL,NULL),
   ('KR','mqtt_subscribe_0016','항목명',NULL,NULL,NULL,NULL);
INSERT INTO public.lang_data (lang_code,lang_key,lang_value,first_create_dt,first_create_id,last_update_dt,last_update_id) VALUES
   ('KR','mqtt_subscribe_0017','비고명',NULL,NULL,NULL,NULL),
   ('KR','commList_0001','시스템 관리',NULL,NULL,NULL,NULL),
   ('KR','commList_0002','공통코드 목록',NULL,NULL,NULL,NULL),
   ('KR','commList_0003','순번',NULL,NULL,NULL,NULL),
   ('KR','popupDashLog_0004','OpenAPI',NULL,NULL,NULL,NULL),
   ('KR','popupDashLog_0005','OneM2M',NULL,NULL,NULL,NULL);

  
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
   ('123','321','N','2021-02-09 17:24:38.547073',NULL,'2021-02-10 16:28:08.339166',NULL),
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

  

INSERT INTO public.adapter_type_info (adapter_type_id,adapter_type_nm,adapter_type_div,use_yn) VALUES ('A000000003','기상청 날씨 측정','I1110','Y');
INSERT INTO public.adapter_type_info (adapter_type_id,adapter_type_nm,adapter_type_div,use_yn) VALUES ('A000000004','기상청 날씨 예보','I1110','Y'); 
INSERT INTO public.adapter_type_info (adapter_type_id,adapter_type_nm,adapter_type_div,use_yn) VALUES ('A000000005','oneM2M 주차장 정보','I1120','Y'); 
INSERT INTO public.adapter_type_info (adapter_type_id,adapter_type_nm,adapter_type_div,use_yn) VALUES ('M000000002','성남시 주차장','I1120','Y'); 
INSERT INTO public.adapter_type_info (adapter_type_id,adapter_type_nm,adapter_type_div,use_yn) VALUES ('M000000003','성남시 기상관측','I1110','Y');


INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('M000000002','MODEL_ID','OffStreetParking,ParkingSpot','Y','성남시 주차장의 모델아이디','I0701',1,'I1201','Y','I1301','b');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('M000000002','DATASET_ID','pocOffStreetParking,pocParkingSpot','Y','성남시 주차장의 데이터셋아이디','I0701',3,'I1201','Y','I1301','b');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('M000000002','INVOKE_CLASS','com.cityhub.adapter.convex.ConvParkingOneM2M','Y','성남시 주차장의 변환클래스','I0701',4,'I1201','Y','I1301','b');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('M000000002','URL_ADDR','tcp://203.253.128.164:1883','Y','성남시 주차장의 주소','I0701',5,'I1201','Y','I1301','b');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('M000000002','REQ_PREFIX','/oneM2M/req/Mobius2/','Y','성남시 주차장의 요청접두사','I0701',6,'I1201','Y','I1301','b');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('M000000002','RESP_PREFIX','/oneM2M/resp/Mobius2/','Y','성남시 주차장의 응답접두사','I0701',7,'I1201','Y','I1301','b');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('M000000002','TOPIC','SlotYatopParking','Y','성남시 주차장의 토픽','I0701',8,'I1201','Y','I1301','b');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('M000000002','META_INFO','http://203.253.128.164:7579/Mobius/sync_parking_raw','Y','성남시 주차장의 구독정보확인','I0701',9,'I1201','Y','I1301','b');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('M000000002','CONN_TERM','3600','Y','갱신주기','I0701',10,'I1201','Y','I1301','b');

INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('M000000003','MODEL_ID','WeatherObserved','Y','성남시 기상측정 모델아이디','I0701',1,'I1201','Y','I1301','b');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('M000000003','DATASET_ID','pocWeatherObserved','Y','성남시 기상측정 데이터셋아이디','I0701',3,'I1201','Y','I1301','b');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('M000000003','INVOKE_CLASS','com.cityhub.adapter.convex.ConvWeatherObserved','Y','성남시 기상측정 변환클래스','I0701',4,'I1201','Y','I1301','b');  
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('M000000003','CONN_TERM','3600','Y','갱신주기','I0701',5,'I1201','Y','I1301','b');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('M000000003','url_addr','http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst?dataType=json&numOfRows=1000&serviceKey=xxu2gHkHh5PrWLXUSnk%2BqICJc2%2FwsENQLJnapmbP0S52Jg7FxIFohMk3FfhI5mkp5Dz7ir%2FuocdMHrnGEP9ZBQ%3D%3D&nx=63&ny=124','Y','공공데이터포털의 성남시 기상측정 가져오기','I0701',6,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('M000000003','ParamVariable','base_date,base_time','Y','가변데이터정보','I0701',9,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('M000000003','base_date','yyyyMMdd,MINUTE,-40','Y','','I0701',12,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('M000000003','base_time','HHmm,MINUTE,-40','Y','','I0701',13,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('M000000003','gs1Code','urn:datahub:WeatherObserved:14858','Y','','I0701',14,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('M000000003','addressCountry','KR','Y','','I0701',15,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('M000000003','addressRegion','경기도','Y','','I0701',16,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('M000000003','addressLocality','성남시','Y','','I0701',18,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('M000000003','addressTown','수정구','Y','','I0701',18,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('M000000003','streetAddress','경기도 성남시 수정구 수정로 319','Y','','I0701',19,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('M000000003','location','[127.14858, 37.4557691]','Y','','I0701',20,'I1201','Y','I1301','a');

INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000005','MODEL_ID','','Y','','I0701',1,'I1201','Y','I1301','b');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000005','DATASET_ID','','Y','','I0701',3,'I1201','Y','I1301','b');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000005','INVOKE_CLASS','','Y','','I0701',4,'I1201','Y','I1301','b');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000005','URL_ADDR','','Y','','I0701',5,'I1201','Y','I1301','b');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000005','REQ_PREFIX','','Y','','I0701',6,'I1201','Y','I1301','b');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000005','RESP_PREFIX','','Y','','I0701',7,'I1201','Y','I1301','b');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000005','TOPIC','','Y','','I0701',8,'I1201','Y','I1301','b');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000005','META_INFO','','Y','','I0701',9,'I1201','Y','I1301','b');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000005','CONN_TERM','3600','Y','','I0701',10,'I1201','Y','I1301','b');

INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000003','MODEL_ID','','Y','','I0701',1,'I1201','Y','I1301','b');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000003','DATASET_ID','','Y','','I0701',3,'I1201','Y','I1301','b');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000003','INVOKE_CLASS','','Y','','I0701',4,'I1201','Y','I1301','b');  
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000003','CONN_TERM','3600','Y','','I0701',5,'I1201','Y','I1301','b');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000003','url_addr','','Y','','I0701',6,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000003','ParamVariable','','Y','','I0701',9,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000003','gs1Code','','Y','','I0701',10,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000003','addressCountry','KR','Y','','I0701',11,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000003','addressRegion','','Y','','I0701',12,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000003','addressTown','','Y','','I0701',13,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000003','addressLocality','','Y','','I0701',14,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000003','streetAddress','','Y','','I0701',15,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000003','location','','Y','','I0701',16,'I1201','Y','I1301','a');
  

INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000004','MODEL_ID','','Y','','I0701',1,'I1201','Y','I1301','b');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000004','DATASET_ID','','Y','','I0701',3,'I1201','Y','I1301','b');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000004','INVOKE_CLASS','','Y','','I0701',4,'I1201','Y','I1301','b');  
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000004','CONN_TERM','3600','Y','','I0701',5,'I1201','Y','I1301','b');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000004','url_addr','','Y','','I0701',6,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000004','ParamVariable','','Y','','I0701',9,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000004','gs1Code','','Y','','I0701',10,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000004','addressCountry','','Y','','I0701',11,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000004','addressRegion','','Y','','I0701',12,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000004','addressTown','','Y','','I0701',13,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000004','addressLocality','','Y','','I0701',14,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000004','streetAddress','','Y','','I0701',15,'I1201','Y','I1301','a');
INSERT INTO public.adapter_type_detail_conf (adapter_type_id,item,value,essential_yn,item_described,value_type,display_seq,setup_method,change_able_yn,use_purpose,sector) VALUES ('A000000004','location','','Y','','I0701',16,'I1201','Y','I1301','a');

