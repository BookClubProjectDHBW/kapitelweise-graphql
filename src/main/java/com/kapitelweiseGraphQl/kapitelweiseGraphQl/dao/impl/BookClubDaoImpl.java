package com.kapitelweiseGraphQl.kapitelweiseGraphQl.dao.impl;

import com.kapitelweiseGraphQl.kapitelweiseGraphQl.dao.BookclubDao;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.domain.BookClub;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.response.BookClubDetailResponse;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.response.BookClubResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;



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
        where username = ?;
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
                where bookclub.id = ? and club_user.username = ? ;
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
}
