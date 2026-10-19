package com.kapitelweiseGraphQl.kapitelweiseGraphQl.service;

import com.kapitelweiseGraphQl.kapitelweiseGraphQl.dao.BookclubDao;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.dao.UserDao;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.domain.BookClub;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.response.BookClubDetailResponse;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.response.BookClubResponse;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class BookClubService {
    private final BookclubDao bookclubDao;
    private final UserDao userDao;
    private final AmazonSQSService amazonSQSService;

    public BookClubService(BookclubDao bookclubDao, UserDao userDao, AmazonSQSService amazonSQSService){
        this.bookclubDao = bookclubDao;
        this.userDao = userDao;
        this.amazonSQSService = amazonSQSService;
    }

    public List<BookClubResponse> getClubs(String username){
        return bookclubDao.getBookClubForMember(username);
    }

    public BookClubDetailResponse getBookClubDetails(Long clubId, String username){
        try{
            return bookclubDao.getClubDetails(clubId, username);
        }
        catch(EmptyResultDataAccessException e){
           throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Kein Mitglied dieses Buchclubs");
        }
    }

    @Transactional
    public BookClubResponse createNewBookClub(String name, String description, String username){

        if(name.length() > 25) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ("Eingabe zu lang!"));

        if(description != null && description.length() > 250) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ("Eingabe zu lang!"));
        BookClub bookclub = bookclubDao.createBookclub(new BookClub(name, description));

        bookclubDao.addMember(bookclub.getId(), username, "ADMIN");

        Long userId = userDao.getUserId(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Diese UserId existiert nicht."));
        amazonSQSService.sendNewMember("MEMBER_JOINED", userId, username, bookclub.getId(), "ADMIN");


        return new BookClubResponse(bookclub.getId(), bookclub.getName(), bookclub.getDescription());

    }
}