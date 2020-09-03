#### SSIA Live Project Milestone 1.2
Build an Oauth Server - replace in-memory backing for userDetails and ClientDetails  services with a DB backend
# 
 
##### Suggested  recommended reading list 
 
1. SSIA Chapter 3  User Details Service
 

Also useful (JPA)
 - JPA

It's also probably ok to push Chapter 3 as recommended reading until Milestone 1.2 



#### OAuth Server project Setup part 2 JPA

#####  Add H2 and JPA dependencies to pom.xml
```
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-data-jpa</artifactId>
            </dependency>


            <dependency>
                <groupId>com.h2database</groupId>
                <artifactId>h2</artifactId>
                <scope>runtime</scope>
            </dependency>
```

##### Define sc user service 
Under src/main/resources add a schema.sql file
and define the SQL Tables 
to store user information and the zero or more authorities they have.
```
drop table if exists AUTHORITY;
drop table if exists USER; 

create table if not exists USER
(
    ID INT auto_increment primary key,
    USERNAME VARCHAR(50) not null,
    PASSWORD VARCHAR(255) not null
);

create table  if not exists AUTHORITY
(
    ID INT auto_increment primary key,
    USER_ID INT not null,
    AUTHORITY VARCHAR(50) not null,
    constraint AUTHORITY_USER_USERNAME_FK
        foreign key (USER_ID) references USER (ID)
);
```

##### set up H2 datasource in application.properties
```
# setup H2 datasource
spring.datasource.url=jdbc:h2:mem:oauth
spring.datasource.username=admin
spring.datasource.password=admin
spring.datasource.driverClassName=org.h2.Driver
# "always" is not something we would normally use for production .
spring.datasource.initialization-mode=always


# for development only, can verify schema is generated and populated
# http://localhost:8080/h2-console
spring.h2.console.enabled=true
```
##### verify the schema is created at application startup
- run the app 
- connect to http://localhost:8080/h2-console
- We required that all endpoints be authenticated,  so you'll need to enter a users credentials in the login form that appears
 e.g user=john and password=12345
- in the  H2 admin console populate the password field with  "admin" and press connect 
- some browsers may complain about opening the H2 console that has Iframes 
- you can open up the left  nav frame in a new window and you should see the USER and AUTHORITY tables defined

##### add some data into the USER and AUTHORITY tables
under src/main/resources add a data.sql file 
Add some entries to your USER and AUTHORITY tables to help populate the H2 in-memory DB with some initial data at application startup time
```
insert into USER (ID, USERNAME, PASSWORD) values (1, 'john' ,'12345);
insert into USER (ID, USERNAME, PASSWORD) values (2, 'admin' ,'swordfish');
INSERT INTO AUTHORITY ( USER_ID ,AUTHORITY ) values (1, 'ROLE_USER');
INSERT INTO AUTHORITY ( USER_ID ,AUTHORITY ) values (2, 'ROLE_ADMIN');
```
##### verify USER table is populated with some data 
start the app and connect to H2 console as before. 
in the top nav window enter the following sql and clinck the "RUN" button
```
select * from USER;
```
you should see your two entries for john and admin

##### Create a JPA entity for your USER table
- We can use the Lombok @Data annotation to avoid adding setters and getters
- don't worry about the authorities child collection just yet, we'll come back to that later 
- the ```@Id   @GeneratedValue(strategy=GenerationType.IDENTITY)```  annotation indicates primary key IDs should be auto generated
```
@Data
@Entity
public class User {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private int id;
    private String username;
    private String password;

    
    public User() {
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
               
                '}';
    }
}
```

##### create a User JPA Repository  interface
```
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository   extends JpaRepository<User, Integer> {
  User findByUsername(String name);
 }
```


##### create a JPA test class to test your User repository 

```
@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=validate"
})
public class UserRepositoryTest {

    @Autowired
    private DataSource dataSource;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private UserRepository userRepository;

    @Test
    void injectedComponentsAreNotNull() {
        assertThat(dataSource).isNotNull();
        assertThat(jdbcTemplate).isNotNull();
        assertThat(entityManager).isNotNull();
        assertThat(userRepository).isNotNull();
    }

    @Test
    void findByUsername() {
        User user = userRepository.findByUsername("john");
        assertThat(user.getUsername()).isEqualTo("john");
    }
}
```
both tests should succeed 
##### Create an Authority JPA Entity for the AUTHORITY Table 
- The Authority entity has many-to-one relationship with the User entity 
- and the authority's USER_ID column is the foreign key we use to  point back to the parent table
- it's a good idea to override the lombok toString() to prevent circular references when printing
```
@Data
@Entity
public class Authority {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;
    private String authority;

    
    @ManyToOne
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user ;

    public Authority() {
    }

    public Authority(String authority, User user) {
        this.authority=authority;
        this.user=user;
    }

    @Override
    public String toString() {
        return "Authority{" +
                "id=" + id +
                ", authority='" + authority + '\'' +
                '}';
    }
}
```

