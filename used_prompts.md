
## ❓ User Question 1
SpringBoot와 JWT인증(SpringSecurity절대 사용안함)을 이용해서 회원가입, 일반 로그인, 구글 로그인, TODO CRUD에 대한 API 서버를 구현하려고 해. DB는 SQLlite3를 사용하려고 해.

## 💡 ChatGPT Answer 1
✅ 프로젝트 구성 개요
Framework: Spring Boot

Database: SQLite3 (JDBC + JPA)

JWT 라이브러리: jjwt (io.jsonwebtoken)

빌드툴: Gradle (Java 17 이상)

SpringSecurity 미사용 → 직접 JWT 인증 필터 구현

✅ 전체 아키텍처 흐름
회원가입 (/users/signup)
→ DB 저장 (비밀번호는 BCrypt로 해싱)

로그인 (/users/login, 일반/구글)
→ 로그인 성공 시 JWT 발급 (access_token 반환)

JWT 인증 필터 직접 구현 (Interceptor or Filter)
→ /users/me, /todos/** 요청 시 Authorization: Bearer <token> 검사

TODO CRUD (/todos)
→ 사용자 인증 후 본인 데이터만 조회/수정 가능

✅ 기술 스택 & 라이브러리
목적	라이브러리
JWT 생성/검증	io.jsonwebtoken:jjwt-api
SQLite3 DB	org.xerial:sqlite-jdbc
JPA ORM	spring-boot-starter-data-jpa
비밀번호 해싱	org.springframework.security.crypto.bcrypt.BCrypt (SpringSecurity는 안 써도 암호화는 가능)

✅ JWT 인증 구현 방식 (SpringSecurity 미사용)
JWTUtil 클래스 (생성 & 검증)

LoginInterceptor (HandlerInterceptorAdapter 상속 → 인증 처리)

WebMvcConfigurer 로 인증이 필요한 URL 패턴에 Interceptor 등록

✅ 디렉토리 구조 예시
css
복사
편집
src/main/java/com/example/demo
 ┣ config/
 ┃ ┗ WebConfig.java (Interceptor 등록)
 ┣ controller/
 ┃ ┣ UserController.java
 ┃ ┗ TodoController.java
 ┣ dto/
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
✅ JWTUtil 클래스 (예시)
java
복사
편집
@Component
public class JwtUtil {
    private final String SECRET_KEY = "secret";

    public String generateToken(Long userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1시간
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public Long validateTokenAndGetUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();
            return Long.parseLong(claims.getSubject());
        } catch (Exception e) {
            throw new RuntimeException("Invalid Token");
        }
    }
}
✅ JWT 인증 Interceptor (예시)
java
복사
편집
@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        String token = authHeader.substring(7);
        Long userId = jwtUtil.validateTokenAndGetUserId(token);

        // 사용자 존재 확인 (optional)
        if (!userRepository.existsById(userId)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        request.setAttribute("userId", userId); // 이후 컨트롤러에서 사용
        return true;
    }
}
✅ WebMvcConfigurer에서 인터셉터 적용
java
복사
편집
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtAuthInterceptor jwtAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/users/me", "/todos/**")
                .excludePathPatterns("/users/signup", "/users/login");
    }
}
✅ SQLite3 설정 (application.properties)
properties
복사
편집
spring.datasource.url=jdbc:sqlite:sample.db
spring.datasource.driver-class-name=org.sqlite.JDBC
spring.jpa.database-platform=org.hibernate.dialect.SQLiteDialect
spring.jpa.hibernate.ddl-auto=update
SQLiteDialect는 직접 구현하거나 simple한 커스텀 Dialect를 쓰셔야 합니다.

✅ 핵심 포인트
포인트	이유
SpringSecurity 대신 Interceptor	Lightweight, Custom JWT 인증
JwtUtil 직접 구현	SecurityContext 없이 토큰 검증
SQLite3는 가볍고 embedded용	개발 과제용으로 적합
userId를 request.setAttribute()로 넘기기	이후 컨트롤러에서 로그인 사용자 참조 가능

## ❓ User Question 2
지금 DB 컬럼명 동일한 건 확인했는데, java.sql.SQLFeatureNotSupportedException: not implemented by SQLite JDBC driver
내가 프로젝트 설정을 잘못했나?

## 💡 ChatGPT Answer 2
이 문제는 SQLite + Hibernate 환경에서 "Generated Keys (PK 자동 증가값)" 를 꺼내올 때 SQLite JDBC가 표준 API를 지원하지 않아서 생기는 것입니다.
이 문제는 "Dialect가 SQLite 전용으로 잘 구현돼야" 해결됩니다.
🔷 1. build.gradle
gradle
복사
편집
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'org.xerial:sqlite-jdbc:3.45.1.0'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'io.github.gwenn:sqlite-dialect:0.1.2'
}
🔷 2. application.properties
properties
복사
편집
spring.datasource.url=jdbc:sqlite:./todo.db
spring.datasource.driver-class-name=org.sqlite.JDBC
spring.jpa.database-platform=org.sqlite.hibernate.dialect.SQLiteDialect
spring.jpa.hibernate.ddl-auto=none
spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:schema.sql
gwenn dialect를 쓰면 Hibernate가 PK 추출 시 SQLite의 last_insert_rowid() 를 자동으로 써줍니다.

이게 안 되면 Hibernate는 getGeneratedKeys()를 쓰려다가 터짐 → gwenn이 해결해줌.

🔷 3. todo.db 삭제 후 재생성
기존 todo.db 파일 삭제

schema.sql이 적용되게 서버 재시작 (ddl-auto=none, schema.sql 기반)

API 테스트 다시 진행

✅ 왜 Gwenn dialect가 답인가?
| 기본 Hibernate | getGeneratedKeys() 로 PK 뽑으려 함 → SQLite 미구현 → 실패 |
| Gwenn dialect | SQLite 전용으로 last_insert_rowid() 로 안전하게 PK를 추출 |
| 실제 운영 사례 | SQLite 쓰는 대부분의 프로젝트가 이걸 씀 |

## ❓ User Question 3
내가 지금 sqlite3와 springboot를 이용해서 jwt인증을 통한 로그인을 구현하려고 해. /users/signup, /users/login을 만들었는데, /users/login에서 email과 password로 로그인 시 jwtUtill을 통해 액세스 토큰을 받아와. 근데 서로 다른 email 로 로그인 API 실행 시 액세스 토큰 값이 같은 것 같아. 달라져야 하지 않나? jwtUtil에서 @PostConstruct를 통해 초기화를 시킨 것 같은데 내가 뭔가 잘못 알고 있는 것 같아. 다시 알려줄래?

## 💡 ChatGPT Answer 3
좋습니다. 상황을 명확하게 짚어드릴게요. 지금 JWT 토큰이 서로 다른 이메일로 로그인해도 똑같이 나오는 이유는 아주 흔한 착각에서 비롯됩니다.

✅ 1. 핵심 원인부터 말하면
❗ JWT 토큰은 로그인할 때마다 새로 만들어지지만,
→ JWT 내부 payload가 같고, secret key가 같으면 토큰 해시 결과도 "똑같이 나옵니다."

왜냐하면:
JWT는 서명된 문자열이지 랜덤값이 아닙니다.

payload + secret key로 HMAC 해시를 만들기 때문에,
→ payload 값(userId)이 같고,
→ 서명 키(secretKey)가 같으면
→ 토큰 값도 항상 같습니다.

✅ 2. 지금 코드에서 발생하는 구조
아마 로그인 시 email → userId를 찾아서 JWT를 만들고 있을 겁니다.

그런데 만약 userId가 같은 값을 쓰고 있거나, 잘못된 값을 쓰고 있다면
→ 토큰이 똑같이 만들어집니다.

즉, JWT는 userId나 payload가 고정되면 같은 토큰이 나오는 게 정상입니다.

✅ 3. JWT가 "서명된 데이터"라는 걸 이해해야 함
개념	설명
Header	서명 알고리즘 등 정보
Payload	userId, email, 권한 같은 데이터
Signature	payload를 secret key로 해싱한 값

→ payload가 똑같고, key가 똑같으면 → 결과 해시(Signature)는 똑같음 → 결국 JWT 문자열도 같음.

✅ 4. email로 로그인했는데 토큰이 같다? → userId 확인해야 함
시나리오:
user1(email: aaa@test.com, id: 1)

user2(email: bbb@test.com, id: 2)

→ 그런데 JWT payload를 만들 때 user.getId() 대신 고정된 값(1 등) 을 넣고 있으면,
→ 이메일은 달라도 항상 같은 userId로 토큰을 만들게 됩니다.

➡ 그래서 토큰도 똑같이 나오는 것입니다.

✅ 5. 체크해야 할 핵심 포인트
체크포인트	확인할 것
login 시 이메일로 user 찾기	userRepository.findByEmail(email) 정상 작동하는가
찾은 user의 userId가 서로 다른가	user.getId() 로 뽑아낸 값이 다른가
JWT 생성 시 setSubject(userId) 를 잘 쓰고 있는가	userId가 고정값이 아닌 실제 로그인한 userId 인가

✅ 6. @PostConstruct는 문제 아님
@PostConstruct는 secretKey 초기화 용입니다.

userId마다 다른 토큰을 만들지 않습니다.

secretKey는 "모든 사용자에 대해 서버가 공통으로 쓰는 서명키"가 맞습니다.

✅ 7. 정리하면
✅ 서로 다른 email로 로그인했는데 토큰이 같다는 건 → userId가 같다는 뜻이고, JWT는 당연히 payload+secret로 서명하는 구조이기 때문에 userId가 다르면 토큰이 달라져야 정상입니다.

→ 그러니까 userId가 다르게 들어가는지 꼭 확인하세요.

✅ 핵심 결론
email로 로그인 → user 찾기 → userId를 JWT payload에 담기

userId가 다르면 → 토큰이 당연히 달라집니다.

secretKey는 공통이고, payload가 다르면 결과가 달라집니다.

## ❓ User Question 4
WebConfig에서 addPathPatterns 가 "/users/me"가 되어있는데, 이 WebConfig에서 API 요청의 headers의 Authorization로부터 userId값을/users/me로 보내는건가?

## 💡 ChatGPT Answer 4
좋습니다. 아주 좋은 질문입니다. 근데 포인트를 정확히 집자면 WebConfig가 "userId를 전달하는 역할"을 하는 건 아닙니다.
정확하게 구분해서 설명드릴게요.

✅ WebConfig의 역할
WebConfig는 Interceptor를 어떤 URL 패턴에 적용할지를 설정하는 역할입니다.

예시:
java
복사
편집
@Override
public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(jwtAuthInterceptor)
            .addPathPatterns("/users/me", "/todos/**");  // 이 URL에 인터셉터 적용
}
여기서 /users/me 요청이 올 때마다 → jwtAuthInterceptor가 작동합니다.

단지 인터셉터를 거칠지 말지를 결정하는 역할일 뿐입니다.

✅ JWTAuthInterceptor가 하는 일
Interceptor가 요청 Headers의 Authorization에서 JWT를 읽고, 검증한 후 userId를 request에 넣어주는 것입니다.

구체적으로:
java
복사
편집
String authHeader = request.getHeader("Authorization");
String token = authHeader.substring(7);
Long userId = jwtUtil.validateTokenAndGetUserId(token);

request.setAttribute("userId", userId);  // ✅ 여기서 userId를 넣어줌
이렇게 하면 Controller에서 request.getAttribute("userId")로 꺼낼 수 있게 됩니다.

→ 이게 바로 JWT를 통해 얻은 userId를 전달하는 핵심 작업입니다.

✅ WebConfig vs Interceptor 차이
WebConfig	Interceptor
URL 패턴 설정 (addPathPatterns)	그 URL 요청을 가로채서 JWT 검증 수행
단순히 "이 요청은 인증 필요하다"는 룰을 설정	Headers에서 Authorization 읽고 userId 추출 후 request에 주입

✅ 한줄 요약
✅ WebConfig는 "어떤 API 요청에 대해 Interceptor를 적용할지" 결정하는 역할이고, 실제로 Authorization 헤더를 읽고 userId를 넣어주는 건 JwtAuthInterceptor가 하는 일입니다.

✅ 쉽게 말하면:
WebConfig: "얘네(/users/me, /todos)는 인증해야 돼 → 인터셉터 써!"

Interceptor: "좋아! 그럼 내가 JWT 토큰 보고 진짜 로그인한 유저인지 검증할게. userId도 넣어줄게."

원하시면 WebConfig → Interceptor → Controller 로 흐르는 전체 과정 (JWT userId 전달 흐름)을 플로우로 정리해드릴 수 있어요.

## ❓ User Question 5
user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()))
이렇게 셋팅된 password를 원복하고 싶으면 어떻게 해야하지

## 💡 ChatGPT Answer 5
좋습니다. 이 부분에서 중요한 개념을 짚어야 합니다.

✅ 핵심부터 말하면:
❗ BCrypt는 "단방향 해시" 이기 때문에 원복(복호화)할 수 없습니다.

이유:
BCrypt는 암호화(encryption)가 아니라 해싱(hash)입니다.

해시는 입력값을 고정된 길이의 값으로 "지문처럼" 바꾸는 것.

원래 값을 복호화할 수 없습니다.

그래서 사용자가 로그인할 때 입력한 비밀번호를 해시한 후 기존 해시와 비교하는 방식을 씁니다.

➡ 해시값 → 원본 비밀번호를 되돌리는 건 불가능합니다.

✅ 비밀번호 원복 대신 하는 방법 (비교 방식)
로그인 시 사용되는 방식이 이겁니다:

java
복사
편집
boolean matched = BCrypt.checkpw(plainPassword, hashedPassword);
plainPassword: 사용자가 입력한 비밀번호 (원본)

hashedPassword: DB에 저장된 해시값

➡ 해시값을 복호화하는 게 아니라,
➡ "입력한 값과 같은지" 를 체크하는 방식입니다.

✅ 다시 정리
복호화	불가능
해시된 비밀번호를 복호화해서 원본 비밀번호로 되돌릴 수 있나?	❌
그럼 어떻게 비교하나?	사용자가 입력한 평문 비밀번호를 다시 해싱해서 DB의 해시값과 비교 (checkpw)

✅ 예시 (로그인 검증할 때)
java
복사
편집
String inputPassword = "user_input_password";  // 사용자가 입력한 비밀번호
String hashedPassword = user.getPassword();    // DB에 저장된 해시값

if (BCrypt.checkpw(inputPassword, hashedPassword)) {
    System.out.println("로그인 성공");
} else {
    System.out.println("비밀번호 불일치");
}
✅ 한줄 핵심
✅ BCrypt로 해싱한 비밀번호는 복호화(원복)할 수 없고, 항상 입력값을 다시 해시해서 비교하는 방식으로 검증합니다

## ❓ User Question 6
Util에다가 로그 클래스를 만들어버리자

## 💡 ChatGPT Answer 6
좋습니다. 매번 @Slf4j 붙이기 귀찮아서 공통 log 유틸리티를 쓰고 싶다는 거군요. 가능합니다. 아래처럼 해보세요.

✅ 1. 공통 LogUtil 클래스 만들기
java
복사
편집
package com.example.todo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtil {

    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    public static void info(Class<?> clazz, String message, Object... args) {
        Logger logger = getLogger(clazz);
        logger.info(message, args);
    }

    public static void warn(Class<?> clazz, String message, Object... args) {
        Logger logger = getLogger(clazz);
        logger.warn(message, args);
    }

    public static void error(Class<?> clazz, String message, Object... args) {
        Logger logger = getLogger(clazz);
        logger.error(message, args);
    }
}
✅ 포인트:
LoggerFactory를 한 번 감싸서 클래스별 Logger를 동적으로 가져옴

호출할 때마다 Class<?> clazz 로 넘기면 됨

info, warn, error 다 지원

✅ 2. 사용하는 방법 (Controller 예시)
java
복사
편집
import com.example.todo.util.LogUtil;

@RestController
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<?> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        LogUtil.info(this.getClass(), "[UserController] /me called with userId={}", userId);

        User user = userService.getUserInfo(userId);

        LogUtil.info(this.getClass(), "[UserController] Successfully retrieved user: {}", user.getUsername());

        return ResponseEntity.ok(user);
    }
}
➡ 이렇게 하면 @Slf4j 안 써도 됨
➡ 그냥 LogUtil.info() 만 쓰면 끝

✅ 3. Service 예시
java
복사
편집
public class UserService {

    public User getUserInfo(Long userId) {
        LogUtil.info(this.getClass(), "[UserService] Fetching user with id={}", userId);

        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    LogUtil.warn(this.getClass(), "[UserService] User not found: id={}", userId);
                    return new UserNotFoundException("User not found with id: " + userId);
                });
    }
}
✅ 장단점
장점	단점
일일이 @Slf4j 안 붙여도 됨	매번 this.getClass() 넘겨야 함
정적 메서드라 간편	IDE 자동완성 @Slf4j 보단 덜 직관적
모든 곳에서 일관된 방식으로 사용 가능	성능이나 기능 면에서 @Slf4j 보다 큰 이점은 없음

➡ 귀찮음을 줄이고 싶다면 괜찮은 방법입니다.

## ❓ User Question 7
Please remove unnecessary stubbings or use 'lenient' strictness. More info: javadoc for UnnecessaryStubbingException class.
	at org.mockito.junit.jupiter.MockitoExtension.afterEach(MockitoExtension.java:197)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1511)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1511)

## 💡 ChatGPT Answer 7
좋습니다. 지금 나온 에러 메시지는 Mockito의 Unnecessary Stubbing(불필요한 스터빙 경고/에러) 입니다.

✅ 요점
Mock으로 when() stubbing 한 게 실제 테스트 중엔 호출되지 않았을 때 발생합니다.

예:

java
복사
편집
when(userRepository.findByUsername("seung")).thenReturn(Optional.of(user));
// 그런데 실제 테스트 코드에서 findByUsername("seung") 를 호출하지 않음
→ 그래서 mockito가 "너 쓰지도 않을 걸 굳이 stubbing 했네?" 라고 경고
✅ 해결법 3가지 (Best Practice)
① 불필요한 when() 지우기
정말 안 쓰는 스터빙이면 → 지워야 합니다.

테스트 로직을 확인하고 필요 없는 stubbing 이면 삭제.

② lenient() 사용 (특정 stubbing만 무시)
불필요하지만 남겨두고 싶을 때 (ex. 공통 stub이라서)

java
복사
편집
lenient().when(userRepository.findByUsername("seung")).thenReturn(Optional.of(user));
➡ 이러면 UnnecessaryStubbingException 을 무시함.

③ 테스트 클래스 전체 lenient 모드로 바꾸기 (추천 X)
class 전체에 적용할 수 있지만 실무에서는 지양 (테스트 정확도 떨어짐)

java
복사
편집
@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    //...
}
✅ 예시 수정 (당신 케이스)
java
복사
편집
@Test
public void login_ValidCredentials_ReturnsToken() {
    when(userRepository.findByUsername("seung")).thenReturn(Optional.of(user));
    when(jwtUtil.generateToken(user.getId())).thenReturn("dummy.jwt.token");

    // 실제로 findByUsername(), generateToken() 호출하는 코드가 있어야 함
    Map<String, String> loginDto = new HashMap<>();
    loginDto.put("username", "seung");
    loginDto.put("password", "test1234");

    String token = userService.login(loginDto);

    assertNotNull(token);
    assertEquals("dummy.jwt.token", token);
}
➡ 만약 이 테스트 안에서 findByUsername() 을 호출 안 하면 위 에러 발생
➡ 호출할 코드가 없다면 → lenient().when(...) 로 감싸주면 됨.

✅ 한줄 요약
✅ 불필요한 when()은 지우고, 정말 일부러 남겨야 하면 lenient()로 감싸세요. 전체 lenient는 추천하지 않습니다.

## ❓ User Question 8
Todo생성-> 목록조회->수정->삭제 테스트 코드를 어떻게 작성하지?

## 💡 ChatGPT Answer 8
좋습니다! 그럼 TodoService에 대해 "생성 → 목록조회 → 수정 → 삭제" 순서대로 테스트하는 단위 테스트 코드만 작성하면 되는군요.

Mockito 기반 단위 테스트

UserRepository, TodoRepository Mocking

Create, Get List, Update, Delete 검증

✅ TodoServiceTest (생성/조회/수정/삭제 테스트 예시)
java
복사
편집
package com.example.todo.unit;

import com.example.todo.dto.TodoDto;
import com.example.todo.entity.Todo;
import com.example.todo.entity.User;
import com.example.todo.exception.TodoNotFoundException;
import com.example.todo.repository.TodoRepository;
import com.example.todo.repository.UserRepository;
import com.example.todo.service.TodoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
public class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TodoService todoService;

    private User user;

    @BeforeEach
    void setup() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();
    }

    // 1. Todo 생성 테스트
    @Test
    void createTodo_success() {
        TodoDto.create createDto = new TodoDto.create("Test Content");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        todoService.createTodo(user.getId(), createDto);

        ArgumentCaptor<Todo> captor = ArgumentCaptor.forClass(Todo.class);
        verify(todoRepository).save(captor.capture());

        Todo savedTodo = captor.getValue();
        assertThat(savedTodo.getContent()).isEqualTo("Test Content");
        assertThat(savedTodo.isCompleted()).isFalse();
        assertThat(savedTodo.getUser()).isEqualTo(user);
    }

    // 2. Todo 목록 조회 테스트
    @Test
    void getTodos_success() {
        Todo todo1 = Todo.builder().id(1L).content("Task 1").completed(false).user(user).build();
        Todo todo2 = Todo.builder().id(2L).content("Task 2").completed(true).user(user).build();

        when(todoRepository.findByUser_Id(user.getId())).thenReturn(List.of(todo1, todo2));

        List<TodoDto.general> result = todoService.getTodos(user.getId());

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getContent()).isEqualTo("Task 1");
        assertThat(result.get(1).getContent()).isEqualTo("Task 2");
    }

    // 3. Todo 수정 테스트
    @Test
    void editTodo_success() {
        Long todoId = 1L;
        Todo todo = Todo.builder().id(todoId).content("Old Content").completed(false).user(user).build();

        TodoDto.general editDto = new TodoDto.general(todoId, "Updated Content", true);

        when(todoRepository.findById(todoId)).thenReturn(Optional.of(todo));

        todoService.editTodo(user.getId(), todoId, editDto);

        assertThat(todo.getContent()).isEqualTo("Updated Content");
        assertThat(todo.isCompleted()).isTrue();
    }

    // 4. Todo 삭제 테스트
    @Test
    void deleteTodo_success() {
        Long todoId = 1L;
        Todo todo = Todo.builder().id(todoId).content("Task to Delete").user(user).build();

        when(todoRepository.findById(todoId)).thenReturn(Optional.of(todo));

        todoService.deleteTodo(user.getId(), todoId);

        verify(todoRepository).delete(todo);
    }

    // 삭제 시 TodoNotFound 예외 테스트 (추가로)
    @Test
    void deleteTodo_notFound() {
        Long todoId = 999L;
        when(todoRepository.findById(todoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> todoService.deleteTodo(user.getId(), todoId))
                .isInstanceOf(TodoNotFoundException.class);
    }
}
✅ 테스트 항목 정리
테스트 항목	검증 내용
createTodo_success	User 조회 후 Todo 생성 → 저장 검증
getTodos_success	User의 Todo 목록 조회 → 반환 값 검증
editTodo_success	Todo 조회 후 content/completed 수정 검증
deleteTodo_success	Todo 조회 후 삭제 검증
(추가) deleteTodo_notFound	Todo가 없을 때 예외 발생 검증

✅ 결론
딱 필요한 4가지(Create, List, Update, Delete) 테스트만 구현

Repository는 Mock 처리 → DB 없이 가능

검증은 ArgumentCaptor, verify, assertThat, assertThrows 로

원하면 Postman 기준 E2E (API까지 치는) 통합 테스트 예시 (SpringBootTest로) 도 작성해드릴 수 있어요.

## ❓ User Question 9
1. 이건뭐야 ArgumentCaptor<Todo> captor = ArgumentCaptor.forClass(Todo.class);
2. 이건 뭐야 assertThat(result).hasSize(2);

## 💡 ChatGPT Answer 9
ChatGPT의 말:
좋습니다. 차근차근 설명드릴게요.

1. ArgumentCaptor<Todo> captor = ArgumentCaptor.forClass(Todo.class);
👉 목적:
**Mockito로 Mock 객체에 전달된 인자값을 실제로 "가져와서 검증"**할 때 씁니다.

🔹 왜 쓰는가?
todoRepository.save(todo) 가 호출된 건 알겠는데,

"거기 들어간 todo가 내가 기대한 값인지" 검증하고 싶을 때 사용합니다.

🔹 예시 흐름
java
복사
편집
// Todo 저장할 때 호출됨
todoService.createTodo(userId, createDto);

// 실제 save() 메서드에 전달된 Todo를 캡처
ArgumentCaptor<Todo> captor = ArgumentCaptor.forClass(Todo.class);
verify(todoRepository).save(captor.capture());

// captor로 전달된 Todo 값 꺼냄
Todo savedTodo = captor.getValue();

// 저장할 때 content가 제대로 들어갔는지 검증
assertThat(savedTodo.getContent()).isEqualTo("Test Content");
🔹 쉽게 말하면:
👉 todoRepository.save(여기 들어간 Todo) 를 "실제로 뭐가 들어갔는지 확인하는 도구"입니다.

2. assertThat(result).hasSize(2);
👉 목적:
result 리스트의 크기(사이즈)가 정확히 2개인지 검증하는 코드입니다.

🔹 왜 쓰는가?
todoService.getTodos(userId) 를 호출했을 때

반환된 결과가 내가 기대하는 것 (예: 2개 리스트인지) 검증하는 Assertion 입니다.

🔹 예시 흐름
java
복사
편집
List<TodoDto.general> result = todoService.getTodos(userId);

// result의 크기가 2개인지 검증
assertThat(result).hasSize(2);
🔹 쉽게 말하면:
👉 결과 리스트가 진짜 2개인지 체크하는 검증입니다.

만약 3개나 1개가 반환되면 이 테스트는 실패합니다.

✅ 정리하면
코드	의미	쉬운 비유
ArgumentCaptor.forClass(Todo.class)	Mock 메서드에 전달된 실제 인자값을 캡처(가져옴)	"save()에 진짜 들어간 값이 뭐였는지 확인"
assertThat(result).hasSize(2);	반환된 리스트가 정확히 2개인지 검증	"조회 결과가 2개인지 체크"


## ❓ User Question 10
orphanRemoval = true	연관관계 끊긴 Todo도 삭제
이게 있으면 어느 유저를 지우든 todo가 같이 지워지는거 아녀?

## 💡 ChatGPT Answer 10
좋습니다. 아주 날카로운 질문입니다. 결론부터 말씀드리면 "아니요, 아무 유저를 지운다고 해서 모든 Todo가 삭제되는 건 아닙니다."

✅ orphanRemoval의 정확한 의미
orphanRemoval = true	의미
✅ "부모(User)에서 자식(Todo)을 리스트에서 삭제(제거) 하면 DB에서도 삭제하라"	
즉, user.getTodos().remove(todo) → 이럴 때만 DB 삭제	

➡ User 엔티티 안에서 직접 리스트에서 빼는 것과 관련된 설정입니다.

✅ cascade = REMOVE 와 orphanRemoval 차이
항목	cascade = REMOVE	orphanRemoval = true
User 삭제 시 → 연관 Todo 삭제	✅ 맞습니다	
User.todos 리스트에서 remove() 시 삭제	❌ (cascade만 있으면 안 됨)	✅ (리스트에서 제거하면 실제 DB 삭제)

✅ 핵심 정리
상황	Todo 삭제됨?
userRepository.delete(user);	✅ (cascade = REMOVE 덕분에 Todo도 삭제됨)
user.getTodos().remove(todo);	✅ (orphanRemoval = true 덕분에 삭제됨)
user와 관계 없는 Todo는 영향 없음	✅

➡ 즉, user가 삭제되면 그 user의 Todo만 삭제되고, 다른 유저의 Todo는 그대로 남습니다.

✅ 예시로 표현:
java
복사
편집
User user = userRepository.findById(1L).orElseThrow();
userRepository.delete(user);
➡ 이 때 user.getTodos()로 연관된 Todo들만 삭제됩니다.

다른 user의 Todo는 영향 없습니다.

✅ 한줄 요약
✅ orphanRemoval은 "User.todos 리스트에서 직접 remove할 때 DB도 삭제"라는 의미이고, cascade = REMOVE는 "User 삭제 시 연관 Todo 삭제"입니다. 다른 유저의 Todo는 전혀 영향 없습니다.