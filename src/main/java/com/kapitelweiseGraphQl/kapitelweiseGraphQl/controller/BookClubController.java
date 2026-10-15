package com.kapitelweiseGraphQl.kapitelweiseGraphQl.controller;

import com.kapitelweiseGraphQl.kapitelweiseGraphQl.requests.BookClubInput;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.response.BookClubResponse;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.service.BookClubService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;


@Controller
public class BookClubController {
    private final BookClubService bookClubService;

    public BookClubController(BookClubService bookClubService) {
        this.bookClubService = bookClubService;
    }


    @MutationMapping
    public BookClubResponse createNewBookClub(@Argument BookClubInput bookClubInput, @AuthenticationPrincipal String username){
        return bookClubService.createNewBookClub(bookClubInput.name(), bookClubInput.description(), username);
    }
}