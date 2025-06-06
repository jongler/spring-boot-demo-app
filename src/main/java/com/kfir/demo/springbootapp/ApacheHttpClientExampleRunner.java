package com.kfir.demo.springbootapp;

import com.kfir.demo.springbootapp.model.MyBody;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.apache.hc.core5.util.Timeout;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ApacheHttpClientExampleRunner implements CommandLineRunner {

  @Override
  public void run(String... args) {
    RestTemplate restTemplate = buildRestTemplate();
    // GET request
    String getUrl = "https://jsonplaceholder.typicode.com/posts/1";
    ResponseEntity<MyBody> getResponse = restTemplate.getForEntity(getUrl, MyBody.class);
    System.out.println("** GET Response Status: " + getResponse.getStatusCode());
    System.out.println("** GET Response Body: " + getResponse.getBody());

    // POST request - request/response as String
    String postUrl = "https://jsonplaceholder.typicode.com/posts";
    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/json");
    String json = "{\"title\": \"foo\", \"body\": \"bar\", \"userId\": 1}";
    HttpEntity<String> postEntity = new HttpEntity<>(json, headers);

    ResponseEntity<String> postResponse = restTemplate.postForEntity(postUrl, postEntity, String.class);
    System.out.println("** POST Response Status: " + postResponse.getStatusCode());
    System.out.println("** POST Response Body: " + postResponse.getBody());

    // POST request - request/response as MyBody
    MyBody myBody = new MyBody("foo", "bar", 1, 10);
    HttpEntity<MyBody> postEntity2 = new HttpEntity<>(myBody, headers);
    ResponseEntity<MyBody> postResponse2 = restTemplate.postForEntity(postUrl, postEntity2, MyBody.class);
    System.out.println("** POST Response Status2: " + postResponse2.getStatusCode());
    System.out.println("** POST Response Body2: " + postResponse2.getBody());
  }

  private RestTemplate buildRestTemplate() {
    try {
      TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

      SSLContext sslContext = SSLContexts.custom()
          .loadTrustMaterial(null, acceptingTrustStrategy)
          .build();

      SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

      ConnectionConfig connConfig = ConnectionConfig.custom()
          .setConnectTimeout(5000, TimeUnit.MILLISECONDS)
          .setSocketTimeout(5000, TimeUnit.MILLISECONDS)
          .build();

      RequestConfig requestConfig = RequestConfig.custom()
          .setConnectionRequestTimeout(Timeout.ofMilliseconds(10000))
          .build();

      PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(
          RegistryBuilder.<ConnectionSocketFactory>create()
              .register("https", csf)
              .register("http", PlainConnectionSocketFactory.getSocketFactory())
              .build()
      );
      cm.setDefaultConnectionConfig(connConfig);

      CloseableHttpClient httpClient = HttpClients.custom()
          .setConnectionManager(cm)
          .setDefaultRequestConfig(requestConfig)
          .build();

      HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
      return new RestTemplate(requestFactory);
    } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
      throw new RuntimeException(e);
    }
  }
}
