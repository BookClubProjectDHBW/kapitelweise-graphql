package com.kapitelweiseGraphQl.kapitelweiseGraphQl.service;


import com.kapitelweiseGraphQl.kapitelweiseGraphQl.dao.UserDao;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.domain.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserDao userDao;


    public UserService(UserDao userDao){
        this.userDao = userDao;
    }


    public Member returnMember(String username){

        Long id = userDao.getUserId(username)
                .orElseThrow(() -> {
                    log.warn(username + " hat ein gültiges Token, aber kennen tun wir ihn nicht");
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Dieser User existiert nicht.");
                });
        log.info(username + " lädt die Startseite");
        return new Member(username, id);
    }
}
