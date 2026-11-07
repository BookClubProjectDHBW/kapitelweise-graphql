package com.kapitelweiseGraphQl.kapitelweiseGraphQl.dao.impl;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.kapitelweiseGraphQl.kapitelweiseGraphQl.dao.BookclubDao;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.domain.BookClub;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.domain.MemberEvent;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.response.BookClubDetailResponse;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.response.BookClubResponse;


@Repository
public class BookClubDaoImpl implements BookclubDao {

    private final JdbcTemplate jdbcTemplate;

    public BookClubDaoImpl(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }


    @Override
    public BookClub createBookclub(BookClub bookClub){

        return jdbcTemplate.queryForObject("insert into bookclub(name, description) Values(?, ?) Returning id, name, description, created_at",
                (rs, rowNum) -> new BookClub(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                ), bookClub.getName(), bookClub.getDescription());
    }

    @Override
    public void addMember(Long bookclubId, Long userId, String role){
        jdbcTemplate.update("Insert into clubmember (bookclub_id, userId, role) Values(?,?,?)", bookclubId, userId, role);
    }


    @Override
    public List<BookClubResponse> getBookClubForMember(String username) {
        String sql = """
        select bookclub.name,  bookclub.description, bookclub.id
        from bookclub
        join clubmember on clubmember.bookclub_id = bookclub.id
        join club_user on club_user.id = clubmember.userid
        where username = ? and clubmember.left_at is null;
        """;
        return jdbcTemplate.query(sql,(rs, rowNum) ->
                        new BookClubResponse(
                                rs.getLong("id"),
                                rs.getString("name"),
                                rs.getString("description")
                        ),

                username
        );
    }

    @Override
    public boolean bookclubExists(Long clubId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM bookclub WHERE id = ?",
                Integer.class, clubId);
        return count != null && count > 0;
    }

    @Override
    public BookClubDetailResponse getClubDetails(Long clubId, String username){
        String sql= """
                select bookclub.id, bookclub.name,  bookclub.description, clubmember.role
                from bookclub
                join clubmember on clubmember.bookclub_id = bookclub.id
                join club_user on club_user.id = clubmember.userid
                where bookclub.id = ? and club_user.username = ? and clubmember.left_at is null;
                """;
        return jdbcTemplate.queryForObject(sql,(rs, rowNum) ->
                        new BookClubDetailResponse(
                                rs.getLong("id"),
                                rs.getString("name"),
                                rs.getString("description"),
                                rs.getString("role")
                        ),

                clubId, username
        );
    }

    // Mit Hilfe der KI entwickelt
    @Override
    public List<MemberEvent> getUnsentMembers(){
        String sql = """
                select clubmember.userid, club_user.username, clubmember.bookclub_id, clubmember.role
                from clubmember
                join club_user on club_user.id = clubmember.userid
                where clubmember.member_sent = false
                and clubmember.left_at is null
                and clubmember.join_date < now() - interval '2 minutes'
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new MemberEvent(
                        "MEMBER_JOINED",
                        rs.getLong("userid"),
                        rs.getString("username"),
                        rs.getLong("bookclub_id"),
                        rs.getString("role")
                ));
    }

    // Mit Hilfe der KI entwickelt
    @Override
    public void markMemberSent(Long userId, Long bookclubId){
        jdbcTemplate.update("update clubmember set member_sent = true where userid = ? and bookclub_id = ?", userId, bookclubId);
    }
    @Override
public int removeMember(Long clubId, Long userId) {
    return jdbcTemplate.update(
            "UPDATE clubmember SET left_at = now() WHERE bookclub_id = ? AND userid = ? AND role = 'MEMBER' AND left_at IS NULL",
            clubId, userId
    );
}

    @Override
    public List<MemberEvent> getUnsentDepartures(){
        String sql = """
                select clubmember.userid, club_user.username, clubmember.bookclub_id, clubmember.role
                from clubmember
                join club_user on club_user.id = clubmember.userid
                where clubmember.left_at is not null
                and clubmember.left_at < now() - interval '2 minutes'
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new MemberEvent(
                        "MEMBER_LEFT",
                        rs.getLong("userid"),
                        rs.getString("username"),
                        rs.getLong("bookclub_id"),
                        rs.getString("role")
                ));
    }

    @Override
    public void markDepartureSent(Long userId, Long bookclubId){
        jdbcTemplate.update("delete from clubmember where userid = ? and bookclub_id = ? and left_at is not null", userId, bookclubId);
    }
}
