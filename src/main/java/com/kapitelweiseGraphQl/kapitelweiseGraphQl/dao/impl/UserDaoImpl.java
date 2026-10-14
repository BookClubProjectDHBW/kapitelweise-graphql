package com.kapitelweiseGraphQl.kapitelweiseGraphQl.dao.impl;

import com.kapitelweiseGraphQl.kapitelweiseGraphQl.dao.UserDao;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

//die konkrete Implementierung, hier führen wir unten die Datenbankabfrage durch.
//@Repository ist ein Spring Annotation, die sagt, dass diese Klasse ein Repository ist, also eine Datenzugriffsschicht.
// Geschrieben wird die Tabelle users nur vom Auth-Service, hier wird nur gelesen.


@Repository
public class UserDaoImpl implements UserDao {


    private final JdbcTemplate jdbcTemplate;

    public UserDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Long> getUserId(String username){
        return jdbcTemplate.queryForList("select id from users where username = ?", Long.class, username)
                .stream().findFirst();

    }
}
