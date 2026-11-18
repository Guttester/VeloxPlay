<div align="center">

# 🎬 VeloxPlay 2.0

**Plataforma demonstrativa de catálogo de filmes, com autenticação, favoritos e painéis por perfil de acesso.**

![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.6-6DB33F?logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=white)

</div>

## Sobre o projeto

O **VeloxPlay 2.0** é um projeto de portfólio voltado à construção de uma experiência de catálogo de filmes e à implementação de uma API REST com autenticação e controle de permissões. O projeto reúne uma interface web distribuída em `Front/` e um backend em Spring Boot.

O foco técnico inclui segurança, persistência de dados, separação de responsabilidades e testes automatizados. **Não se trata de um serviço comercial de transmissão de filmes.**

## Funcionalidades

- Navegação e consulta pública do catálogo de filmes.
- Cadastro e autenticação de usuários com tokens JWT.
- Gerenciamento de perfil e favoritos para usuários autenticados.
- Controle de acesso por perfis `USER`, `MODERATOR` e `ADMIN`.
- Área demonstrativa de moderação com dados fictícios e isolamento por sessão.
- Painel administrativo para gerenciamento de filmes e usuários reais.
- Limitação de requisições nas rotas sensíveis de autenticação.

## Tecnologias

| Camada | Tecnologias |
| --- | --- |
| Interface web | Frontend compilado disponível em `Front/` |
| API | Java 17, Spring Boot 3.5.6, Spring Web |
| Segurança | Spring Security, JWT, BCrypt, validação de entrada |
| Persistência | Spring Data JPA, Hibernate, PostgreSQL |
| Testes | JUnit / Spring Boot Test, Spring Security Test, H2 em memória |
| Empacotamento | Maven e Docker (build em múltiplas etapas) |

## Perfis de acesso

| Perfil | Acesso |
| --- | --- |
| **Visitante** | Consulta pública de filmes |
| **USER** | Catálogo, perfil próprio e favoritos |
| **MODERATOR** | Recursos de USER e operações na área demonstrativa `/demo`, sem acessar a administração real |
| **ADMIN** | Administração real de filmes e usuários, além da área demonstrativa |

A autorização é aplicada no **backend**. A ocultação de elementos na interface não substitui o controle de acesso da API.

## API — principais rotas

| Recurso | Rotas | Observação |
| --- | --- | --- |
| Autenticação | `/auth/login`, `/auth/register`, `/auth/refresh` | Acesso público, com proteções de autenticação |
| Filmes | `/filmes`, `/filmes/{id}` | Leitura pública; escrita restrita a ADMIN |
| Administração | `/admin/**`, `/users` | Operações protegidas por perfil |
| Demonstração | `/demo/filmes`, `/demo/users`, `/demo/stats`, `/demo/reset` | Área isolada para MODERATOR e ADMIN |

## Execução local

**Pré-requisitos:** Java 17, Maven (ou Maven Wrapper) e uma instância PostgreSQL acessível.

1. Crie no PostgreSQL o banco de dados `veloxplay`.
2. Copie `.env.example` para `.env` na raiz do backend.
3. Configure pelo menos `SPRING_DATASOURCE_PASSWORD` e `JWT_SECRET` (segredo privado de, no mínimo, 32 bytes), além da URL e do usuário do banco, caso sejam diferentes dos padrões.
4. Execute na pasta do backend:

```bash
./mvnw spring-boot:run
```

No Windows, utilize `mvnw.cmd spring-boot:run`.

A API utiliza a porta **8080** por padrão. O PostgreSQL é obrigatório em execução normal; o H2 é usado exclusivamente pelos testes.

> **Segurança:** não versione o arquivo `.env`, senhas, chaves JWT nem credenciais privadas de administrador. Em produção, forneça os segredos por variáveis de ambiente ou por um gerenciador de segredos.

## Docker

O `Dockerfile` contém um build em múltiplas etapas: Maven compila a aplicação, e uma imagem Java 17 JRE executa o JAR com um usuário sem privilégios de root.

O contêiner da API **não substitui o PostgreSQL**: configure a conexão e os segredos para o ambiente de execução. O frontend compilado está na pasta `Front/` e deve ser publicado por um servidor web apropriado.

## Contas demonstrativas

O projeto contempla duas contas públicas de demonstração, criadas conforme as configurações de inicialização:

| Perfil | E-mail | Senha padrão de demonstração |
| --- | --- | --- |
| USER | `demo@veloxplay.com` | `Demo@123` |
| MODERATOR | `moderador@veloxplay.com` | `Moderador@123` |

Essas contas **não possuem privilégios administrativos reais**. Configure o administrador privado com `ADMIN_EMAIL` e `ADMIN_PASSWORD` no ambiente. Nunca publique suas credenciais.

A área `/demo` trabalha com dados fictícios isolados por token, com limites de registros e expiração automática. Ela não altera os filmes nem os usuários reais.

## Testes automatizados

Para executar a suíte de testes:

```bash
./mvnw test
```

Os testes usam **H2 em memória**, sem modificar o banco PostgreSQL real. A suíte contempla cenários de autenticação, permissões, proteção das contas de demonstração, favoritos, isolamento de sessões e operações administrativas.

## Estrutura do repositório

```text
VeloxPlay/
├── src/                 # Código-fonte e testes do backend
├── Front/               # Arquivos compilados da interface web
├── .mvn/                # Maven Wrapper
├── .env.example         # Exemplo de configuração
├── Dockerfile           # Build e imagem de execução da API
├── mvnw / mvnw.cmd      # Scripts do Maven Wrapper
├── pom.xml              # Dependências e configuração Maven
└── README.md
```

## Observações de implantação

- A inicialização depende de um PostgreSQL acessível e das variáveis de ambiente obrigatórias.
- As políticas de CORS e de proxies confiáveis devem ser configuradas conforme o domínio e a infraestrutura publicados.
- O bootstrap pode preparar contas demonstrativas e um catálogo inicial; seu comportamento é controlado pelas variáveis de ambiente.
- A aplicação utiliza atualização de esquema via Hibernate por padrão; em ambientes críticos, considere um processo controlado de migrações.

---

<div align="center">

**VeloxPlay 2.0 — Projeto de portfólio de desenvolvimento de software**

[Repositório no GitHub](https://github.com/Guttester/VeloxPlay)

</div>
