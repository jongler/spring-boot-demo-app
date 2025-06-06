package com.kfir.demo.springbootapp.spring.configuration;

import com.kfir.demo.springbootapp.client.JsonPlaceHolderClient;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

  @Bean
  public JsonPlaceHolderClient jsonPlaceHolderClient() {
    RestTemplate restTemplate = buildRestTemplate();
    return new JsonPlaceHolderClient(restTemplate);
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
