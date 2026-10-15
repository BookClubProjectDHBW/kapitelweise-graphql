package com.kapitelweiseGraphQl.kapitelweiseGraphQl.dao.impl;


import com.kapitelweiseGraphQl.kapitelweiseGraphQl.dao.InvitationDao;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.domain.Invitation;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.domain.InvitationStatus;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.response.BookClubResponse;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.response.InvitationUserResponse;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.response.UserResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class InvitationDaoImpl implements InvitationDao {

    private final JdbcTemplate jdbcTemplate;

    public InvitationDaoImpl(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<InvitationUserResponse> getUserInvitations(String username) {
        String sql = """
      
     SELECT invitation.id AS invId, bookclub.id AS clubId, bookclub.name, bookclub.description, invitation.created_at, inviter.id AS invitedById, inviter.username AS invitedByName
      FROM invitation
      JOIN users invited ON invited.id = invitation.invited_user_id
      JOIN users inviter ON inviter.id = invitation.invited_by_user_id
      JOIN bookclub ON bookclub.id = invitation.bookclub_id
      WHERE invited.username = ?
        AND invitation.invitation_status = 'PENDING'
      ORDER BY invitation.created_at DESC
            
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                        new InvitationUserResponse(
                                rs.getLong("invId"),
                                new BookClubResponse(
                                        rs.getLong("clubId"),
                                        rs.getString("name"),
                                        rs.getString("description")
                                ),
                                rs.getTimestamp("created_at").toLocalDateTime(),
                                new UserResponse(
                                        rs.getLong("invitedById"),
                                        rs.getString("invitedByName")
                                )

                        ),
                username
        );


    }

    @Override
    public Optional<Invitation> findInvitation(Long invitationId, String username) {
        String sql = """
                SELECT invitation.id, invitation.invited_user_id,
                       bookclub.id AS club_id, bookclub.name, bookclub.description
                FROM invitation
                JOIN users    ON users.id = invitation.invited_user_id
                JOIN bookclub ON bookclub.id = invitation.bookclub_id
                WHERE invitation.id = ? AND users.username = ?
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                        new Invitation(
                                rs.getLong("id"),
                                rs.getLong("invited_user_id"),
                                new BookClubResponse(
                                        rs.getLong("club_id"),
                                        rs.getString("name"),
                                        rs.getString("description")
                                )
                        ),
                        invitationId, username)
                .stream()
                .findFirst();
    }
    @Override
    public boolean answerIfPending(Long invitationId, InvitationStatus status) {
        int rows = jdbcTemplate.update("""
                UPDATE invitation
                SET invitation_status = ?, answered_at = CURRENT_TIMESTAMP
                WHERE id = ? AND invitation_status = 'PENDING'
                """, status.name(), invitationId);
        return rows > 0;
    }


    @Override
    public void addMember(Long userId, Long bookClubId) {
        jdbcTemplate.update("""
                INSERT INTO clubmember (role, userid, bookclub_id)
                VALUES ('MEMBER', ?, ?)
                ON CONFLICT (userid, bookclub_id) DO NOTHING
                """, userId, bookClubId);
    }
}
