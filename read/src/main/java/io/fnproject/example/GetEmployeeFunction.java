package io.fnproject.example;

import com.fnproject.fn.api.RuntimeContext;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

public class GetEmployeeFunction {

    private static final String ORDS_REST_SERVICE_ENDPOINT = System.getenv().getOrDefault("ORDS_REST_SERVICE_ENDPOINT", "http://localhost:8080/ords/pdb1/rest-workspace/hr/");

    private Client client = null;

    public GetEmployeeFunction(RuntimeContext ctx) {

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
                    .hostnameVerifier((a,b) -> true)
                    .build();

        } catch (Throwable e) {
            System.err.println("Error - " + e.getMessage());
        }
    }

    public String handle(String empID) {
        WebTarget target = null;
        String queryEndpoint = null;
        String result = "FAILED to fetch employee info";
        try {
            if (empID.equals("")) {
                System.err.println("Getting all employees...");
                queryEndpoint = ORDS_REST_SERVICE_ENDPOINT + "employees";
            } else {
                System.err.println("Fetching employee info for " + empID);
                queryEndpoint = ORDS_REST_SERVICE_ENDPOINT + "employees/" + empID;
            }

            System.out.println("ORDS query endppoint " + queryEndpoint);

            target = this.client.target(queryEndpoint);
            String employeeInfo = target.request(MediaType.APPLICATION_JSON).get(String.class);
            result = employeeInfo;

            System.err.println("Employee info " + employeeInfo);
        } catch (Exception se) {
            System.err.println("Unable to fetch employee info " + se.getMessage());
        }
        return result;
    }

}
