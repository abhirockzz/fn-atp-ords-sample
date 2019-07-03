package io.fnproject.example;

import com.fnproject.fn.api.RuntimeContext;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

public class UpdateEmployeeFunction {

    private static final String ORDS_REST_SERVICE_ENDPOINT = System.getenv().getOrDefault("ORDS_REST_SERVICE_ENDPOINT", "http://localhost:8080/ords/pdb1/rest-workspace/hr/");

    private Client client = null;

    public UpdateEmployeeFunction(RuntimeContext ctx) {

        TrustManager[] customTrustManager = new TrustManager[]{
            new X509TrustManager() {

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                }
            }
        };

        try {
            SSLContext sc = SSLContext.getInstance("ssl");
            sc.init(null, customTrustManager, null);

            this.client = ClientBuilder
                    .newBuilder()
                    .sslContext(sc) 
                    .hostnameVerifier((a, b) -> true)
                    .build();

        } catch (Throwable e) {
            System.err.println("Error - " + e.getMessage());
        }
    }

    public String handle(EmployeeInfo empInfo) {
        WebTarget target = null;
        String queryEndpoint = ORDS_REST_SERVICE_ENDPOINT + "employees/" + empInfo.getEmpno();
        System.out.println("ORDS query endppoint " + queryEndpoint);

        String result = null;
        try {

            target = this.client.target(queryEndpoint);
            /*
            the JSON representation of EmployeeInfo is internally taken care of by jersey-media-json-binding module 
            (a MessageBodyWriter is made available)
             */
            int respStatus = target.request().put(Entity.entity(empInfo, MediaType.APPLICATION_JSON)).getStatus();
            result = (respStatus == 200) ? "SUCCESS" : "FAILED to update info for employee no. " + empInfo.getEmpno();

        } catch (Exception se) {
            System.err.println("Unable to fetch employee info " + se.getMessage());
        }
        return result;
    }

}
