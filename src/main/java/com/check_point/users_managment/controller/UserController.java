package com.check_point.users_managment.controller;

import com.check_point.users_managment.entity.User;
import com.check_point.users_managment.exception.ResourceNotFoundException;
import com.check_point.users_managment.response.UserResponse;
import com.check_point.users_managment.service.UserService;
import com.check_point.users_managment.utils.ConvertUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.retry.annotation.Recover;




@RestController
@RequestMapping(path = "/api/user")
@EnableRetry
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody User user) throws JsonProcessingException {
        UserResponse userResponse=null;
        try {
            User addedUser = userService.addUser(user, true);
            userResponse = ConvertUtil.convertObject(addedUser, UserResponse.class);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(userResponse, HttpStatus.CREATED);
    }

    @Retryable(value = { Exception.class },maxAttempts = 5,backoff = @Backoff(delay = 5000))
    @GetMapping(path = "/{id}")
    public ResponseEntity<UserResponse> findUserById(@PathVariable("id") Long id) {
        ResponseEntity<UserResponse> userEntity=null;
        try {
            userEntity = userService.findUserById(id);
        } catch (Exception e) {
            System.out.println("Failed to get user from DB - try again");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return userEntity;
    }


    @GetMapping
    @Retryable(value = { Exception.class }, maxAttempts = 5, backoff = @Backoff(delay = 5000))
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            List<UserResponse> userResponses = users.stream()
                    .map(user -> {
                        try {
                            return ConvertUtil.convertObject(user, UserResponse.class);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException("Failed to convert User to UserResponse", e);
                        }
                    })
                    .collect(Collectors.toList());
            return ResponseEntity.ok(userResponses);
        } catch (Exception e) {
            System.out.println("Failed to get users from DB - try again");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteById(@PathVariable("id") Long id) {
        try {
            Boolean result = userService.deleteUserById(id, true);
            if(result){
                return ResponseEntity.ok("User Deleted Successfully");
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found ");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error occurred while deleting the user: " + e.getMessage());
        }
    }


    @Recover
    public void recover(Exception e){
        System.out.println("Failed to fetch from DB after several attempts" +e +  e.getMessage());
    }


}
