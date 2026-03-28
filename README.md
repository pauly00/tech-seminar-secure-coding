# 시큐어 코딩 기술세미나

SQL Injection, XSS, 파일 업로드 취약점, 경로 순회 등 대표적인 웹 취약점을 취약한 코드와 안전한 코드로 직접 비교하며 실습할 수 있는 Spring Boot 기반 프로젝트입니다.

---

## 실행 방법

#### 환경 요구사항

- Java 17+
- Maven 3.8+
- MySQL 8.0+

#### DB 설정

MySQL에 접속해 데이터베이스를 생성합니다.

```sql
mysql -u root -p
CREATE DATABASE techseminar CHARACTER SET utf8mb4;
```

`schema.sql` / `data.sql` 은 앱 시작 시 자동으로 실행됩니다.

#### DB 연결 설정

`src/main/resources/application.properties` 에서 DB 접속 정보를 확인하거나 수정합니다.

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/techseminar?...
spring.datasource.username=root
spring.datasource.password=1234
```

#### 앱 실행

```bash
mvn spring-boot:run
```

실행 후 `http://localhost:8080` 에 접속합니다.

#### 테스트 계정

| 아이디 | 비밀번호 |
|--------|----------|
| admin  | 1234     |
| alice  | 1234     |
| bob    | 1234     |

---

## 데모 파일 경로

#### 공격용 샘플 파일

각 취약점 폴더 안의 파일을 데모 페이지 업로드 폼에 바로 사용할 수 있습니다.

```
samples/
├── sql-injection/
│   └── payloads.txt           // 공격 페이로드 목록 — 복사해서 취약 폼에 붙여넣기
├── xss/
│   ├── payloads.txt           // 공격 페이로드 목록
│   └── xss.svg                // SVG XSS 파일 — 취약 파일 업로드 폼에 올려서 확인
├── file-upload/
│   ├── webshell.jsp           // [1단계] 취약 업로드 폼에 올린 뒤 웹쉘 실행 섹션에서 명령어 입력
│   ├── evil-traversal.zip     // [2단계] 취약 ZIP 해제 폼에 올려서 ZIP Slip 경로 탈출 확인
│   ├── safe-normal.zip        // 안전 ZIP 해제와 비교용 정상 파일
│   ├── malware.exe            // 안전 업로드 폼에 올리면 .exe 확장자로 차단됨
│   ├── double-ext/            // .jsp.jpg 등 이중 확장자 우회 시도 샘플
│   └── zip-bomb/              // ZIP 폭탄 — 압축 해제 시 용량 폭발 개념 확인용
└── path-traversal/
    └── payloads.txt           // 공격 경로 목록 — 복사해서 취약 폼에 붙여넣기
```

#### 업로드 저장 위치

데모 실행 시 아래 디렉토리가 자동 생성되고 파일이 저장됩니다.

```
uploads/
├── 999/          // 취약 업로드로 저장된 파일 (webshell.jsp 가 여기에 저장됨)
├── zip-vuln/     // 취약 ZIP 해제 결과 — ../가 포함된 경로로 탈출한 파일 확인 가능
└── zip-secure/   // 안전 ZIP 해제 결과 — 경로 탈출 시도가 차단된 결과만 저장됨

safe-files/       // 경로 순회 데모의 기준 디렉토리 — 이 폴더 밖으로 나가는 요청은 차단됨
```

---

## 취약점 데모 목록

### SQL Injection (`/demo/sql-injection`)

사용자 입력을 SQL 문자열에 직접 이어붙이면 쿼리 구조 자체를 변조할 수 있습니다.

| 구분 | 내용 |
|------|------|
| 취약 코드 | 문자열 연결: `"... WHERE user_id = '" + userId + "'"` |
| 안전 코드 | `"... WHERE user_id = ?"` + 파라미터 바인딩 |
| OWASP | A03:2021 Injection · CVE-2017-5638 |

#### 공격 예시

- 로그인 우회: ID = `admin' --` / PW = 아무거나
- 전체 행 조회: `%' OR '1'='1`
- 계정 전체 탈취: `%' UNION SELECT 1,user_id,user_password,user_name,user_email,user_gender,0 FROM user_tb --`

