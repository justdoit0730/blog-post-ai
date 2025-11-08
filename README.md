# AUTO CAFE project

## ⚙️ Features

### 1️⃣ AI 글쓰기
- 주제와 프롬프트를 입력하면 **AI가 자동으로 글을 생성**합니다.
- OpenAI GPT 모델(`gpt-4o-mini`) 기반으로 자연스럽고 문맥에 맞는 글을 작성합니다.
- **설정 가능 항목:**
  - `max_tokens` : 생성 글의 최대 길이
  - `temperature` : 창의성 및 랜덤성 조절

---

### 2️⃣ Naver Cafe 포스팅
- **카페 ID**, **게시판 ID**, **제목**, **내용**을 입력하여  
  사용자가 직접 작성한 글을 **네이버 카페에 게시**할 수 있습니다.
- 네이버 공식 **Cafe API**를 활용하여 안정적으로 포스팅 처리합니다.
- **인증 방식:**
  - 기본 제공된 API 인증 사용
  - 또는 사용자가 자신의 계정으로 직접 인증하여 연결 가능

---

### 3️⃣ Naver Cafe AI 포스팅
- **카페 ID**, **게시판 ID**, **주제**, **프롬프트**를 입력하면  
  AI가 자동으로 글을 생성하고, **즉시 네이버 카페에 게시**합니다.
- 글 생성 → 게시까지 한 번에 처리되는 **AI 자동 포스팅 기능**입니다.
- 커뮤니티 운영, 카페 자동화, 마케팅용 콘텐츠 제작에 적합합니다.

---

## 🔐 Access & Usage Rules
- 위의 모든 **기능은 회원가입 후 사용 가능**합니다.
- AI 관련 기능은 계정별로 **토큰 제한** 및 **temperature 설정**을 통해 제어할 수 있습니다.
- 네이버 카페 API는 **기본 제공 + 사용자 개인 인증** 방식 모두 지원합니다.

---

> 💡 *이 프로젝트는 AI 자동 글쓰기와 네이버 카페 포스팅을 통합한 콘텐츠 자동화 플랫폼입니다.*

# 🚀 Web Project Specification

## 🧠 Language & Build Tool
- **Java 17**
- **Gradle 8.5**

---

## 🌱 Frameworks
- **Spring Boot** `3.4.5`
- **Spring Security** `3.4.5`
- **Spring Mustache** `3.4.5`
- **Spring Data JPA** (with `mariadb-java-client:3.4.0`)

---

## 🤖 AI & External Libraries
- **OpenAI GPT API**
  - Library: `openai-gpt3-java:service:0.18.2`
  - **Current Model:** `gpt-4o-mini`
  - **Upgrade Plan:** Future upgrade to `GPT-5` (or the latest `GPT-4.5` / `GPT-5` series)  
    for enhanced reasoning performance, lower latency, and cost optimization.

- **AWS SDK for S3**
  - Library: `software.amazon.awssdk:s3:2.20.35`

---

## ☁️ AWS Infrastructure
- **AWS S3** – File storage
- **AWS EC2** – Application hosting
- **AWS RDS** – Database service
- **AWS IAM** – Access & permission management

---

## 💾 Database
- **MariaDB**
  - Driver: `mariadb-java-client:3.4.0`
  - ORM: Spring Data JPA

---

## 🧩 Project Template
- **Base Template:** [https://www.temha.io/template/](https://www.temha.io/template/)

---

## 📁 Summary

| Category | Stack |
|-----------|--------|
| Build Tool | Gradle 8.5 |
| Language | Java 17 |
| Framework | Spring Boot / Security / Mustache / JPA |
| AI | OpenAI GPT API (Model: gpt-4o-mini → Planned upgrade: GPT-5) |
| Cloud | AWS (S3, EC2, RDS, IAM) |
| Database | MariaDB |
| Template | temha.io project base |

---
