# Today's Casting Backend

Today's Casting 서비스의 Spring Boot 백엔드 서버입니다.

## 기술 스택

- Java 21
- Spring Boot 4
- Spring Data JPA
- Spring Security
- MySQL 8
- Flyway
- Swagger / Springdoc OpenAPI
- Docker, Docker Compose

## 필요 환경

- Java 21
- Docker Desktop
- Docker Compose

## 환경변수 설정

프로젝트 루트에서 `.env.example` 파일을 복사해 `.env` 파일을 생성합니다.
`.env` 파일에는 실제 비밀번호, API Key 같은 민감한 값이 들어갈 수 있으므로 Git에 올리지 않습니다.

## Docker Compose 실행

Docker Compose는 배포 서버에서 Spring Boot 애플리케이션 컨테이너를 실행합니다.
DB는 Compose 안에서 띄우지 않고 `.env`의 `LOCAL_DB_*` 값으로 외부 DB에 연결합니다.

## 로컬 확인 URL

- 서버 상태 확인: http://localhost:8080/actuator/health
- Swagger UI: http://localhost:8080/swagger-ui.html

## Flyway 마이그레이션

DB 마이그레이션 파일 위치:

```text
src/main/resources/db/migration
```

파일명 규칙:

```text
V1__init.sql
V2__create_users.sql
V3__create_daily_records.sql
```

현재 JPA 설정은 다음과 같습니다.

```yml
ddl-auto: validate
```

따라서 테이블 변경은 Hibernate 자동 생성이 아니라 Flyway 마이그레이션 파일을 통해 관리합니다.

## GitHub Actions CI/CD

GitHub Actions는 PR과 push에서 빌드 및 테스트를 수행합니다.
'dev' 브랜치에 merge 되기 전 모든 테스트와 점검을 하고 'main' 브랜치에 push 합니다.
`main` 브랜치에 push되면 DockerHub에 이미지를 올린 뒤 EC2에서 Docker Compose 배포를 갱신합니다.
DockerHub repository는 public 기준으로 사용합니다.
.env 값들은 git secrets로 관리하고 주입합니다.

## AWS EC2 배포

현재 구조는 RDS를 DB로 사용하고, EC2에서는 Docker Compose로 Spring Boot 애플리케이션 컨테이너만 실행하는 방식입니다.

## 참고 사항

- DB 접속 환경변수는 `LOCAL_DB_*`로 통일합니다.
