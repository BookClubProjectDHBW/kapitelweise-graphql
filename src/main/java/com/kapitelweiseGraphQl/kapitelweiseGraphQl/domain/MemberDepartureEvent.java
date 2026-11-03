package com.kapitelweiseGraphQl.kapitelweiseGraphQl.domain;

public record MemberDepartureEvent(
        Long id,
        Long userId,
        String username,
        Long bookclubId,
        String role
) {
}