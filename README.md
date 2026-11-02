# Sistema APAE

Sistema web para a APAE organizar os atendimentos (fisioterapia, fonoaudiologia, psicologia, terapia ocupacional, aulas etc.).

## Funcionalidades

- **Pacientes** – cadastro com data de nascimento, diagnóstico/CID, responsável, telefone e observações (alergias, cuidados).
- **Professores e profissionais** – cadastro com especialidade, registro no conselho (CREFITO, CRP...) e contato.
- **Horários de atendimento** – agendamento de profissional + paciente com data, horário, sala e tipo de atendimento.
  - Opção de **repetir toda semana** até uma data (ex.: fisioterapia toda segunda às 8h).
  - **Bloqueia conflitos**: o mesmo profissional ou o mesmo paciente não pode ter dois atendimentos no mesmo horário.
  - Situação do atendimento: Agendado, Realizado, Faltou, Cancelado.
- **Agenda do dia** – horários do dia com cada paciente (idade, diagnóstico, sala, responsável, observações);
  navega entre os dias, marca presença/falta e pode imprimir.
- **Login com dois perfis**
  - **Administração** (secretaria/coordenação): acessa e altera tudo, e cadastra os acessos.
  - **Profissional** (professor/terapeuta): vê **somente a própria agenda** e marca presença/falta nos próprios atendimentos.

## Primeiro acesso

Na primeira vez que o sistema roda é criado o usuário **`admin`** com senha **`admin`**.
Ao entrar, o sistema obriga a criar uma senha nova.

Para dar acesso a um professor: **Profissionais → Criar acesso**, defina usuário e uma senha inicial e passe para ele.
No primeiro login ele também precisará trocar a senha. Se esquecer a senha, a administração define uma nova
em **Acessos → Editar**.

Para outra senha inicial do `admin`, inicie com `--apae.admin.senha-inicial=...`.

## Como rodar

Requisito: Java 21 ou mais novo. O Maven é baixado automaticamente pelo wrapper.

```bash
# Windows
mvnw.cmd spring-boot:run
# Linux / macOS / Git Bash
./mvnw spring-boot:run
```

Acesse http://localhost:8080

## Banco de dados

Usa o H2 salvo em arquivo na pasta `data/` (não precisa instalar nada). Os dados ficam guardados entre reinícios.
Para consultar as tabelas (logado como administração): http://localhost:8080/h2-console com JDBC URL `jdbc:h2:file:./data/apae`, usuário `sa`, sem senha.

Para usar outro banco (PostgreSQL, SQL Server, MySQL), adicione o driver no `pom.xml` e altere
`spring.datasource.*` em `src/main/resources/application.properties`.

## Testes

```bash
./mvnw test
```
