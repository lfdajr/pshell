CREATE TABLE BANCO_DADOS (
  ID INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  NOME VARCHAR(100) NULL,
  ANOTACAO VARCHAR(2000) NULL,
  PRIMARY KEY(ID),
  UNIQUE INDEX BANCO_DADOS_UNICO(NOME)
)
TYPE=InnoDB;

CREATE TABLE TABELA (
  ID INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  BANCO_DADOS_ID INTEGER UNSIGNED NOT NULL,
  NOME VARCHAR(100) NULL,
  ANOTACAO VARCHAR(2000) NULL,
  PRIMARY KEY(ID),
  INDEX TABELA_FKIndex1(BANCO_DADOS_ID),
  FOREIGN KEY(BANCO_DADOS_ID)
    REFERENCES BANCO_DADOS(ID)
      ON DELETE CASCADE
      ON UPDATE NO ACTION
)
TYPE=InnoDB;

CREATE TABLE STORED_PROCEDURES (
  ID INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  BANCO_DADOS_ID INTEGER UNSIGNED NOT NULL,
  NOME VARCHAR(100) NULL,
  ANOTACAO VARCHAR(2000) NULL,
  PRIMARY KEY(ID),
  INDEX STORED_PROCEDURES_FKIndex1(BANCO_DADOS_ID),
  FOREIGN KEY(BANCO_DADOS_ID)
    REFERENCES BANCO_DADOS(ID)
      ON DELETE NO ACTION
      ON UPDATE NO ACTION
)
TYPE=InnoDB;

CREATE TABLE COLUNA (
  ID INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  CHAVE_ESTRANGEIRA INTEGER UNSIGNED NULL,
  TABELA_ID INTEGER UNSIGNED NOT NULL,
  NOME VARCHAR(100) NULL,
  TIPO VARCHAR(100) NULL,
  ANOTACAO VARCHAR(2000) NULL,
  NULO INTEGER UNSIGNED NULL,
  TAMANHO VARCHAR(10) NULL,
  AUTO_INCREMENTO BOOL NULL,
  CHAVE_PRIMARIA BOOL NULL,
  PRIMARY KEY(ID),
  INDEX COLUNA_FKIndex1(TABELA_ID),
  INDEX COLUNA_FKIndex2(CHAVE_ESTRANGEIRA),
  FOREIGN KEY(TABELA_ID)
    REFERENCES TABELA(ID)
      ON DELETE CASCADE
      ON UPDATE NO ACTION,
  FOREIGN KEY(CHAVE_ESTRANGEIRA)
    REFERENCES COLUNA(ID)
      ON DELETE SET NULL
      ON UPDATE NO ACTION
)
TYPE=InnoDB;


