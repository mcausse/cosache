
# Custache templates

## With Spring

```xml
    <dependency>
    <groupId>io.homs</groupId>
    <artifactId>custache-spring</artifactId>
    <version>0.0.2</version>
</dependency>
```

```java
@Import(CustacheConfiguration.class)
@SpringBootApplication
public class Application {
```

```java
@RestController
@RequestMapping(value = "/")
public class CustacheController {

    @Autowired
    CachedModelAndView cachedModelAndView;
    
    [...]

    @GetMapping
    public String index() {

        [...]

        return cachedModelAndView.getOrParse("gitinillo-template.html")
                .with("repositories", repositoryInfos)
                .with("servlet-context", "/" + REPOSITORIES_BASE_URL)
                .evaluate();
    }
```

### Custache config

In `custache.properties`:

```properties
custache.prefix=templates/
custache.suffix=
```

, or in `application.yml`:

```yml
custache:
  prefix: templates/
  suffix:
```