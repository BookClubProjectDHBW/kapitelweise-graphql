package com.kapitelweiseGraphQl.kapitelweiseGraphQl.controller;

import com.kapitelweiseGraphQl.kapitelweiseGraphQl.dao.BookclubDao;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.domain.BookClub;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.domain.Member;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.service.UserService;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class MemberController {

    private final UserService userService;


    public MemberController(UserService userService) {

        this.userService = userService;

    }


    @QueryMapping(name="member")
    public Member findName(@AuthenticationPrincipal String username){
      return userService.returnMember(username);
    }
}
