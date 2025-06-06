package com.kfir.demo.springbootapp;

import com.kfir.demo.springbootapp.client.JsonPlaceHolderClient;
import com.kfir.demo.springbootapp.model.PostPayload;
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
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
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

  private final JsonPlaceHolderClient jsonPlaceHolderClient;

  public ApacheHttpClientExampleRunner(JsonPlaceHolderClient jsonPlaceHolderClient) {
    this.jsonPlaceHolderClient = jsonPlaceHolderClient;
  }

  @Override
  public void run(String... args) {
    PostPayload postPayloadResp = jsonPlaceHolderClient.getPost(2);
    System.out.println("** GET Post Payload: " + postPayloadResp);

    PostPayload postPayloadToPost = new PostPayload("foo", "bar", 1, 10);
    postPayloadResp = jsonPlaceHolderClient.postPost(postPayloadToPost);
    System.out.println("** POST Post Payload: " + postPayloadResp);
//
//    RestTemplate restTemplate = buildRestTemplate();
//    // GET request
//    String getUrl = "https://jsonplaceholder.typicode.com/posts/1";
//    ResponseEntity<PostPayload> getResponse = restTemplate.getForEntity(getUrl, PostPayload.class);
//    System.out.println("** GET Response Status: " + getResponse.getStatusCode());
//    System.out.println("** GET Response Body: " + getResponse.getBody());
//
//    // POST request - request/response as String
//    String postUrl = "https://jsonplaceholder.typicode.com/posts";
//    HttpHeaders headers = new HttpHeaders();
//    headers.set("Content-Type", "application/json");
//    String json = "{\"title\": \"foo\", \"body\": \"bar\", \"userId\": 1}";
//    HttpEntity<String> postEntity = new HttpEntity<>(json, headers);
//
//    ResponseEntity<String> postResponse = restTemplate.postForEntity(postUrl, postEntity, String.class);
//    System.out.println("** POST Response Status: " + postResponse.getStatusCode());
//    System.out.println("** POST Response Body: " + postResponse.getBody());
//
//    // POST request - request/response as MyBody
//    PostPayload postPayload = new PostPayload("foo", "bar", 1, 10);
//    HttpEntity<PostPayload> postEntity2 = new HttpEntity<>(postPayload, headers);
//    ResponseEntity<PostPayload> postResponse2 = restTemplate.postForEntity(postUrl, postEntity2, PostPayload.class);
//    System.out.println("** POST Response Status2: " + postResponse2.getStatusCode());
//    System.out.println("** POST Response Body2: " + postResponse2.getBody());
  }

  private RestTemplate buildRestTemplate() {
    try {
      TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

      SSLContext sslContext = SSLContexts.custom()
          .loadTrustMaterial(null, acceptingTrustStrategy)
          .build();

      SSLConnectionSocketFactory csf = SSLConnectionSocketFactoryBuilder.create()
          .setSslContext(sslContext)
          .build();

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
