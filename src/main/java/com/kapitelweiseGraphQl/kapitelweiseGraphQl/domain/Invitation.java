package com.kapitelweiseGraphQl.kapitelweiseGraphQl.domain;

import com.kapitelweiseGraphQl.kapitelweiseGraphQl.response.BookClubResponse;

public record Invitation(Long invitationId, Long invitedUserId, BookClubResponse bookclub) {
}