##### update the User Entity to include a collection of the authories it owns 
- The mappedBy value of "user" defines the field in the Authority class which is used to map back to the User Class
- you may also update the toString() to include the authorities field
```
  @Entity
  public class User {  

   ...   
  
      @OneToMany(mappedBy = "user", cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
      private List<com.manning.ssia.ssiamilestone.jpa.Authority> authorities;

```
##### Update your UserRepositoryTesT   class to verify that the user "john" has an Authority populated
```
 @Test
    void johnUserhasAtLeastOneAuthority() {
        User user = userRepository.findByUsername("john");
        assertThat(user.getAuthorities()).isNotEmpty();
    }
```
##### create a UserDetailsService (replaces in-memory equivalent)
- re-pruprosing the JPA User entity to implement the UserDetails interface is usually more trouble than it's ever worth
- instead create your own custom class inplementing the UserDetails interface which can work with our JPA User entity
```
public class CustomUserDetails  implements UserDetails {
        private User user;  //our JPA entity

        public CustomUserDetails(User user) {
            this.user = user;
        }
        
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getAuthorities().stream()
                .map(Authority::getAuthority)
                .map(role -> new SimpleGrantedAuthority(role))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String toString() {
        return "CustomUserDetails{" +
                "username=" + user.getUsername() +
                "authorities=" + this.getAuthorities()  +
                '}';
    }
}
```
##### create a UserDetailsService (replaces in-memory equivalent)
create your own JPA backed implementation of the UserDetailsService
```
@Slf4j
@Service
public class JpaUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        log.info("looking up user {}", username);
        User user = userRepository.findByUsername(username);
        if (user == null) {  //TODO use Optional ?
            log.error("did not find user {}", username);
            throw new UsernameNotFoundException(username);
        }
        com.manning.ssia.ssiamilestone.security.CustomUserDetails userDetails = new com.manning.ssia.ssiamilestone.security.CustomUserDetails(user);
        log.info("found userdetails {}", userDetails);
        return userDetails;
    }
}
```

##### Add some JPA properties to application.properties
- JPA can also create the DB schema at start up. 
- you'll want to disable that as it will conflict with your schema.sql
```
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=none
```
##### create a test for your new JpaUserDetailsService
 
```
@SpringBootTest
class JpaUserDetailsServiceTest {

    @Resource(name = "jpaUserDetailsService")
    private JpaUserDetailsService userDetailsService;

    @Test
    void loadUserByUsernameJohnIsFound() {
        UserDetails userDetails = userDetailsService.loadUserByUsername("john");
        assertThat(userDetails.getUsername()).isEqualTo("john");
        assertThat(userDetails.isEnabled()).isTrue();
    }
}
```

##### replace the in-memory user details service with your new JPA backed userDetailsService
- find the java configuration class where you defined a userDetailsService bean and delete it.
- Your new JpaUserDetailsService will automatically be detected and used
```
// DELETE the following config bean
@Override
    @Bean
    public UserDetailsService userDetailsService() {
        InMemoryUserDetailsManager userDetailsService = new InMemoryUserDetailsManager();
      
```

##### re-run all your unit tests
- UserController
  UserDtoUserController
         UserDto
- you should also be able to connect to the http://localhost:8080/alive endpoint using the same john:12345 credentials you used with the in-memory service
 

##### Troubleshooting
if you've haing problems authenticating usin yoour new JPA userdetailsService then I recommend temporarily enabling
debug in the @EnableWebSecurity annotation e.g 
```
@Configuration
@EnableWebSecurity(debug = true)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

```
you can also enable debug logging in your application.properties
```
logging.level.org.springframework.security=debug
```
#### Creatings a /users REST endpoint

##### Create a UserDto object that the controller will return 
I find it's best not to return JPA entity objects ina REST Controller but instead map them to a simple DTO
```
@Data
@NoArgsConstructor
public class UserDto {
    private int id;
    private String username;
    private String password; // this can be removed after debugging
    private List<String> authorities;

    UserDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.authorities = user.getAuthorities()
                .stream()
                .map(Authority::getAuthority)
                .collect(Collectors.toList());
    }
}
```

##### create a REST Contoller for the /users endpoint
 UserController
```

@Slf4j
@RestController
@RequestMapping(path = "/users", produces = "application/json")
@CrossOrigin(origins = "*")
public class UserController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
          this.passwordEncoder =  passwordEncoder;
    }

    @GetMapping
    public Iterable<UserDto> listUsers() {

        log.debug("user repo is size " + userRepository.count());
        //userRepo.findAll().forEach(t->log.debug("found user "+t));

        PageRequest page = PageRequest.of(
                0, 12, Sort.by("username").ascending());

        return userRepository
                .findAll(page)
                .getContent()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    private UserDto convertToDto(User user) {
        return new UserDto(user);
    }
}
``` 
after restarting the app you should be able to connect to http://localhost:8080/users/ and see all the users
```
{
"access_token":"47da9aac-f6f3-4ad1-a7b3-35fb7065e1e4",
"token_type":"bearer",
"refresh_token":"19004b27-1f3f-4d31-a897-7f500da06186",
"expires_in":42910,
"scope":"read"
}
```

