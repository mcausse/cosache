
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