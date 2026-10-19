package com.kapitelweiseGraphQl.kapitelweiseGraphQl.response;

import java.time.LocalDateTime;

public record InvitationResponse(Long id, String invitationStatus, String invitedUser, LocalDateTime createdAt) {
}
