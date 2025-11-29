# 📘 Desafio Cloud-Native: Task Manager API

Uma aplicação de gerenciamento de tarefas (ToDo List) robusta, desenvolvida para demonstrar a orquestração de microsserviços utilizando **Spring Boot**, **Docker** e **Kubernetes**.

O projeto ilustra a migração de um ambiente de desenvolvimento local para um cluster Kubernetes, com foco em persistência de dados e descoberta de serviços.

## 📑 Índice

* [Sobre o Projeto](#-sobre-o-projeto)
* [Tecnologias Utilizadas](#-tecnologias-utilizadas)
* [Configuração do Ambiente](#-configuração-do-ambiente)
* [Passo 1: Banco de Dados Customizado](#passo-1-banco-de-dados-customizado-parte-3---q1)
* [Passo 2: Migração para Kubernetes](#passo-2-migração-para-kubernetes-parte-3---q2)
* [Passo 3: Aplicação Spring Boot](#passo-3-aplicação-spring-boot-parte-4---q1)
* [Executando a Aplicação](#-executando-a-aplicação)
* [Endpoints da API](#-endpoints-da-api)
* [Exemplos de Requisição](#-requisições-e-respostas-da-api)

-----

## 🎯 Sobre o Projeto

Este repositório é o resultado de um desafio técnico focado em DevOps e Engenharia de Software. O objetivo principal é desenvolver uma API RESTful e implantá-la em um ambiente conteinerizado, garantindo:

1.  Criação de imagens Docker otimizadas (Multi-stage build).
2.  Personalização de imagens de Banco de Dados.
3.  Orquestração via Kubernetes (Minikube).
4.  Comunicação entre serviços via DNS interno do cluster.

-----

## 🚀 Tecnologias Utilizadas

  * **Java 21**: Linguagem base (LTS).
  * **Spring Boot 3.4**: Framework para criação da API REST.
      * *Spring Data JPA*: Persistência.
      * *Spring Web*: MVC e REST.
  * **MySQL 8**: Banco de dados relacional.
  * **Docker**: Conteinerização e criação de imagens.
  * **Kubernetes (Minikube)**: Orquestração local.
  * **Maven**: Gerenciamento de dependências.

-----

## ⚙️ Configuração do Ambiente

Pré-requisitos necessários para rodar este projeto:

  * **WSL2** (Ubuntu 24.04 ou superior)
  * **Docker Desktop** (ou Engine)
  * **Minikube** v1.37+
  * **Kubectl**
  * **Java JDK 21**

-----

## 🛠️ Detalhamento da Implementação

### Passo 1: Banco de Dados Customizado (Parte 3 - Q1)

Não utilização da imagem padrão do MySQL. Criação da imagem personalizada que autoinicializa o esquema do banco.

**Dockerfile do MySQL:**

```dockerfile
FROM mysql:8.0
ENV MYSQL_DATABASE=desafio_db
ENV MYSQL_ROOT_PASSWORD=root_secreta
COPY init.sql /docker-entrypoint-initdb.d/
EXPOSE 3306
```

**Comandos de Build e Push:**

```bash
docker build -t <SEU_USUARIO>/mysql-custom-challenge:v1 -f Dockerfile-mysql .
docker push <SEU_USUARIO>/mysql-custom-challenge:v1
```

### Passo 2: Migração para Kubernetes (Parte 3 - Q2)

No Kubernetes, o banco de dados é exposto via **Service**, permitindo que a API o encontre pelo nome `mysql-service`, independentemente do IP do Pod.

**Arquivo:** `mysql-k8s.yaml` (Resumo)

```yaml
kind: Service
metadata:
  name: mysql-service
spec:
  ports:
    - port: 3306
  selector:
    app: mysql-db
---
kind: Deployment
metadata:
  name: mysql-deployment
spec:
  # ... especificações do container usando a imagem customizada
```

### Passo 3: Aplicação Spring Boot (Parte 4 - Q1)

A API foi desenvolvida com 5 endpoints CRUD e empacotada usando **Multi-stage build** para reduzir o tamanho final da imagem (apenas JRE Alpine).

**Dockerfile da Aplicação:**

```dockerfile
# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Manifesto Kubernetes (`app-k8s.yaml`):**
Injeta a URL do banco via variável de ambiente.

```yaml
env:
  - name: SPRING_DATASOURCE_URL
    value: "jdbc:mysql://mysql-service:3306/desafio_db?allowPublicKeyRetrieval=true&useSSL=false"
```

-----

## ▶️ Executando a Aplicação

1.  **Inicie o Minikube:**

    ```bash
    minikube start
    ```

2.  **Aplique os Manifestos:**

    ```bash
    kubectl apply -f mysql-k8s.yaml
    kubectl apply -f app-k8s.yaml
    ```

3.  **Aguarde os Pods:**

    ```bash
    kubectl get pods
    # Aguarde status "Running" em ambos
    ```

4.  **Acesse a API:**
    Como o Minikube roda isolado, use o comando abaixo para gerar um túnel de acesso:

    ```bash
    minikube service spring-service --url
    # Exemplo de saída: http://127.0.0.1:36897
    ```

-----

## 🔌 Endpoints da API

| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| `GET` | `/tasks` | Lista todas as tarefas |
| `GET` | `/tasks/{id}` | Busca uma tarefa por ID |
| `POST` | `/tasks` | Cria uma nova tarefa |
| `PUT` | `/tasks/{id}` | Atualiza uma tarefa existente |
| `DELETE` | `/tasks/{id}` | Remove uma tarefa |

-----

## 📡 Requisições e Respostas da API

Exemplos utilizando `curl`. Substitua `BASE_URL` pela URL gerada pelo Minikube.

### 1\. Criar Tarefa (POST)

**Requisição:**

```bash
curl -X POST BASE_URL/tasks \
     -H "Content-Type: application/json" \
     -d '{"description": "Finalizar documentação", "status": "PENDING"}'
```

**Resposta:**

```json
{
  "id": 4,
  "description": "Finalizar documentação",
  "status": "PENDING"
}
```

### 2\. Listar Tarefas (GET)

**Requisição:**

```bash
curl BASE_URL/tasks
```

**Resposta:**

```json
[
  {
    "id": 1,
    "description": "Aprender Docker",
    "status": "DONE"
  },
  {
    "id": 4,
    "description": "Finalizar documentação",
    "status": "PENDING"
  }
]
```

-----

*Desenvolvido por Leandro Medeiros.*
