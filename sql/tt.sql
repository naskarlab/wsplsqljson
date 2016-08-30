--------------------------------------------------------
--  Arquivo criado - Terça-feira-Agosto-30-2016   
--------------------------------------------------------
--------------------------------------------------------
--  DDL for Type TESTE_OBJECT_TYPE
--------------------------------------------------------

  CREATE OR REPLACE TYPE "TT"."TESTE_OBJECT_TYPE" is object (
    rid number,
    rds varchar2(256)
  );

/
--------------------------------------------------------
--  DDL for Type TESTE_TABLE_TYPE
--------------------------------------------------------

  CREATE OR REPLACE TYPE "TT"."TESTE_TABLE_TYPE" is table of teste_object_type;

/
--------------------------------------------------------
--  DDL for Table TB_CUSTOMER
--------------------------------------------------------

  CREATE TABLE "TT"."TB_CUSTOMER" 
   (	"ID" NUMBER, 
	"DS" VARCHAR2(20 BYTE), 
	"DT_CT" DATE
   ) SEGMENT CREATION IMMEDIATE 
  PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255 NOCOMPRESS LOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "SYSTEM" ;
--------------------------------------------------------
--  DDL for Table TB_DOC
--------------------------------------------------------

  CREATE TABLE "TT"."TB_DOC" 
   (	"ID_DOC" NUMBER(5,0), 
	"DS_BLOB" BLOB
   ) SEGMENT CREATION IMMEDIATE 
  PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255 NOCOMPRESS LOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "SYSTEM" 
 LOB ("DS_BLOB") STORE AS BASICFILE (
  TABLESPACE "SYSTEM" ENABLE STORAGE IN ROW CHUNK 8192 RETENTION 
  NOCACHE LOGGING 
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)) ;
REM INSERTING into TT.TB_CUSTOMER
SET DEFINE OFF;
Insert into TT.TB_CUSTOMER (ID,DS,DT_CT) values ('1','1111',to_date('26/05/16','DD/MM/RR'));
Insert into TT.TB_CUSTOMER (ID,DS,DT_CT) values ('2','2222',to_date('26/05/16','DD/MM/RR'));
Insert into TT.TB_CUSTOMER (ID,DS,DT_CT) values ('3','333',to_date('26/05/16','DD/MM/RR'));
REM INSERTING into TT.TB_DOC
SET DEFINE OFF;
Insert into TT.TB_DOC (ID_DOC) values ('100');
--------------------------------------------------------
--  Constraints for Table TB_DOC
--------------------------------------------------------

  ALTER TABLE "TT"."TB_DOC" MODIFY ("DS_BLOB" NOT NULL ENABLE);
  ALTER TABLE "TT"."TB_DOC" MODIFY ("ID_DOC" NOT NULL ENABLE);
--------------------------------------------------------
--  DDL for Package PKG_BLOB
--------------------------------------------------------

  CREATE OR REPLACE PACKAGE "TT"."PKG_BLOB" as 

  procedure prc_upload_blob(
    p_id_doc in number,
    p_file1 in blob,
    p_size_file1 out number
  );
  
  procedure prc_download_blob(
    p_id_doc in number,
    p_file1 out blob
  );

end pkg_blob;

/
--------------------------------------------------------
--  DDL for Package PKG_TESTE
--------------------------------------------------------

  CREATE OR REPLACE PACKAGE "TT"."PKG_TESTE" as 

  procedure prc_teste(
    p_id in number,
    p_name in varchar2,
    p_date in date,
    p_dados in out number,
    p_result_id out number,
    p_result_name out varchar2,
    p_result_date out date,
    p_result_2 out number,
    p_result_3 out number
  );
  
  procedure prc_teste2(
    p_id in number,
    p_rows out sys_refcursor
  );
  
  procedure prc_teste_refcursor_table(
    p_id in number,
    p_rows out sys_refcursor
  );
  
  procedure prc_teste_session(
    p_id in number,
    pls_session_cd_usuario in out varchar2,
    p_cd_usuario out varchar2
  );
  
  function fnc_teste(
    p_id number
  ) return varchar2;
  
  function fnc_teste_refcursor(
    p_id number
  ) return sys_refcursor;
  
end pkg_teste;

/
--------------------------------------------------------
--  DDL for Package Body PKG_BLOB
--------------------------------------------------------

  CREATE OR REPLACE PACKAGE BODY "TT"."PKG_BLOB" as

  procedure prc_upload_blob(
    p_id_doc in number,
    p_file1 in blob,
    p_size_file1 out number
  ) as
  begin
    p_size_file1 := dbms_lob.getlength(p_file1);
  end prc_upload_blob;
  
  procedure prc_download_blob(
    p_id_doc in number,
    p_file1 out blob
  ) as
  begin
    select ds_blob 
    into p_file1 
    from tb_doc 
    where id_doc = p_id_doc;
  end prc_download_blob;

end pkg_blob;

/
--------------------------------------------------------
--  DDL for Package Body PKG_TESTE
--------------------------------------------------------

  CREATE OR REPLACE PACKAGE BODY "TT"."PKG_TESTE" as

  procedure prc_teste(
    p_id in number,
    p_name in varchar2,
    p_date in date,
    p_dados in out number,
    p_result_id out number,
    p_result_name out varchar2,
    p_result_date out date,
    p_result_2 out number,
    p_result_3 out number
  ) as
  begin
  
    p_dados := p_id + 10;
    p_result_id := p_id + 20;
    p_result_name := p_name || ' OK';
    p_result_date := p_date + 10;
    p_result_2 := 2.55;
    
  end prc_teste;
  
  procedure prc_teste2(
    p_id in number,
    p_rows out sys_refcursor
  ) as
  begin
    open p_rows for select id from tb_customer;
  end;
  
  procedure prc_teste_refcursor_table(
    p_id in number,
    p_rows out sys_refcursor
  ) as 
    v_table teste_table_type := teste_table_type();
    v_tuple teste_object_type; 
  begin
    v_tuple := teste_object_type(1, 'teste 1');
    v_table.extend();
    v_table(1) := v_tuple;
    
    v_tuple := teste_object_type(2, 'teste 2');
    v_table.extend();
    v_table(2) := v_tuple;
    
    open p_rows for select * from table(v_table);
  end;
  
  procedure prc_teste_session(
    p_id in number,
    pls_session_cd_usuario in out varchar2,
    p_cd_usuario out varchar2
  ) as 
  begin
    if pls_session_cd_usuario is null then
      pls_session_cd_usuario := 'rafael';  
      p_cd_usuario := 'new session' || pls_session_cd_usuario;
    else
      p_cd_usuario := pls_session_cd_usuario;
    end if;
  end;
  
  function fnc_teste(
    p_id number
  ) return varchar2
  as
  begin
    return 'teste';
  end;
  
  function fnc_teste_refcursor(
    p_id number
  ) return sys_refcursor
  as
    v_rows sys_refcursor;
  begin
    open v_rows for select t.id, t.DT_CT from tb_customer t;
    return v_rows;
  end;
  
end pkg_teste;

/
