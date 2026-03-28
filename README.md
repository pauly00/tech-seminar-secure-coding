# 시큐어 코딩 기술세미나

취약한 코드와 안전한 코드를 나란히 비교하는 Spring Boot 기반 실습 데모 프로젝트

## 실행 방법

**환경**

: Java 17+
: Maven 3.8+
: MySQL 8.0+

**DB 설정**

```sql
mysql -u root -p
CREATE DATABASE techseminar CHARACTER SET utf8mb4;
```

`schema.sql` / `data.sql` 은 앱 시작 시 자동 실행됨

**DB 연결** (`src/main/resources/application.properties`)

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/techseminar?...
spring.datasource.username=root
spring.datasource.password=1234
```

**실행**

```bash
mvn spring-boot:run
```

`http://localhost:8080` 접속

**테스트 계정**

| 아이디 | 비밀번호 |
|--------|----------|
| admin  | 1234     |
| alice  | 1234     |
| bob    | 1234     |

## 데모 파일 경로

### 공격용 샘플 파일 (취약점별 폴더로 구성)

```
samples/
├── sql-injection/
│   └── payloads.txt           ← 공격 페이로드 목록 (복사해서 취약 폼에 붙여넣기)
├── xss/
│   ├── payloads.txt           ← 공격 페이로드 목록
│   └── xss.svg                ← SVG 기반 XSS 파일 (취약 업로드 폼에 업로드)
├── file-upload/
│   ├── webshell.jsp           ← [1단계] 취약 업로드 폼에 업로드 → 웹쉘 실행
│   ├── evil-traversal.zip     ← [2단계] 취약 ZIP 해제 폼에 업로드 → ZIP Slip 확인
│   ├── safe-normal.zip        ← 안전 ZIP 해제 비교용
│   ├── malware.exe            ← .exe 차단 확인용 (안전 업로드에 올리면 차단됨)
│   ├── double-ext/            ← 이중 확장자 우회 시도 샘플
│   └── zip-bomb/              ← ZIP 폭탄 샘플
└── path-traversal/
    └── payloads.txt           ← 공격 경로 목록 (복사해서 취약 폼에 붙여넣기)
```

### 업로드 후 저장 위치 (앱 실행 시 자동 생성)

```
uploads/
├── 999/                       ← 취약 업로드 결과 (webshell.jsp 여기 저장됨)
├── zip-vuln/                  ← 취약 ZIP 해제 결과
└── zip-secure/                ← 안전 ZIP 해제 결과

safe-files/                    ← 경로 순회 데모 기준 디렉토리 (이 폴더 밖으로 나가면 차단)
```

## 취약점 데모 목록

### SQL Injection (`/demo/sql-injection`)

| 구분 | 내용 |
|------|------|
| 취약 코드 | 문자열 연결: `"... WHERE user_id = '" + userId + "'"` |
| 안전 코드 | `"... WHERE user_id = ?"` + 파라미터 바인딩 |
| OWASP | A03:2021 Injection · CVE-2017-5638 |

공격 예시

: 로그인 우회: ID = `admin' --` / PW = 아무거나
: 전체 조회: `%' OR '1'='1`
: 계정 탈취: `%' UNION SELECT 1,user_id,user_password,user_name,user_email,user_gender,0 FROM user_tb --`

### XSS (`/demo/xss`)

| 구분 | 내용 |
|------|------|
| 취약 코드 | `th:utext` (HTML 그대로 렌더링) |
| 안전 코드 | `th:text` (HTML 자동 인코딩) |
| OWASP | A03:2021 Injection · CVE-2019-11358 |

공격 예시

: Stored XSS: `<script>alert('XSS!')</script>`
: 쿠키 탈취: `<script>fetch('/demo/steal?c='+document.cookie)</script>`
: 탈취 결과 확인: `/demo/steal`

### 파일 업로드 취약점 (`/demo/file-upload`)

| 구분 | 내용 |
|------|------|
| 취약 코드 | 확장자 검사 없음, 원본 파일명 저장 |
| 안전 코드 | 화이트리스트 검사 + UUID 파일명 |
| OWASP | A04:2021 Insecure Design · CVE-2021-22005 |

공격 파일 (`samples/file-upload/` 폴더에서 가져올 것)

| 파일 | 설명 |
|------|------|
| `samples/file-upload/webshell.jsp` | 취약 업로드 후 `/demo/file-upload/webshell?cmd=whoami`로 RCE 실행 |
| `samples/file-upload/evil-traversal.zip` | 취약 ZIP 해제 시 임의 경로 파일 쓰기 (ZIP Slip) |

### 경로 순회 (`/demo/path-traversal`)

| 구분 | 내용 |
|------|------|
| 기준 경로 | `./safe-files/` |
| 취약 코드 | `safeFilesDir + "/" + filename` |
| 안전 코드 | `normalize()` + `startsWith(basePath)` |
| OWASP | A01:2021 Broken Access Control · CVE-2021-41773 |

공격 예시

: `../src/main/resources/application.properties` (DB 비밀번호 노출)
: `../../pom.xml` (프로젝트 구조 노출)
: `../../../Windows/win.ini` (Windows 시스템 파일)

## 기술 스택

| 항목 | 버전 |
|------|------|
| Java | 17+ |
| Spring Boot | 3.2.3 |
| Thymeleaf | 3.x |
| Spring Security | 6.x (전체 비활성화, 데모 목적) |
| MySQL | 8.0+ |

## 프로젝트 구조

```
src/main/java/com/techseminar/
├── config/
│   └── SecurityConfig.java
├── controller/
│   ├── HomeController.java
│   └── demo/
│       ├── SqlInjectionController.java
│       ├── XssController.java
│       ├── CookieStealController.java
│       ├── FileUploadDemoController.java
│       └── PathTraversalController.java
└── service/
    ├── UserService.java        (loginVulnerable / loginSecure)
    ├── BbsService.java         (searchVulnerable / searchSecure)
    └── FileService.java        (upload / extract / executeWebshell)

src/main/resources/
├── application.properties      (app.upload.dir / app.safe-files.dir 설정)
├── schema.sql
├── data.sql
├── static/style.css
└── templates/
    ├── index.html
    ├── fragments/
    └── demo/

samples/                        ← 취약점별 공격 샘플 (sql-injection / xss / file-upload / path-traversal)
uploads/                        ← 업로드된 파일 저장 위치 (앱 실행 시 생성)
safe-files/                     ← 경로 순회 데모 기준 디렉토리
```
