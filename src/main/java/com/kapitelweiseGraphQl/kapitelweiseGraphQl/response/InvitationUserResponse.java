package com.kapitelweiseGraphQl.kapitelweiseGraphQl.response;

import java.time.LocalDateTime;

public record InvitationUserResponse(Long id, BookClubResponse bookclub, LocalDateTime createdAt, UserResponse invitedBy) {

}
