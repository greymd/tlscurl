package fun.gre.tlscurl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.glassfish.jersey.SslConfigurator;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.JerseyClientBuilder;

class Main {

    private static final String[] SUPPORTED_PROTOCOLS = {
        "TLSv1",
        "TLSv1.1",
        "TLSv1.2"
    };

    private static final String[] SUPPORTED_CIPHER_SUITES = {
        "TLS_DHE_DSS_WITH_3DES_EDE_CBC_SHA",
        "TLS_DHE_DSS_WITH_AES_128_CBC_SHA",
        "TLS_ECDHE_ECDSA_WITH_RC4_128_SHA",
        "TLS_ECDHE_RSA_WITH_RC4_128_SHA",
        "TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA",
        "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA",
        "TLS_ECDH_ECDSA_WITH_RC4_128_SHA",
        "TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA",
        "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA",
        "TLS_ECDH_RSA_WITH_RC4_128_SHA",
        "TLS_RSA_WITH_RC4_128_MD5",
        "TLS_RSA_WITH_RC4_128_SHA",
        "TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA",
        "TLS_DHE_RSA_WITH_AES_128_CBC_SHA",
        "TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA",
        "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA",
        "TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA",
        "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
        "TLS_RSA_WITH_3DES_EDE_CBC_SHA",
        "TLS_RSA_WITH_AES_128_CBC_SHA",
        "TLS_EMPTY_RENEGOTIATION_INFO_SCSV"
    };

    private Client client = ClientBuilder.newClient();

    public static void main(String args[]) {
        Main mainClass = new Main();
        mainClass.run(
                args[0],
                args[1].split(","),
                args[2].split(",")
                );
    }

    public void run(String url, String[] protocols, String[] ciphers) {
        try {
            Map<String, String> headers = new HashMap<String, String>();
            headers.put("User-Agent", "cipher_curl");
            WebTarget target;
            target = makeTarget(url, protocols, ciphers, null);
            Response res = callPostAPI(target, "{\"payload\":1}", headers);
            System.out.println(res.toString());
            System.out.println(res.readEntity(String.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected WebTarget makeTarget(String url, String[] protocols, String[] ciphers, SSLContext sslContext) {
        ApacheConnectorProvider provider = new ApacheConnectorProvider();
        ClientConfig config = new ClientConfig().connectorProvider(provider);

        LayeredConnectionSocketFactory sslSocketFactory;
        if (sslContext == null) {
            sslContext = SslConfigurator.getDefaultContext();
        }
        sslSocketFactory = new SSLConnectionSocketFactory(
                sslContext,
                protocols,
                ciphers, // SUPPORTED_CIPHER_SUITES,
                SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);

        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory> create()
            .register("http", PlainConnectionSocketFactory.getSocketFactory())
            .register("https", sslSocketFactory)
            .build();

        BasicHttpClientConnectionManager connectionManager = new BasicHttpClientConnectionManager(registry);
        config.property(ApacheClientProperties.CONNECTION_MANAGER, connectionManager);

        client = JerseyClientBuilder.newClient(config);
        return client.target(url);
    }

    protected Response callPostAPI(WebTarget target, String form, Map<String, String> headerMap) {

        Invocation.Builder invocationBuilder =  target.request();
        Entity<String> entity = (form != null) ? Entity.entity(form, MediaType.APPLICATION_JSON_TYPE) : null;

        for (Entry<String, String> entry: headerMap.entrySet()) {
            invocationBuilder.header(entry.getKey(), entry.getValue());
        }

        Response response =  invocationBuilder.post(entity);
        return response;
    }
}
