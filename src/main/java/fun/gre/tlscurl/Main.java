package fun.gre.tlscurl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

import org.apache.commons.lang3.StringUtils;
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
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

class Main {

    @Option(name = "-p", aliases = "--protocols", required = false, usage = "Provide protocols for TSL connection (i.e TLSv1, TLSv1.1 or TLSv1.2).")
    private String protocols = "TLSv1,TLSv1.1,TLSv1.2";

    @Option(name = "-c", aliases = "--ciphers", required = false, usage = "Provide cipher suites for connection (i.e TLS_RSA_WITH_RC4_128_SHA).")
    private String ciphers = null;

    // receives other command line parameters than options
    @Argument
    private List<String> arguments = new ArrayList<String>();

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


    public static void main(String[] args) {
        new Main().doMain(args);

    }

    private void doMain(String[] args) {
        String url = null;
        String[] protocols = null;
        String[] ciphers = null;

        CmdLineParser parser = new CmdLineParser(this);
        try {
            parser.parseArgument(args);

            if (arguments.isEmpty())
                throw new CmdLineException(parser, "No argument is given");

            url = arguments.get(0);
            protocols = this.protocols.split(",");

            if (StringUtils.isNotEmpty(this.ciphers)) {
                ciphers = this.ciphers.split(",");
            }

            run(
                    url,
                    protocols,
                    ciphers
            );
        } catch (CmdLineException e) {
            e.printStackTrace();
        }

    }

    private void run(String url, String[] protocols, String[] ciphers) {
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

    private WebTarget makeTarget(String url, String[] protocols, String[] ciphers, SSLContext sslContext) {
        Client client = ClientBuilder.newClient();
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

        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslSocketFactory)
                .build();

        BasicHttpClientConnectionManager connectionManager = new BasicHttpClientConnectionManager(registry);
        config.property(ApacheClientProperties.CONNECTION_MANAGER, connectionManager);

        client = JerseyClientBuilder.newClient(config);
        return client.target(url);
    }

    private Response callPostAPI(WebTarget target, String form, Map<String, String> headerMap) {

        Invocation.Builder invocationBuilder = target.request();
        Entity<String> entity = (form != null) ? Entity.entity(form, MediaType.APPLICATION_JSON_TYPE) : null;

        for (Entry<String, String> entry : headerMap.entrySet()) {
            invocationBuilder.header(entry.getKey(), entry.getValue());
        }

        return (Response)invocationBuilder.post(entity);
    }
}
