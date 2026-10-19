package com.kapitelweiseGraphQl.kapitelweiseGraphQl.domain;

public record MemberEvent(String type, Long userId, String username, Long bookclubId, String role) {
}
