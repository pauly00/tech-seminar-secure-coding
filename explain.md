# 시연 순서 가이드

## 1. 발표 배경 (2분) — 슬라이드만

청중이 "이게 왜 중요한가"를 느끼게 만드는 구간

| 연도 | 사고 | 원인 | 피해 |
|------|------|------|------|
| 2017 | 에퀴팩스 해킹 | 입력값 검증 미흡 (CVE-2017-5638) | 1억 4,700만 명 개인정보 유출 |
| 2021 | Log4Shell | 입력값 검증 미흡 — JNDI Injection (CVE-2021-44228) | 전 세계 수억 대 서버 동시 마비 |
| 2025 | 국내 통신사 대규모 해킹 | 통신 인프라 침투 | 대규모 개인정보 유출 · 통신 인프라 위협 |
| 2025 | 롯데카드 개인정보 유출 | 웹쉘 업로드를 통한 서버 침투 | 국내 카드사 대량 개인정보 유출 |

> "에퀴팩스 해킹은 패치가 없어서 당한 게 아닙니다.
> 개발자가 입력값을 검증하지 않은 코드 한 줄이 시작이었습니다."

## 2. 시큐어코딩이란 (2분) — 슬라이드만

버그 vs 취약점 구분:

```
버그       : 의도하지 않은 동작 (기능 오류)
취약점     : 공격자가 악용 가능한 동작 (보안 위협)

모든 취약점은 버그지만, 모든 버그가 취약점은 아님
```

수정 비용 비교 (NIST 기준):

```
설계 단계  : 1
개발 단계  : 10
테스트 단계: 100
운영 이후  : 1,000 ~ 10,000
```

## 3. 주요 취약점 (8분) — 핵심 시연 구간

> 브라우저에서 `http://localhost:8080` 열어둔 상태에서 시작

---

### 3-1. SQL Injection (3분)

**분류**: OWASP A03:2021 Injection · CVE-2017-5638 (Equifax 해킹)

**취약점**: 사용자 입력을 SQL 문자열에 직접 이어붙여 쿼리 구조 자체를 변조 가능

```java
// 취약 코드 — 입력값이 SQL 구조를 바꿈
String sql = "SELECT * FROM user_tb WHERE user_id = '" + userId + "'";

// ID에 admin' -- 를 넣으면 실제 실행되는 쿼리:
// SELECT * FROM user_tb WHERE user_id = 'admin' --' AND ...
// → 비밀번호 조건이 주석 처리되어 우회
```

**한 줄 수정**: `?` 자리표시자 + `pstmt.setString(1, userId)` — PreparedStatement 파라미터 바인딩

```java
// 안전 코드 — 입력값은 항상 데이터로만 처리됨
String sql = "SELECT * FROM user_tb WHERE user_id = ?";
pstmt.setString(1, userId);
```

**시연 순서**

1. `/demo/sql-injection` 이동
2. 취약 검색창에 `%' OR '1'='1` 입력 → 전체 행 노출 확인
3. `%' UNION SELECT 1,user_id,user_password,user_name,user_email,user_gender,0 FROM user_tb --` 입력 → DB 계정 전체 탈취 확인
4. 취약 로그인에 ID = `admin' --`, PW = 아무거나 → 로그인 우회 확인
5. 안전 검색창에 동일한 문자열 입력 → 0개 행 반환 확인
6. "PreparedStatement 파라미터 바인딩 한 줄로 막힙니다"

---

### 3-2. XSS (2분 30초)

**분류**: OWASP A03:2021 Injection · CVE-2019-11358 (jQuery XSS, 수천 개 사이트 영향)

**취약점**: 사용자 입력을 HTML 이스케이프 없이 그대로 출력 → 악성 스크립트가 다른 사용자 브라우저에서 실행

```html
<!-- 취약 코드 — HTML 태그가 그대로 실행됨 -->
<span th:utext="${comment}"></span>

<!-- 안전 코드 — <, > 등이 &lt; &gt; 로 인코딩됨 -->
<span th:text="${comment}"></span>
```

**한 줄 수정**: `th:utext` → `th:text` 로 변경

**시연 순서**

1. `/demo/xss` 이동
2. 취약 댓글창에 `<script>alert('XSS!')</script>` 입력 → 팝업 확인
3. `<script>fetch('/demo/steal?c='+document.cookie)</script>` 입력 → `/demo/steal`에서 탈취된 쿠키 확인
4. 취약 반사형 폼에 `<script>alert(1)</script>` 입력 → alert 실행 확인
5. 안전 댓글창에 동일한 스크립트 입력 → 텍스트로 그대로 출력됨 확인
6. "`th:text` 하나로 막힙니다" — 코드 비교 가리키기
7. 댓글 초기화 버튼 클릭

---

### 3-3. 파일 업로드 취약점 (2분 30초)

**분류**: OWASP A04:2021 Insecure Design · CVE-2021-22005 (VMware vCenter 웹쉘 업로드 → RCE)

