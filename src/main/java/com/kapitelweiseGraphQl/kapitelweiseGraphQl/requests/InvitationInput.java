package com.kapitelweiseGraphQl.kapitelweiseGraphQl.requests;

import com.kapitelweiseGraphQl.kapitelweiseGraphQl.domain.InvitationStatus;

// Feldnamen müssen exakt wie im input InvitationInput im Schema heißen
public record InvitationInput(Long invitationId, InvitationStatus status) {
}
