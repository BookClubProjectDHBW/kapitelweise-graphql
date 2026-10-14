package com.kapitelweiseGraphQl.kapitelweiseGraphQl.dao;

import com.kapitelweiseGraphQl.kapitelweiseGraphQl.response.InvitationUserResponse;

import java.util.List;

public interface InvitationDao {
    List<InvitationUserResponse> getUserInvitations(String username);
}
