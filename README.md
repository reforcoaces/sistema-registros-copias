# sistema-registros-copias

Sistema web em Java para registrar atendimentos de uma copiadora com:
- data e hora do atendimento
- varios produtos no mesmo pedido
- quantidade por item
- calculo automatico do valor total
- meio de pagamento (pix, debito, credito, dinheiro)

## Persistencia

Os dados ficam em **MySQL** (base `sistema_copias` por defeito). Execute o script em `src/main/resources/db/mysql-init.sql` se quiser criar o esquema manualmente, ou deixe o Hibernate atualizar com `spring.jpa.hibernate.ddl-auto=update`. A aplicacao **nao** le nem grava ficheiros JSON em `data/`; se ainda tiveres essa pasta localmente com ficheiros antigos, podes apaga-la.

### Senha e dados sensiveis (nao commitar)

O ficheiro `src/main/resources/application.properties` **nao** deve conter senhas. Tens duas formas comuns:

1. **Variavel de ambiente** (CI, servidor, ou PowerShell antes de correr): `SPRING_DATASOURCE_PASSWORD` (e opcionalmente `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`).
2. **Ficheiro local na raiz do projeto**: copia `application-local.properties.example` para `application-local.properties`, edita a senha e arranca a app a partir da raiz do repo (o Spring importa esse ficheiro por `spring.config.import`). O nome `application-local.properties` esta no `.gitignore`.

O ficheiro `.env` tambem esta ignorado pelo Git; podes usa-lo com ferramentas que exportam variaveis para o processo antes de `mvn spring-boot:run`, mas o Spring Boot **nao** le `.env` sozinho.

**Nota:** Se uma senha chegou a ser commitada no historico do Git, convém **alterar a senha no MySQL** e usar apenas um dos metodos acima daqui em diante.

## Produtos e precos

- Impressao Preto e Branco: R$ 2,00
- Impressao Colorida: R$ 3,00
- Copia Preto e Branco: R$ 1,00
- Copia Colorida: R$ 2,00
- Digitalizacao: R$ 1,00
- Plastificacao: R$ 5,00

## Tecnologias

- Java 17
- Spring Boot 3
- Thymeleaf
- Persistencia em MySQL (Spring Data JPA)

## Funcionalidades implementadas

- Botao **Adicionar mais produto** no pedido
- Botao **Remover item** antes de finalizar
- Botao **Atualizar total**
- Botao **Finalizar pedido**
- Registro com data/hora automatica no momento da finalizacao
- Tela de historico de pedidos
- Relatorio de faturamento diario
- Relatorio de faturamento mensal
- Exportacao de pedidos em CSV
- Exportacao de pedidos em PDF
- Login com perfis ADMIN/COLABORADOR
- Edicao de pedidos finalizados (somente ADMIN)
- Gestao de usuarios e senhas (somente ADMIN)
- Importacao retroativa (somente ADMIN)
- Filtros por data e pagamento em **Pedidos** e **Relatorios**
- **Dashboard** com resumo visual (cores)

## Como rodar localmente

### Opcao 1 (recomendada neste projeto): sem instalar globalmente

1. Execute o setup de ferramentas locais (baixa JDK 17 + Maven portateis):

```powershell
./setup-local-tools.ps1
```

2. Rode a aplicacao:

```powershell
./ligar-sistemas.ps1
```

3. Acesse:
- `http://localhost:8080` (novo pedido)
- `http://localhost:8080/dashboard` (resumo visual)
- `http://localhost:8080/pedidos` (historico)
- `http://localhost:8080/relatorios` (faturamento)

### Opcao 2: com Java 17 e Maven instalados no sistema

```bash
mvn clean package
mvn spring-boot:run
```

Depois do `package`, pode subir só com a JVM (sem Maven no arranque):

```bash
java -jar target/sistema-registros-copias-0.0.1-SNAPSHOT.jar
```

### Exportacoes

- CSV: `http://localhost:8080/pedidos/export.csv`
- PDF: `http://localhost:8080/pedidos/export.pdf`

### Acesso inicial

- Admin: usuario `admin`, senha `admin123`
- Colaborador: usuario `lucilene`, senha `colab123`
- Colaborador: usuario `lukas`, senha `colab123`

Altere as senhas iniciais na tela `Usuarios` assim que entrar.
