package com.kapitelweiseGraphQl.kapitelweiseGraphQl.dao.impl;


import com.kapitelweiseGraphQl.kapitelweiseGraphQl.dao.InvitationDao;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.dao.UserDao;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.response.BookClubResponse;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.response.InvitationUserResponse;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.response.UserResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

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
}
