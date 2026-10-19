package com.kapitelweiseGraphQl.kapitelweiseGraphQl.dao.impl;

import com.kapitelweiseGraphQl.kapitelweiseGraphQl.dao.UserDao;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

//die konkrete Implementierung, hier führen wir unten die Datenbankabfrage durch.
//@Repository ist ein Spring Annotation, die sagt, dass diese Klasse ein Repository ist, also eine Datenzugriffsschicht.
// club_user wird über die Queue aus dem Auth-Service befüllt.


@Repository
public class UserDaoImpl implements UserDao {


    private final JdbcTemplate jdbcTemplate;

    public UserDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Long> getUserId(String username){
        return jdbcTemplate.queryForList("select id from club_user where username = ?", Long.class, username)
                .stream().findFirst();

    }

    @Override
    public void upsertUser(Long id, String username) {
        String sql = """
        INSERT INTO club_user (id, username)
        VALUES (?, ?)
        ON CONFLICT (id) 
        DO UPDATE SET username = EXCLUDED.username
    """;

        jdbcTemplate.update(sql, id, username);
    }
}
