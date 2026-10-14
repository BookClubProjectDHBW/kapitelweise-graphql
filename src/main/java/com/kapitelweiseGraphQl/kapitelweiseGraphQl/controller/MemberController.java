package com.kapitelweiseGraphQl.kapitelweiseGraphQl.controller;

import com.kapitelweiseGraphQl.kapitelweiseGraphQl.dao.BookclubDao;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.domain.BookClub;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.domain.Member;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.response.BookClubResponse;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.response.InvitationUserResponse;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.service.BookClubService;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.service.UserService;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.service.InvitationService;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class MemberController {

    private final UserService userService;
    private final BookClubService bookClubService;
    private final InvitationService invitationService;

    public MemberController(UserService userService, BookClubService bookClubService, InvitationService invitationService) {

        this.userService = userService;
        this.bookClubService = bookClubService;
        this.invitationService = invitationService;
    }


    @QueryMapping(name="member")
    public Member findName(@AuthenticationPrincipal String username) {
        return userService.returnMember(username);
    }


    @SchemaMapping(typeName = "Member", field="membership")
    public List<BookClubResponse> bookclub(@AuthenticationPrincipal String username){
        return bookClubService.getClubs(username);
    }

    @SchemaMapping(typeName = "Member", field="invitationPending")
        public List<InvitationUserResponse> invitations(@AuthenticationPrincipal String username){
        return invitationService.getInvitations(username);

    }
}
