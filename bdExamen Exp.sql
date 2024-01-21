CREATE TABLE IF NOT EXISTS documents_type (
  id_documents_type SERIAL PRIMARY KEY,
  cod_type VARCHAR(45) NOT NULL,
  desc_type VARCHAR(45) NOT NULL,
  status INT NOT NULL,
  user_create VARCHAR(45),
  date_create TIMESTAMP,
  user_modif VARCHAR(45),
  date_modif TIMESTAMP
);
INSERT INTO documents_type (cod_type, desc_type, status, user_create, date_create)
VALUES ('01', 'DNI', 1, 'ADMIN', NOW());
INSERT INTO documents_type (cod_type, desc_type, status, user_create, date_create)
VALUES ('04', 'CARNET EXT.', 1, 'ADMIN', NOW());
INSERT INTO documents_type (cod_type, desc_type, status, user_create, date_create)
VALUES ('06', 'RUC', 1, 'ADMIN', NOW());
INSERT INTO documents_type (cod_type, desc_type, status, user_create, date_create)
VALUES ('07', 'PASAPORTE', 1, 'ADMIN', NOW());
INSERT INTO documents_type (cod_type, desc_type, status, user_create, date_create)
VALUES ('00', 'OTROS', 1, 'ADMIN', NOW());



CREATE TABLE IF NOT EXISTS enterprises_type (
  id_enterprises_type SERIAL PRIMARY KEY,
  desc_type VARCHAR(45) NOT NULL,
  cod_type VARCHAR(45) NOT NULL,
  status INT NOT NULL,
  user_create VARCHAR(45),
  date_create TIMESTAMP,
  user_modif VARCHAR(45),
  date_modif TIMESTAMP
);
INSERT INTO enterprises_type (cod_type, desc_type, status, user_create, date_create)
VALUES ('01', 'SA', 1, 'ADMIN', NOW());
INSERT INTO enterprises_type (cod_type, desc_type, status, user_create, date_create)
VALUES ('02', 'SAC', 1, 'ADMIN', NOW());
INSERT INTO enterprises_type (cod_type, desc_type, status, user_create, date_create)
VALUES ('03', 'SRL', 1, 'ADMIN', NOW());
INSERT INTO enterprises_type (cod_type, desc_type, status, user_create, date_create)
VALUES ('04', 'EIRL', 1, 'ADMIN', NOW());
INSERT INTO enterprises_type (cod_type, desc_type, status, user_create, date_create)
VALUES ('05', 'SAA', 1, 'ADMIN', NOW());

-- Table enterprises
CREATE TABLE IF NOT EXISTS enterprises (
  id_enterprises SERIAL PRIMARY KEY,
  num_document VARCHAR(15) NOT NULL,
  business_name VARCHAR(150) NOT NULL,
  tradename VARCHAR(150) NOT NULL,
  status INT NOT NULL,
  user_create VARCHAR(45),
  date_create TIMESTAMP,
  user_modif VARCHAR(45),
  date_modif TIMESTAMP,
  user_delete VARCHAR(45),
  date_delete TIMESTAMP,
  document_type_id_document_type INT NOT NULL,
  enterprises_type_id_enterprises_type INT NOT NULL,
  FOREIGN KEY (document_type_id_document_type) REFERENCES documents_type (id_documents_type) ON DELETE NO ACTION ON UPDATE NO ACTION,
  FOREIGN KEY (enterprises_type_id_enterprises_type) REFERENCES enterprises_type (id_enterprises_type) ON DELETE NO ACTION ON UPDATE NO ACTION
);