# 시큐어 코딩 기술세미나

취약한 코드와 안전한 코드를 나란히 비교하는 Spring Boot 기반 실습 데모 프로젝트

## 실행 방법

**환경**
- Java 17+
- Maven 3.8+
- MySQL 8.0+

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

## 취약점 데모 목록

### SQL Injection (`/demo/sql-injection`)

| 구분 | 내용 |
|------|------|
| 취약 코드 | 문자열 연결: `"... WHERE user_id = '" + userId + "'"` |
| 안전 코드 | `"... WHERE user_id = ?"` + 파라미터 바인딩 |

공격 예시
- 로그인 우회: ID = `admin' --` / PW = 아무거나
- 전체 조회: `%' OR '1'='1`
- 계정 탈취: `%' UNION SELECT 1,user_id,user_password,user_name,user_email,user_gender,0 FROM user_tb --`

### XSS (`/demo/xss`)

| 구분 | 내용 |
|------|------|
| 취약 코드 | `th:utext` (HTML 그대로 렌더링) |
| 안전 코드 | `th:text` (HTML 자동 인코딩) |

공격 예시
- Stored XSS: `<script>alert('XSS!')</script>`
- 쿠키 탈취: `<script>fetch('/demo/steal?c='+document.cookie)</script>`
- 탈취 결과 확인: `/demo/steal`

### 파일 업로드 취약점 (`/demo/file-upload`)

| 구분 | 내용 |
|------|------|
| 취약 코드 | 확장자 검사 없음, 원본 파일명 저장 |
| 안전 코드 | 화이트리스트 검사 + UUID 파일명 |

공격 파일

| 파일 | 설명 |
|------|------|
| `webshell.jsp` | 업로드 후 `/demo/file-upload/webshell?cmd=whoami`로 RCE 실행 |
| `zipslip.zip` | ZIP 해제 시 임의 경로 파일 쓰기 |

ZIP Slip 악성 파일 생성 (Python)

```python
import zipfile
zf = zipfile.ZipFile('zipslip.zip', 'w')
zf.writestr('../../uploads/pwned.txt', 'ZIP Slip success!')
zf.writestr('normal.txt', 'normal file')
zf.close()
```

### 경로 순회 (`/demo/path-traversal`)

| 구분 | 내용 |
|------|------|
| 기준 경로 | `./safe-files/` |
| 취약 코드 | `safeFilesDir + "/" + filename` |
| 안전 코드 | `normalize()` + `startsWith(basePath)` |

공격 예시
- `../src/main/resources/application.properties` (DB 비밀번호 노출)
- `../../pom.xml` (프로젝트 구조 노출)
- `../../../Windows/win.ini` (Windows 시스템 파일)

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
├── application.properties
├── schema.sql
├── data.sql
├── static/style.css
└── templates/
    ├── index.html
    ├── fragments/
    └── demo/

safe-files/                     경로 순회 데모용 기준 디렉토리
```
