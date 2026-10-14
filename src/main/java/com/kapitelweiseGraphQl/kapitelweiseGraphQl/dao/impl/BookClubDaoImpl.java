package com.kapitelweiseGraphQl.kapitelweiseGraphQl.dao.impl;

import com.kapitelweiseGraphQl.kapitelweiseGraphQl.dao.BookclubDao;
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
    public List<BookClubResponse> getBookClubForMember(String username) {
        String sql = """
        select bookclub.name,  bookclub.description, bookclub.id
        from bookclub
        join clubmember on clubmember.bookclub_id = bookclub.id
        join users on users.id = clubmember.userid
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
}
