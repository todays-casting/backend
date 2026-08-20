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

Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

macOS / Linux:

```bash
cp .env.example .env
```

기본 로컬 환경변수는 다음과 같습니다.

```env
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=local
DOCKERHUB_USERNAME=your-dockerhub-username

LOCAL_DB_URL=jdbc:mysql://localhost:3306/todays_casting?serverTimezone=Asia/Seoul&characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true
LOCAL_DB_USERNAME=todays
LOCAL_DB_PASSWORD=change-me

# AWS S3
AWS_S3_BUCKET_NAME=your-s3-bucket-name
AWS_S3_REGION=your-s3-region
WITHDRAWN_EMAIL_HMAC_SECRET=replace-with-a-random-secret-at-least-32-bytes-long

TZ=Asia/Seoul
```

`.env` 파일에는 실제 비밀번호, API Key 같은 민감한 값이 들어갈 수 있으므로 Git에 올리지 않습니다.

## Docker Compose 실행

Docker Compose는 배포 서버에서 Spring Boot 애플리케이션 컨테이너를 실행합니다.
DB는 Compose 안에서 띄우지 않고 `.env`의 `LOCAL_DB_*` 값으로 외부 DB에 연결합니다.

```bash
docker compose up -d
```

컨테이너 상태 확인:

```bash
docker compose ps
```

애플리케이션 로그 확인:

```bash
docker compose logs -f app
```

컨테이너 종료:

```bash
docker compose down
```

## 로컬 확인 URL

- 서버 상태 확인: http://localhost:8080/actuator/health
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs

## MySQL 접속 정보

일반 개발에서는 아래 계정을 사용합니다.

```text
Host: 127.0.0.1
Port: 3306
Database: todays_casting
Username: .env LOCAL_DB_USERNAME
Password: .env LOCAL_DB_PASSWORD
```


root 또는 관리자 계정은 긴급 DB 관리가 필요할 때만 사용합니다.

```text
Username: root
Password: 필요한 대상 DB의 관리자 비밀번호를 사용합니다.
```

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

## GitHub 업로드

원격 저장소:

```text
https://github.com/likelion14th-hackathon/backend.git
```

처음 업로드할 때:

```bash
git init
git add .
git commit -m "chore: initial backend setup"
git branch -M main
git remote add origin https://github.com/likelion14th-hackathon/backend.git
git push -u origin main
```

`.env`는 `.gitignore`에 포함되어 있으므로 Git에 올라가지 않습니다.

## GitHub Actions CI/CD

GitHub Actions는 PR과 push에서 빌드 및 테스트를 수행합니다.
`main` 브랜치에 push되면 DockerHub에 이미지를 올린 뒤 EC2에서 Docker Compose 배포를 갱신합니다.
DockerHub repository는 public 기준으로 사용합니다.

GitHub Repository Secrets에 아래 값을 등록합니다.

```text
DOCKERHUB_USERNAME
DOCKERHUB_TOKEN
EC2_HOST
EC2_USERNAME
EC2_SSH_KEY
EC2_DEPLOY_PATH
LOCAL_DB_URL
LOCAL_DB_USERNAME
LOCAL_DB_PASSWORD
AWS_S3_BUCKET_NAME
AWS_S3_REGION
WITHDRAWN_EMAIL_HMAC_SECRET
```

- `DOCKERHUB_USERNAME`: DockerHub 계정명입니다.
- `DOCKERHUB_TOKEN`: DockerHub Access Token입니다.
- `EC2_HOST`: EC2 퍼블릭 IP 또는 도메인입니다.
- `EC2_USERNAME`: EC2 SSH 사용자명입니다. Amazon Linux는 보통 `ec2-user`입니다.
- `EC2_SSH_KEY`: EC2 접속용 private key 전체 내용입니다.
- `EC2_DEPLOY_PATH`: EC2 안에서 배포 저장소를 둘 디렉터리입니다. 예시는 `/home/ec2-user/backend`입니다.
- `LOCAL_DB_URL`: 배포 환경에서 사용할 RDS JDBC URL입니다.
- `LOCAL_DB_USERNAME`: 배포 환경에서 사용할 RDS 사용자명입니다.
- `LOCAL_DB_PASSWORD`: 배포 환경에서 사용할 RDS 비밀번호입니다.
- `AWS_S3_BUCKET_NAME`: 이미지 파일을 저장할 S3 버킷명입니다.
- `AWS_S3_REGION`: S3 버킷이 생성된 AWS 리전입니다.
- `WITHDRAWN_EMAIL_HMAC_SECRET`: 탈퇴 이메일 재가입 차단용 HMAC 비밀키입니다. GitHub Secrets와 서버 환경변수에 같은 값을 등록합니다.

## AWS EC2 배포

현재 구조는 RDS를 DB로 사용하고, EC2에서는 Docker Compose로 Spring Boot 애플리케이션 컨테이너만 실행하는 방식입니다.

1. EC2 인스턴스를 생성합니다.
   - Amazon Linux 기준으로 문서를 작성합니다.
   - 해커톤 초기에는 `t3.micro` 또는 `t2.micro` 사용 가능

2. 보안 그룹 인바운드 규칙을 설정합니다.
   - SSH: `22`
   - 백엔드 API: `8080`
   - 추후 Nginx/HTTPS 적용 시: `80`, `443`

3. EC2에 접속합니다.

```bash
ssh -i <키페어파일.pem> ec2-user@<EC2_PUBLIC_IP>
```

4. Docker, Docker Compose, Git을 설치합니다.

```bash
sudo yum update -y
sudo yum install -y docker git
sudo systemctl enable docker
sudo systemctl start docker
sudo usermod -aG docker ec2-user
```

권한 적용을 위해 SSH 접속을 끊고 다시 접속합니다.

5. 프로젝트를 clone합니다.

```bash
cd /home/ec2-user
git clone https://github.com/likelion14th-hackathon/backend.git
cd backend
```

6. `.env` 파일을 준비합니다.

CD 실행 시 GitHub Secrets의 `LOCAL_DB_URL`, `LOCAL_DB_USERNAME`, `LOCAL_DB_PASSWORD` 값으로 EC2 서버에 `.env`가 생성됩니다.
애플리케이션은 환경 구분 없이 `LOCAL_DB_*` 값을 datasource로 사용합니다.

수동으로 먼저 실행해야 한다면 `.env.example`을 복사한 뒤 `LOCAL_DB_*`에 RDS 접속 정보를 넣습니다.

7. Docker Compose로 실행합니다.

```bash
docker compose up -d
```

8. 배포 상태를 확인합니다.

```bash
docker compose ps
docker compose logs -f app
```

브라우저에서 아래 주소를 확인합니다.

```text
http://<EC2_PUBLIC_IP>:8080/actuator/health
http://<EC2_PUBLIC_IP>:8080/swagger-ui.html
```

## 참고 사항

- IntelliJ 로컬 실행은 로컬 DB의 `LOCAL_DB_*` 값을 사용합니다.
- EC2 배포 실행은 RDS 접속 정보가 들어간 `LOCAL_DB_*` 값을 사용합니다.
- DB 접속 환경변수는 `LOCAL_DB_*`로 통일합니다.
