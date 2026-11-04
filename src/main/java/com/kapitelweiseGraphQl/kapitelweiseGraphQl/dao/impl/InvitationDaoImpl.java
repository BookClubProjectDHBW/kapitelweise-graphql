package com.kapitelweiseGraphQl.kapitelweiseGraphQl.dao.impl;


import com.kapitelweiseGraphQl.kapitelweiseGraphQl.dao.InvitationDao;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.domain.BookClub;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.domain.Invitation;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.domain.InvitationStatus;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.response.BookClubResponse;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.response.InvitationResponse;
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
      JOIN club_user invited ON invited.id = invitation.invited_user_id
      JOIN club_user inviter ON inviter.id = invitation.invited_by_user_id
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
                       bookclub.id AS club_id, bookclub.name, bookclub.description, bookclub.created_at
                FROM invitation
                JOIN club_user ON club_user.id = invitation.invited_user_id
                JOIN bookclub ON bookclub.id = invitation.bookclub_id
                WHERE invitation.id = ? AND club_user.username = ?
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                        new Invitation(
                                rs.getLong("id"),
                                rs.getLong("invited_user_id"),
                                new BookClub(
                                        rs.getLong("club_id"),
                                        rs.getString("name"),
                                        rs.getString("description"),
                                        rs.getTimestamp("created_at").toLocalDateTime()
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
                ON CONFLICT (userid, bookclub_id) DO UPDATE
                SET left_at = NULL, member_sent = false, join_date = now()
                WHERE clubmember.left_at IS NOT NULL
                """, userId, bookClubId);
    }

    @Override
    public boolean isUserAdmin(Long userId, Long bookClubId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM clubmember WHERE userid = ? AND bookclub_id = ? AND role = 'ADMIN'",
                Integer.class, userId, bookClubId);
        return count != null && count > 0;
    }

    @Override
    public InvitationResponse createInvitation(Long bookClubId, Long invitedUserId, Long invitedById, String invitedUsername) {
        return jdbcTemplate.queryForObject("insert into invitation(invitation_status, invited_user_id, bookclub_id, invited_by_user_id) Values(?,?,?,?) Returning id, invitation_status, created_at",
                (rs, rowNum) -> new InvitationResponse(
                        rs.getLong("id"),
                        rs.getString("invitation_status"),
                        invitedUsername,
                        rs.getTimestamp("created_at").toLocalDateTime()
                ), InvitationStatus.PENDING.name(), invitedUserId, bookClubId, invitedById);
    }

    @Override
    public boolean isAlreadyMember(Long invitedUserId, Long bookClubId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM clubmember WHERE userid = ? AND bookclub_id = ? AND left_at IS NULL",
                Integer.class, invitedUserId, bookClubId);
        return count != null && count > 0;
    }


    @Override
    public boolean invitationPending(Long invitedUserId, Long bookClubId) {
        Integer count = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM invitation WHERE invited_user_id = ? AND bookclub_id = ? AND invitation_status = 'PENDING'",
                Integer.class, invitedUserId, bookClubId);
        return count != null && count > 0;
    }
}
