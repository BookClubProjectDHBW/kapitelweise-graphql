package com.kapitelweiseGraphQl.kapitelweiseGraphQl.dao;

import com.kapitelweiseGraphQl.kapitelweiseGraphQl.domain.Invitation;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.domain.InvitationStatus;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.response.InvitationResponse;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.response.InvitationUserResponse;

import java.util.List;
import java.util.Optional;

public interface InvitationDao {
    List<InvitationUserResponse> getUserInvitations(String username);
    Optional<Invitation> findInvitation(Long invitationId, String username);
    boolean answerIfPending(Long invitationId, InvitationStatus status);
    void addMember(Long userId, Long bookClubId);
    boolean isUserAdmin(Long userId, Long bookClubId);
    InvitationResponse createInvitation(Long bookClubId, Long invitedUserId, Long invitedById, String invitedUsername);
    boolean invitationPending(Long invitedUserId, Long bookClubId);
    boolean isAlreadyMember(Long invitedUserId, Long bookClubId);
}