##### Create a postman test to verify password grant request
I recommend writing  the Postman test   to actually verify the 200 http status return code

see https://learning.postman.com/docs/writing-scripts/test-scripts/
```
pm.test("Expecting Status code of 200, was "+ pm.response.code, function () {
    pm.response.to.have.status(200);
});
```
##### use postman to extract tokens for subsequent postman request 

it's possible to use postman to extract the values of the access_token and refresh_token to make it easier to use on subsequent requests e.g a refesh_token request

see https://learning.postman.com/docs/sending-requests/variables/ 
```

pm.test("save tokens", function () {
    var jsonData = pm.response.json();
    console.log("Saving access_token value to 'access_token' environmental variable: " + jsonData.access_token)
    pm.environment.set("access_token", jsonData.access_token);
    pm.environment.set("refresh_token", jsonData.refresh_token);
});
```

##### enable symetric key JWT - see section 15.1.2
see https://livebook.manning.com/book/spring-security-in-action/chapter-15/v-7/22
```
public class AuthServerConfig
  extends AuthorizationServerConfigurerAdapter {

@Value("${jwt.key}")
  private String jwtKey;
 

@Override
  public void configure(
    AuthorizationServerEndpointsConfigurer endpoints) {
      endpoints
        .authenticationManager(authenticationManager)
        .tokenStore(tokenStore())
        .accessTokenConverter(
           jwtAccessTokenConverter());
  }
 
  @Bean
  public TokenStore tokenStore() {
    return new JwtTokenStore(
      jwtAccessTokenConverter());
  }
 
  @Bean
  public JwtAccessTokenConverter jwtAccessTokenConverter() {
    JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
    converter.setSigningKey(jwtKey);
    return converter;
 }
```

##### add a symetric key value 
https://livebook.manning.com/book/spring-security-in-action/chapter-15/v-7/38
```
#application.properties
jwt.key=MjWP5L7CiD
```

##### try the password grant again to see if you get a JWT token
```
curl --location -u client:secret \
--request POST 'localhost:8080/oauth/token?grant_type=password&username=john&password=12345&scope=read' \
--header 'Content-Type: application/json'  
``` 
you should se JWT style tokens returned with the three dots in the access and refresh tokens
```
{"access_token":"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE1OTg4MTEwMjgsInVzZXJfbmFtZSI6ImpvaG4iLCJhdXRob3JpdGllcyI6WyJST0xFX1VTRVIiXSwianRpIjoiYzdlZTcwMTYtYzE4OS00MTNlLTg5NjYtMzU0ZTM2Y2Y5NjZiIiwiY2xpZW50X2lkIjoiY2xpZW50Iiwic2NvcGUiOlsicmVhZCJdfQ.srE7t4IbawhlRrjOvkPnE-ZOws2a6Mj-VYTFT_vVUK4","token_type":"bearer","refresh_token":"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX25hbWUiOiJqb2huIiwic2NvcGUiOlsicmVhZCJdLCJhdGkiOiJjN2VlNzAxNi1jMTg5LTQxM2UtODk2Ni0zNTRlMzZjZjk2NmIiLCJleHAiOjE2MDEzNTk4MjgsImF1dGhvcml0aWVzIjpbIlJPTEVfVVNFUiJdLCJqdGkiOiI3MWRkZDRiNi0zMzI5LTRkMmUtYTEwMi0yZDhlMmY1MjYyNWQiLCJjbGllbnRfaWQiOiJjbGllbnQifQ.eYQreV9u51Xqd9o6XoTQqPY5TfvGC3pBGhB8ggXGqws","expires_in":43199,"scope":"read","jti":"c7ee7016-c189-413e-8966-354e36cf966b"}
```
#### create  Asymmetric keys using keytool section 15.2.1
see https://livebook.manning.com/book/spring-security-in-action/chapter-15/v-7/89

##### replace Jwt Converter with  asymmetric implementation section 15.2.2
see https://livebook.manning.com/book/spring-security-in-action/chapter-15/v-7/103
```
  @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();

        KeyStoreKeyFactory keyStoreKeyFactory =
                new KeyStoreKeyFactory(
                        new ClassPathResource(privateKey),
                        password.toCharArray()
                );

        converter.setKeyPair(
                keyStoreKeyFactory.getKeyPair(alias));

        return converter;
    }
```


##### try the password grant again to see if you get a aymetric key JWT token
you should see the access_token is much larger than when using the symetric key
```
curl --location -u client:secret \
--request POST 'localhost:8080/oauth/token?grant_type=password&username=john&password=12345&scope=read' \
--header 'Content-Type: application/json'  
``` 

##### TODO craft an oauth refresh_token grant curl/postman request 
hmm, getting a 500 internal server error 
```

```
##### TODO craft a  /oauth/check_token curl/postman  request 