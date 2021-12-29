package com.programming.techie.springredditclone.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.programming.techie.springredditclone.dto.PostRequest;
import com.programming.techie.springredditclone.dto.PostResponse;
import com.programming.techie.springredditclone.service.PostService;
import lombok.AllArgsConstructor;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.ResponseEntity.status;

@RestController
@RequestMapping("/api/posts/")
@AllArgsConstructor
public class PostController {

    private final PostService postService;

    public boolean hasAccess(String user, String action, String object) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String uri = "http://localhost:4466/check";

        JSONObject jsonobj = new JSONObject();
        jsonobj.put("namespace", "default-namespace");
        jsonobj.put("subject_id", user);
        jsonobj.put("object", object);
        jsonobj.put("relation", action);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        HttpEntity<String> request =
                new HttpEntity<String>(jsonobj.toString(), headers);
        try {
            String resp = restTemplate.postForObject(uri, request, String.class);
            return resp.contains("true");

        } catch(Exception e) {
            return false;
        }
    }

    @PostMapping
    public ResponseEntity<Void> createPost(@RequestBody PostRequest postRequest) {


        if (!hasAccess("zakariaziani", "create", "comment")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } else {
            postService.save(postRequest);
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
    }

    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts() {
        return status(HttpStatus.OK).body(postService.getAllPosts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Long id) {
        return status(HttpStatus.OK).body(postService.getPost(id));
    }

    @GetMapping("by-subreddit/{id}")
    public ResponseEntity<List<PostResponse>> getPostsBySubreddit(Long id) {
        return status(HttpStatus.OK).body(postService.getPostsBySubreddit(id));
    }

    @GetMapping("by-user/{name}")
    public ResponseEntity<List<PostResponse>> getPostsByUsername(@PathVariable String name) {
        return status(HttpStatus.OK).body(postService.getPostsByUsername(name));
    }
}
