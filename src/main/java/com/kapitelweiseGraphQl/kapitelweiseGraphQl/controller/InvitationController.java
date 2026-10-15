package com.kapitelweiseGraphQl.kapitelweiseGraphQl.controller;

import com.kapitelweiseGraphQl.kapitelweiseGraphQl.requests.InvitationInput;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.response.BookClubResponse;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.service.InvitationService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Controller
public class InvitationController {


    private final InvitationService invitationService;

    public InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }


    @MutationMapping
    public BookClubResponse responseInvitation(@Argument InvitationInput invitationInput, @AuthenticationPrincipal String username){
        return invitationService.responseInvitation(invitationInput.invitationId(), invitationInput.status(), username);
    }
}