**취약점**: 확장자 검사 없이 파일 저장 → `.jsp` 같은 실행 파일 업로드 후 서버에서 직접 실행 가능 (RCE)

```java
// 취약 코드 — 모든 파일 허용, 원본 파일명 그대로 저장
file.transferTo(dir.resolve(originalFilename).toFile());

// 안전 코드 — 화이트리스트 검사 + UUID 파일명으로 실행 불가
if (!ALLOWED_EXTENSIONS.contains(extension)) {
    return "허용되지 않은 파일 형식";          // ← 이 한 줄이 핵심
}
String savedName = UUID.randomUUID() + "." + extension;
```

**한 줄 수정**: `if (!ALLOWED_EXTENSIONS.contains(extension)) return "차단";`

**시연 순서**

1. `/demo/file-upload` 이동
2. 취약 업로드에 `samples/file-upload/webshell.jsp` 선택 → 업로드
3. 저장 경로 확인 (`uploads/999/webshell.jsp`)
4. 웹쉘 실행 섹션에서 `whoami`, `dir` 입력 → 서버 명령 실행 확인
5. 취약 ZIP 해제에 `samples/file-upload/evil-traversal.zip` 업로드 → 해제 경로가 `uploads/` 밖으로 탈출하는 것 확인
6. 안전 ZIP 해제에 동일한 파일 → BLOCKED 확인
7. 안전 업로드에 `samples/file-upload/webshell.jsp` → 차단 확인
8. "확장자 화이트리스트 한 줄로 막힙니다"

---

### 3-4. 경로 순회 (1분)

**분류**: OWASP A01:2021 Broken Access Control · CVE-2021-41773 (Apache HTTP Server 경로 순회 → RCE)

**취약점**: 파일 경로를 정규화하지 않으면 `../` 를 이용해 기준 디렉토리 밖의 파일 접근 가능

```java
// 취약 코드 — 경로 정규화 없이 그대로 접근
Path file = Paths.get(safeFilesDir + "/" + filename);

// 안전 코드 — normalize 후 기준 경로 밖이면 차단
Path resolved = Paths.get(safeFilesDir).resolve(filename).normalize();
if (!resolved.startsWith(basePath)) {          // ← 이 한 줄이 핵심
    throw new SecurityException("경로 순회 감지");
}
```

**한 줄 수정**: `if (!resolved.normalize().startsWith(basePath)) throw ...;`

**시연 순서**

1. `/demo/path-traversal` 이동
2. 취약 폼에 `../src/main/resources/application.properties` 입력 → DB 비밀번호 포함 설정 파일 노출 확인
3. `../../pom.xml` 입력 → 프로젝트 구조 노출 확인
4. 안전 폼에 동일한 경로 입력 → "경로 순회 감지" 차단 확인
5. "`normalize() + startsWith()` 두 줄로 막힙니다"

---

## 4. 방어 도구 (2분) — 슬라이드만

SAST vs DAST 비교:

```
SAST (정적 분석)
  : 코드를 실행하지 않고 소스 분석
  : 도구 — SonarQube, Checkmarx, SpotBugs
  : 장점 — 개발 단계에서 조기 발견
  : 단점 — 오탐(False Positive) 많음

DAST (동적 분석)
  : 실행 중인 앱에 실제 공격 시도
  : 도구 — OWASP ZAP, Burp Suite
  : 장점 — 실제 동작 기반, 미탐 적음
  : 단점 — 커버리지 한계, 늦은 발견
```

> "도구가 잡아주는 것에만 의존하면 안 됩니다.
> PreparedStatement를 쓸지, th:text를 쓸지는
> 결국 개발자의 습관과 코드 리뷰에서 결정됩니다."

## 5. 마무리 (1분) — 슬라이드만

| 취약점 | OWASP Top 10 (2021) | 대표 CVE | 한 줄 수정 |
|--------|---------------------|----------|-----------|
| SQL Injection | A03 Injection | CVE-2017-5638 | `pstmt.setString(1, input)` |
| XSS | A03 Injection | CVE-2019-11358 | `th:text` |
| 파일 업로드 | A04 Insecure Design | CVE-2021-22005 | `ALLOWED_EXTENSIONS.contains(ext)` |
| 경로 순회 | A01 Broken Access Control | CVE-2021-41773 | `resolved.startsWith(basePath)` |

> "OWASP Top 10은 매년 업데이트됩니다.
> 오늘 본 취약점들은 2003년부터 목록에 있었고, 아직도 목록에 있습니다."

## 시연 전 체크리스트

: `http://localhost:8080` 정상 접속 확인
: MySQL 서버 실행 중 확인
: `samples/file-upload/webshell.jsp` 존재 확인
: `samples/file-upload/evil-traversal.zip` 존재 확인
: 브라우저 확대 비율 125~150% (청중 가시성)
: 댓글 초기화 버튼으로 XSS 데모 초기 상태 복원
