package fun.gre.tlscurl;

import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.SslConfigurator;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.Map.Entry;

class Main {

    private static final String VERSION = "v1.1.1";

    @Option(name = "-p", aliases = "--protocols", required = false, usage = "Provide protocols for TSL connection (i.e TLSv1, TLSv1.1 or TLSv1.2).")
    private String protocols = "TLSv1,TLSv1.1,TLSv1.2";

    @Option(name = "-c", aliases = "--ciphers", required = false, usage = "Provide cipher suites for connection (i.e TLS_RSA_WITH_RC4_128_SHA).")
    private String ciphers = null;

    @Option(name = "--list-ciphers", required = false, usage = "Show all the available ciphers starting with \"*\".")
    private Boolean listCiphersFlag = false;

    @Option(name = "-x", required = false, usage = "Provide proxy address.")
    private String proxyUrl = "";

    @Option(name = "-k", required = false, usage = "Allow self-certified SSL.")
    private Boolean allowAllCerts = false;

    @Option(name = "-V", aliases = "--version", required = false, usage = "Show version and exit.")
    private Boolean versionFlag = false;

    @Option(name = "--help", required = false, usage = "Show help and exit.")
    private Boolean helpFlag = false;

    @Argument
    private List<String> arguments = new ArrayList<String>();

    private TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public void checkClientTrusted(X509Certificate[] certs, String authType) {
        }

        public void checkServerTrusted(X509Certificate[] certs, String authType) {
        }
    }};

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

            if (this.helpFlag) {
                System.out.println("Usage:");
                System.out.println(" tlscurl [options]");
                System.out.println(" tlscurl [options] [arguments]");
                System.out.println();
                System.out.println("Options:");
                parser.printUsage(System.out);
                System.exit(0);
            }

            if (this.versionFlag) {
                System.err.println(this.VERSION);
                System.exit(0);
            }

            if (this.listCiphersFlag) {
                showAvailableCiphers();
                System.exit(0);
            }

            if (arguments.isEmpty())
                throw new CmdLineException(parser, "No argument is given");

            url = arguments.get(0);
            protocols = this.protocols.split(",");

            if (StringUtils.isNotEmpty(this.ciphers))
                ciphers = this.ciphers.split(",");


        Map<String, String> headers = new HashMap<String, String>();
            headers.put("User-Agent", "tlscurl");
            WebTarget target;
            target = makeTarget(url, protocols, ciphers, this.proxyUrl, null);
            Response res = callPostAPI(target, "{\"payload\":1}", headers);

            System.out.println(res.toString());
            System.out.println(res.readEntity(String.class));
        } catch (CmdLineException e) {
            e.printStackTrace();
        }

    }

    private WebTarget makeTarget(String url,
                                 String[] protocols,
                                 String[] ciphers,
                                 String proxyUrl,
                                 SSLContext sslContext) {
        Client client = ClientBuilder.newClient();

        LayeredConnectionSocketFactory sslSocketFactory;
        if (sslContext == null) {
            sslContext = SslConfigurator.getDefaultContext();
        }
        try {
            if (allowAllCerts) {
                sslContext.init(null, this.trustAllCerts, new SecureRandom());
            }
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        sslSocketFactory = new SSLConnectionSocketFactory(
                sslContext,
                protocols,
                ciphers,
                SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);

        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslSocketFactory)
                .build();

        ApacheConnectorProvider provider = new ApacheConnectorProvider();
        ClientConfig config = new ClientConfig().connectorProvider(provider);

        BasicHttpClientConnectionManager connectionManager = new BasicHttpClientConnectionManager(registry);
        config.property(ApacheClientProperties.CONNECTION_MANAGER, connectionManager);

        if (StringUtils.isNotBlank(proxyUrl)) {
            config.property(ClientProperties.PROXY_URI, URI.create(proxyUrl));
        }

        client = JerseyClientBuilder.newClient(config);
        return client.target(url);
    }

    private Response callPostAPI(WebTarget target, String form, Map<String, String> headerMap) {

        Invocation.Builder invocationBuilder = target.request();
        Entity<String> entity = (form != null) ? Entity.entity(form, MediaType.APPLICATION_JSON_TYPE) : null;

        for (Entry<String, String> entry : headerMap.entrySet()) {
            invocationBuilder.header(entry.getKey(), entry.getValue());
        }

        return invocationBuilder.post(entity);
    }

    private void showAvailableCiphers() {
        {
            SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();

            String[] defaultCiphers = ssf.getDefaultCipherSuites();
            String[] availableCiphers = ssf.getSupportedCipherSuites();

            TreeMap ciphers = new TreeMap();

            for (int i = 0; i < availableCiphers.length; ++i)
                ciphers.put(availableCiphers[i], Boolean.FALSE);

            for (int i = 0; i < defaultCiphers.length; ++i)
                ciphers.put(defaultCiphers[i], Boolean.TRUE);

            System.out.println("Default\tCipher");
            for (Iterator i = ciphers.entrySet().iterator(); i.hasNext(); ) {
                Map.Entry cipher = (Map.Entry) i.next();

                if (Boolean.TRUE.equals(cipher.getValue()))
                    System.out.print('*');
                else
                    System.out.print(' ');

                System.out.print('\t');
                System.out.println(cipher.getKey());
            }
        }
    }
}
