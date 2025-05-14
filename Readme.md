# ✅ TODO 관리 API (SQLite3 + JWT 인증 기반)

## 1. 프로젝트 개요
SQLite3와 JWT 인증 방식을 활용한 TODO 관리 RESTful API 프로젝트입니다.  
사용자는 회원가입/로그인을 통해 발급받은 JWT 토큰을 이용해 본인의 TODO를 생성, 조회, 수정, 삭제할 수 있습니다.

## 2. 기술 스택
- Spring Boot 3.x
- Java 17
- SQLite3
- JWT (jjwt)
- Spring Data JPA
- Mockito (단위 테스트)
- Spring MockMvc (통합 테스트)

## 3. 프로젝트 구조
src/main/java/com/example/demo
 ┣ config/
 ┃ ┗ WebConfig.java (Interceptor 등록)
 ┣ controller/
 ┃ ┣ UserController.java
 ┃ ┗ TodoController.java
 ┣ dto
 ┃ ┣ UserDto.java, LoginDto.java, TodoDto.java
 ┣ entity/
 ┃ ┣ User.java
 ┃ ┗ Todo.java
 ┣ interceptor/
 ┃ ┗ JwtAuthInterceptor.java
 ┣ repository/
 ┃ ┣ UserRepository.java
 ┃ ┗ TodoRepository.java
 ┣ service/
 ┃ ┣ UserService.java
 ┃ ┗ TodoService.java
 ┣ util/
 ┃ ┗ JwtUtil.java
 ┗ DemoApplication.java
 
## 4. 실행 방법
### 1️⃣ API 서버 프로젝트 실행
- todo 프로젝트 내로 이동
  ```bash
  java -jar build\libs\todo-0.0.1-SNAPSHOT.jar
  ```
### 2️⃣ Postman을 통해 API 테스트 가능
**예시 (POST 요청):**

- URL:
  ```
  http://localhost:8080/users/signup
  ```

- Headers:
  | Key           | Value              |
  |---------------|-------------------:|
  | Content-Type  | application/json   |

- Body (JSON):
  ```json
  {
    "email": "abcd2@naver.com",
    "password": "1234",
    "social": "local",
    "username": "test2"
  }
  ```

## 5. API 명세 요약

### 🔐 USER 인증 (JWT 인증 필요)
| Method | Endpoint | Description | Request | Response |
|---------|----------|-------------|---------|----------|
| POST | /users/signup | 회원가입 | { "username": "", "email": "", "password": "", "social": "" } | 200 OK, "Sign Success" |
| POST | /users/login | 로그인 및 JWT 토큰 발급 | { "email": "", "password": "" } | { "access_token": "..." } |
| GET | /users/me | 내 정보 조회 | Header: Authorization: Bearer {token} | { "username": "", "password": "", "email": "", "social": "", "todos" : [] } |
| PUT | /users/me | 내 정보 수정 | Header: Authorization: Bearer {token} <br> Body : { "username": "", "email": "", "password":"" } | 200 OK, "edit userInfo Success" |
| DELETE | /users/me | 내 정보 삭제 | Header: Authorization: Bearer {token} | 200 OK, "delete user Success" | 

### 📝 TODO API (JWT 인증 필요)
| Method | Endpoint | Description | Request | Response |
|---------|----------|-------------|---------|----------|
| POST | /todos | TODO 생성 | { "content": "할 일 내용" } | 200 OK |
| GET | /todos | 내 TODO 목록 조회 | - | [ { "id": 1, "content": "내용", "completed": false } ] |
| GET | /todos/{id} | 특정 TODO 조회 | PathVariable: id | { "id": 1, "content": "내용", "completed": false } |
| PUT | /todos/{id} | TODO 수정 | { "content": "수정내용", "completed": true } | 200 OK |
| DELETE | /todos/{id} | TODO 삭제 | - | 200 OK |
| GET | /todos/search | 검색 (optional: keyword, completed) | QueryParam: keyword, completed | [ ... ] |

## 6. 예외 처리 정책
| 상황 | 응답 코드 | 예외 메시지 |
|-------|-----------|-------------|
| 존재하지 않는 유저 조회 | 404 Not Found | UserNotFoundException Error |
| 존재하지 않는 Todo 접근 | 404 Not Found | TodoNotFoundException Error |
| 다른 사람의 Todo 접근 | 401 Unauthorized | AccessDeniedException Error |
| 잘못된 로그인 정보 | 400 Bad Request | Invalid Credentials |
| 필수 파라미터 누락 | 400 Bad Request | Validation Errors |

## 7. 테스트
- 단위 테스트 (Mockito): [UserServiceTest.java] UserService, [TodoService.java] TodoService
- 통합 테스트 (MockMvc): [IntegrationTest.java] 회원가입 → 로그인 → Todo CRUD 전체 플로우 검증

## 8. SQLite 초기화
- schema.sql: 테이블 정의 (user, todo)
- data.sql: 샘플 데이터 삽입
- application.yml에서 spring.sql.init.mode=always 설정

## 9. 주의사항
- 개발/테스트 환경용 프로젝트
- 운영 배포 시 RDBMS & 마이그레이션 도구 적용 필요

## 10. 테스트 코드 실행 방법

### 1️⃣ IntelliJ IDE에서 실행
- 테스트 파일을 열고 ▶️ 버튼 클릭 후 Run Test
- 하단 Test 결과 창에서 결과 확인

### 2️⃣ Gradle (콘솔/터미널 기준)
- 전체 테스트 실행
  ```bash
  ./gradlew test  # Windows는 gradlew.bat test
  ```

- 특정 클래스만 실행 (예: TodoServiceTest)
  ```bash
  ./gradlew test --tests "com.example.todo.service.TodoServiceTest"
  ```

- 특정 메서드만 실행 (예: createTodo_success)
  ```bash
  ./gradlew test --tests "com.example.todo.service.TodoServiceTest.createTodo_success"
  ```

### 3️⃣ 테스트 결과 확인
- 콘솔 로그에서 성공/실패 확인 가능
- Gradle 기준: /build/reports/tests/test/index.html 에서 HTML 테스트 리포트 확인 가능