---

### XSS (`/demo/xss`)

사용자 입력을 HTML 인코딩 없이 출력하면 악성 스크립트가 다른 사용자 브라우저에서 실행됩니다.

| 구분 | 내용 |
|------|------|
| 취약 코드 | `th:utext` — HTML 태그를 그대로 렌더링 |
| 안전 코드 | `th:text` — `<`, `>` 등을 HTML 엔티티로 자동 인코딩 |
| OWASP | A03:2021 Injection · CVE-2019-11358 |

#### 공격 예시

- Stored XSS: `<script>alert('XSS!')</script>`
- 쿠키 탈취: `<script>fetch('/demo/steal?c='+document.cookie)</script>`
- 탈취된 쿠키 확인: `/demo/steal`

---

### 파일 업로드 취약점 (`/demo/file-upload`)

확장자 검사 없이 파일을 저장하면 `.jsp` 같은 실행 파일을 업로드해 서버에서 직접 명령어를 실행할 수 있습니다.

| 구분 | 내용 |
|------|------|
| 취약 코드 | 확장자 검사 없음, 원본 파일명 그대로 저장 |
| 안전 코드 | 화이트리스트 확장자 검사 + UUID 파일명으로 저장 |
| OWASP | A04:2021 Insecure Design · CVE-2021-22005 |

#### 공격 파일 (`samples/file-upload/` 폴더)

| 파일 | 사용 방법 |
|------|-----------|
| `webshell.jsp` | 취약 업로드 폼에 올린 뒤, 웹쉘 실행 섹션에서 `whoami` 등 명령어 입력 |
| `evil-traversal.zip` | 취약 ZIP 해제 폼에 올리면 `../` 경로로 `uploads/` 밖에 파일이 생성됨 |

---

### 경로 순회 (`/demo/path-traversal`)

경로를 정규화하지 않으면 `../` 를 이용해 허용된 디렉토리 밖의 파일에 접근할 수 있습니다.

| 구분 | 내용 |
|------|------|
| 기준 경로 | `./safe-files/` |
| 취약 코드 | `safeFilesDir + "/" + filename` — 경로 검증 없음 |
| 안전 코드 | `normalize()` + `startsWith(basePath)` — 기준 경로 밖이면 차단 |
| OWASP | A01:2021 Broken Access Control · CVE-2021-41773 |

#### 공격 예시

- `../src/main/resources/application.properties` — DB 비밀번호 등 설정 파일 노출
- `../../pom.xml` — 프로젝트 의존성 구조 노출
- `../../../Windows/win.ini` — Windows 시스템 파일 접근

---

## 기술 스택

| 항목 | 버전 |
|------|------|
| Java | 17+ |
| Spring Boot | 3.2.3 |
| Thymeleaf | 3.x |
| Spring Security | 6.x (전체 비활성화 — 데모 목적) |
| MySQL | 8.0+ |

---

## 프로젝트 구조

```
src/main/java/com/techseminar/
├── config/
│   └── SecurityConfig.java              // Spring Security 전체 비활성화 설정
├── controller/demo/
│   ├── SqlInjectionController.java
│   ├── XssController.java
│   ├── CookieStealController.java       // 탈취된 쿠키 수신 시뮬레이션
│   ├── FileUploadDemoController.java
│   └── PathTraversalController.java
└── service/
    ├── UserService.java                 // loginVulnerable / loginSecure
    ├── BbsService.java                  // searchVulnerable / searchSecure
    └── FileService.java                 // upload / extract / executeWebshell

src/main/resources/
├── application.properties              // 업로드 경로, DB 설정
├── schema.sql                          // 테이블 생성
├── data.sql                            // 초기 테스트 데이터
├── static/style.css
└── templates/
    ├── index.html
    ├── fragments/                       // head, navbar 공통 레이아웃
    └── demo/                           // 각 취약점별 데모 페이지

samples/                                // 취약점별 공격 샘플 파일 (sql-injection / xss / file-upload / path-traversal)
uploads/                                // 데모 실행 시 자동 생성되는 업로드 저장 위치
safe-files/                             // 경로 순회 데모 기준 디렉토리
```
