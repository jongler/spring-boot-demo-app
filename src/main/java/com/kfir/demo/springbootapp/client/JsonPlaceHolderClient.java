package com.kfir.demo.springbootapp.client;

import com.kfir.demo.springbootapp.model.PostPayload;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class JsonPlaceHolderClient {

  private static final String BASE_URL = "http://jsonplaceholder.typicode.com";
  private static final String GET_POSTS_URL = BASE_URL + "/posts/{postId}";
  private static final String POST_POSTS_URL = BASE_URL + "/posts";

  private final RestTemplate restTemplate;

  public JsonPlaceHolderClient(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public PostPayload getPost(int postId) {
    ResponseEntity<PostPayload> getResponse = restTemplate.getForEntity(GET_POSTS_URL, PostPayload.class, postId);
    return getResponse.getBody();
  }

  public PostPayload postPost(PostPayload postPayload) {
//    HttpHeaders headers = new HttpHeaders();
//    headers.set("Content-Type", "application/json");
    HttpEntity<PostPayload> postEntity = new HttpEntity<>(postPayload);
    ResponseEntity<PostPayload> postResponse = restTemplate.postForEntity(POST_POSTS_URL, postEntity, PostPayload.class);
    return postResponse.getBody();
  }
}
