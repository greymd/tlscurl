# curl for checking various TLS parameters.

# Build

```
mvn clean compile assembly:single
```

# Execute

```
java -jar target/tlscurl-1.0-SNAPSHOT-jar-with-dependencies.jar \
--protocols "TLSv1,TLSv1.1,TLSv1.2" \
--ciphers "TLS_DHE_DSS_WITH_3DES_EDE_CBC_SHA,TLS_RSA_WITH_AES_128_CBC_SHA" \
"https://example.com"
```
