-- =============================================================================
-- Sistema registros copias — schema MySQL 8+ (referencia alinhada ao JPA)
-- =============================================================================
-- Opcao A: executar este script e depois arrancar a app com
--   spring.jpa.hibernate.ddl-auto=validate
-- Opcao B: nao executar o script e usar apenas
--   spring.jpa.hibernate.ddl-auto=update (padrao no application.properties)
-- =============================================================================

CREATE DATABASE IF NOT EXISTS sistema_copias
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE sistema_copias;

-- ---------------------------------------------------------------------------
-- Utilizadores
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS app_user (
  id              VARCHAR(36)  NOT NULL,
  username        VARCHAR(120) NOT NULL,
  password_hash   VARCHAR(200) NOT NULL,
  role            VARCHAR(32)  NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_app_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- Pedidos copiadora
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS copia_pedido (
  id               VARCHAR(36) NOT NULL,
  created_at       DATETIME(6) NOT NULL,
  total_amount     DECIMAL(19,2),
  payment_method   VARCHAR(32),
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS copia_pedido_item (
  pedido_id     VARCHAR(36) NOT NULL,
  line_idx      INT         NOT NULL,
  product_type  VARCHAR(64),
  quantity      INT         NOT NULL,
  unit_price    DECIMAL(19,2),
  total         DECIMAL(19,2),
  PRIMARY KEY (pedido_id, line_idx),
  CONSTRAINT fk_copia_pedido_item_pedido
    FOREIGN KEY (pedido_id) REFERENCES copia_pedido (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- Reforco escolar
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS reforco_aluno (
  id                         VARCHAR(36) NOT NULL,
  nome_completo              VARCHAR(300) NOT NULL,
  idade                      INT          NOT NULL,
  escolaridade               VARCHAR(40)  NOT NULL,
  tipo_pcd                   VARCHAR(40)  NOT NULL,
  sexo                       VARCHAR(20),
  nome_pai                   VARCHAR(300),
  nome_mae                   VARCHAR(300),
  telefone_contato           VARCHAR(80),
  objetivo_reforco           TEXT,
  expectativa_pais           TEXT,
  valor_mensalidade          DECIMAL(19,2),
  dia_pagamento_preferido    INT,
  recorrencia_mensalidade    VARCHAR(32),
  proxima_cobranca_prevista  DATE,
  created_at                 DATETIME(6),
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS reforco_atividade_aluno (
  id                    VARCHAR(36) NOT NULL,
  aluno_id              VARCHAR(36) NOT NULL,
  created_at            DATETIME(6),
  updated_at            DATETIME(6),
  texto                 TEXT         NOT NULL,
  professora            VARCHAR(200),
  status                VARCHAR(32)  NOT NULL,
  percepcao_professor   TEXT,
  PRIMARY KEY (id),
  KEY idx_atividade_aluno (aluno_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- Agenda semanal (documento num unico registo)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS reforco_agenda_singleton (
  id           VARCHAR(36) NOT NULL,
  json_payload LONGTEXT     NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- Fluxo financeiro
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS fluxo_entrada (
  id                     VARCHAR(36) NOT NULL,
  data_hora_registro     DATETIME(6) NOT NULL,
  tipo                   VARCHAR(40) NOT NULL,
  aluno_id               VARCHAR(36),
  descricao_livre        TEXT,
  valor                  DECIMAL(19,2),
  meio_pagamento         VARCHAR(40),
  meio_pagamento_outro   VARCHAR(200),
  situacao               VARCHAR(24),
  referencia_cobranca    VARCHAR(120),
  PRIMARY KEY (id),
  KEY idx_fluxo_entrada_aluno (aluno_id),
  KEY idx_fluxo_entrada_data (data_hora_registro)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS fluxo_saida (
  id                    VARCHAR(36) NOT NULL,
  data_hora_registro    DATETIME(6) NOT NULL,
  categoria_recorrente  VARCHAR(40),
  descricao_compra      TEXT,
  valor                 DECIMAL(19,2),
  data_compra_ou_saida  DATE,
  banco_pagamento       VARCHAR(120),
  link_compra           TEXT,
  origem_compra         VARCHAR(300),
  PRIMARY KEY (id),
  KEY idx_fluxo_saida_data (data_compra_ou_saida)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS fluxo_origem_compra_custom (
  id    BIGINT AUTO_INCREMENT NOT NULL,
  nome  VARCHAR(255) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_fluxo_origem_nome (nome)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- Biblioteca ACES (emprestimos)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS biblioteca_emprestimo (
  id                         VARCHAR(36)  NOT NULL,
  nome_aluno                 VARCHAR(300) NOT NULL,
  aluno_id                   VARCHAR(36),
  titulo_livro               VARCHAR(500) NOT NULL,
  data_emprestimo            DATE         NOT NULL,
  data_devolucao_prevista    DATE         NOT NULL,
  created_at                 DATETIME(6),
  PRIMARY KEY (id),
  KEY idx_biblioteca_devolucao (data_devolucao_prevista),
  KEY idx_biblioteca_aluno (aluno_id),
  CONSTRAINT fk_biblioteca_aluno
    FOREIGN KEY (aluno_id) REFERENCES reforco_aluno (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
