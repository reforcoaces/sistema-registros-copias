# sistema-registros-copias

Sistema web em Java para registrar atendimentos de uma copiadora com:
- data e hora do atendimento
- varios produtos no mesmo pedido
- quantidade por item
- calculo automatico do valor total
- meio de pagamento (pix, debito, credito, dinheiro)

## Recomendacao de armazenamento inicial

Use **JSON** (e nao TXT), porque:
- mantem estrutura clara de pedidos e itens
- facilita leitura por codigo Java
- simplifica migracao futura para banco de dados real

Os dados ficam em `data/orders.json`.

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
- Persistencia em arquivo JSON

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
- Agendamento: todo dia as **20:00** (America/Sao_Paulo) envia o faturamento do dia por e-mail; no **ultimo dia do mes** as **20:05** envia backup do `orders.json` (se SMTP estiver configurado)

## E-mail (opcional)

Destino padrao: `reforco.aces@gmail.com` (altere com `APP_NOTIFICATION_TO`).

Configure SMTP (exemplo Gmail com senha de app):

- `spring.mail.host=smtp.gmail.com`
- `spring.mail.port=587`
- `spring.mail.username=...`
- `spring.mail.password=...`

Sem `spring.mail.host` a aplicacao sobe normalmente; os envios agendados sao ignorados ate voce configurar o e-mail.

## Como rodar localmente

### Opcao 1 (recomendada neste projeto): sem instalar globalmente

1. Execute o setup de ferramentas locais (baixa JDK 17 + Maven portateis):

```powershell
./setup-local-tools.ps1
```

2. Rode a aplicacao:

```powershell
./run-local.ps1
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
