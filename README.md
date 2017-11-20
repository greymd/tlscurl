# curl for checking various TLS parameters.

# Build

```
mvn clean compile assembly:single
```

# Execute

```
java -jar target/tlscurl-1.0-SNAPSHOT-jar-with-dependencies.jar "https://example.com" "TLSv1,TLSv1.1,TLSv1.2" "TLS_RSA_WITH_AES_128_CBC_SHA"
```
