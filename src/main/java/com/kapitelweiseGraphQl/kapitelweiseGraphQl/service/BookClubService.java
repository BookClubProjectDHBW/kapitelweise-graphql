package com.kapitelweiseGraphQl.kapitelweiseGraphQl.service;

import com.kapitelweiseGraphQl.kapitelweiseGraphQl.dao.BookclubDao;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.domain.BookClub;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.response.BookClubResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class BookClubService {
    private final BookclubDao bookclubDao;

    public BookClubService(BookclubDao bookclubDao){
        this.bookclubDao = bookclubDao;
    }

    public List<BookClubResponse> getClubs(String username){
        return bookclubDao.getBookClubForMember(username);
    }

    @Transactional
    public BookClubResponse createNewBookClub(String name, String description, String username){

        if(name.length() > 25) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ("Eingabe zu lang!"));

        if(description != null && description.length() > 250) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ("Eingabe zu lang!"));
        BookClub bookclub = bookclubDao.createBookclub(new BookClub(name, description));

        bookclubDao.addMember(bookclub.getId(), username, "ADMIN");


        return new BookClubResponse(bookclub.getId(), bookclub.getName(), bookclub.getDescription());

    }
}