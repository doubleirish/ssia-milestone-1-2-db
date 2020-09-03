#### SSIA Live Project Milestone 1.2
Build an Oauth Server - replace in-memory backing for userDetails and ClientDetails  services with a DB backend
# 
 
##### Suggested  recommended reading list 
 
1. SSIA Chapter 3  User Details Service
 

Also useful (JPA)
 - JPA

It's also probably ok to push Chapter 3 as recommended reading until Milestone 1.2 



#### OAuth Server project Setup part 2 JPA

#####  Add JPA dependency


##### Create the configuration class to enable an oauth server
see https://livebook.manning.com/book/spring-security-in-action/chapter-13/v-7/
```
@Configuration
@EnableAuthorizationServer
public class OAuthConfig extends AuthorizationServerConfigurerAdapter {
}
```

##### run app and verify new /oauth/* endpoints are available
it easiest to just run inside your IDE. but you can use ```mvn spring-boot:run```

##### create a simple "alive" controller for testing purposes
see https://livebook.manning.com/book/spring-in-action-fifth-edition/chapter-6/20
```
@RestController
@RequestMapping(path = "/alive") 
public class AliveController {
    @GetMapping
    public String alive() {   return "healthy!";   }
}
```
##### verify access to alive-controller returns "alive!"
```
curl http://localhost:8080/alive
-> returns status 200 and "alive" text
```

##### Add a Config class to require authenticated users 
https://livebook.manning.com/book/spring-security-in-action/chapter-2/v-7/17
https://livebook.manning.com/book/spring-security-in-action/chapter-2/v-7/160
```
@Configuration
@EnableWebSecurity(debug = true)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http
                .authorizeRequests()
                .anyRequest()
                .authenticated()
                .and()
                .httpBasic()
        ;
    }
```

##### verify access to alive-controller without creds fails with 401
```
curl http://localhost:8080/alive
-> returns status 401 unauthorized
```


##### verify access to alive-controller with good user creds is sucessful
see https://livebook.manning.com/book/spring-security-in-action/chapter-2/v-7/35
```
# search the logs for the password with the log line begining with "Using generated security password:" 
curl -u user:380ec167-9b45-4644-952d-cb93331bb3e5 http://localhost:8080/alive
-> returns status 200 and "alive" text
```
#### setup userDetailsService
see https://livebook.manning.com/book/spring-security-in-action/chapter-2/v-7/110
##### replace the default security with an in-memory userdetails service 
```
@Configuration
public class ProjectConfig {
    @Bean
    public UserDetailsService userDetailsService() {
        InMemoryUserDetailsManager userDetailsService = new InMemoryUserDetailsManager();
        UserDetails john = User.withUsername("john")
                .password("12345")
                .authorities("ROLE_USER")
                .build();
        userDetailsService.createUser(john);
        return userDetailsService;
    }
```

##### override password encoder with no-op password encoder
see https://livebook.manning.com/book/spring-security-in-action/chapter-4/v-7/33
```
  @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
```
##### test access to /alive with bad credentials
```
curl -I -u john:badpass http://localhost:8080/alive
-- returns 401
```
##### test access to /alive with good credentials
```
curl -I -u john:12345 http://localhost:8080/alive
-- returns 200
```

##### add a test of authentiation using the userDetailsService 
for TestRestTemplate see https://livebook.manning.com/book/cloud-native-spring-in-action/chapter-3/v-1/185

for MockMvc see https://livebook.manning.com/book/spring-security-in-action/chapter-20/v-7/47

```
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Slf4j
public class AliveControllerTest {
    @LocalServerPort
    private int port;
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void alive_authorized() throws Exception {
        String jsonStr = this.restTemplate
                .withBasicAuth("john", "12345")
                .getForObject("http://localhost:" + port + "/alive", String.class);
        assertThat(jsonStr).contains("healthy!");
    }
```

##### add in-memory client details service
see  https://livebook.manning.com/book/spring-security-in-action/chapter-13/v-7/44
```
public class OAuthConfig extends AuthorizationServerConfigurerAdapter {
    @Override
    public void configure(  ClientDetailsServiceConfigurer clients)
            throws Exception {
        clients.inMemory()
                .withClient("client")
                .secret("secret")
                .authorizedGrantTypes("password","authorization_code","client_credentials","refresh_token")
                .scopes("read");
    }
```

#### setup clientDetails service
 

##### register the authentication manager in the authorization server

see https://livebook.manning.com/book/spring-security-in-action/chapter-13/v-7/30
```
public class AuthServerConfig
  extends AuthorizationServerConfigurerAdapter {
 
 
  @Autowired
  private AuthenticationManager authenticationManager;
 
  @Override
  public void configure(
    AuthorizationServerEndpointsConfigurer endpoints) {
      endpoints.authenticationManager(authenticationManager);
  }
```

##### generate a token using  the password grant
Another option is to create these "curl" statements using postman.
After you construct a working postman request just click on the "code" link thats  to the right of the "Params", "Auth" and "Headers" tab

see https://learning.postman.com/docs/getting-started/introduction/
```
curl --location -u client:secret \
--request POST 'localhost:8080/oauth/token?grant_type=password&username=john&password=12345&scope=read' \
--header 'Content-Type: application/json'  
``` 
you should see a response body similar to
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