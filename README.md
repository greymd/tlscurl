# cURL for checking various TLS parameters on JVM.

# Installation

## Linux/macOS

```
$ curl -L https://github.com/greymd/tlscurl/releases/download/v1.0.0/tlscurl_unix.tar.gz | tar zxv
$ sudo install -m 0755 tlscurl /usr/bin/tlscurl
```

## Windows

TBD

# Usage

```

$ ./tlscurl --help
Usage:
 tlscurl [options]
 tlscurl [options] [target_url]

Options:
 --help               : Show help and exit. (default: true)
 -V                   : Show version and exit. (default: false)
 -c (--ciphers) VAL   : Provide cipher suites for connection (i.e
                        TLS_RSA_WITH_RC4_128_SHA).
 -k                   : Allow self-certified SSL. (default: false)
 -p (--protocols) VAL : Provide protocols for TSL connection (i.e TLSv1,
                        TLSv1.1 or TLSv1.2). (default: TLSv1,TLSv1.1,TLSv1.2)
 -x VAL               : Provide proxy address. (default: )
```

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
