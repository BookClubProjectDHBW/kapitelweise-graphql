package com.kapitelweiseGraphQl.kapitelweiseGraphQl.service;


import com.kapitelweiseGraphQl.kapitelweiseGraphQl.dao.UserDao;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.domain.Member;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {
    private final UserDao userDao;


    public UserService(UserDao userDao){
        this.userDao = userDao;
    }


    public Member returnMember(String username){

        Long id = userDao.getUserId(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dieser User existiert nicht."));
        return new Member(username, id);
    }
}
