package com.kapitelweiseGraphQl.kapitelweiseGraphQl.service;

import com.kapitelweiseGraphQl.kapitelweiseGraphQl.dao.BookclubDao;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.dao.UserDao;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.domain.BookClub;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.response.BookClubDetailResponse;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.response.BookClubResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class BookClubService {
    private static final Logger log = LoggerFactory.getLogger(BookClubService.class);

    private final BookclubDao bookclubDao;
    private final UserDao userDao;
    private final AmazonSQSService amazonSQSService;

    public BookClubService(BookclubDao bookclubDao, UserDao userDao, AmazonSQSService amazonSQSService){
        this.bookclubDao = bookclubDao;
        this.userDao = userDao;
        this.amazonSQSService = amazonSQSService;
    }

    public List<BookClubResponse> getClubs(String username){
        List<BookClubResponse> clubs = bookclubDao.getBookClubForMember(username);
        log.info(username + " ist in " + clubs.size() + " Clubs");
        return clubs;
    }

    public BookClubDetailResponse getBookClubDetails(Long clubId, String username){
        try{
            BookClubDetailResponse details = bookclubDao.getClubDetails(clubId, username);
            log.info("Details von Club " + clubId + " für " + username + " geladen");
            return details;
        }
        catch(EmptyResultDataAccessException e){
            log.warn(username + " wollte in Club " + clubId + " reinschauen, ist aber kein Mitglied");
           throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Kein Mitglied dieses Buchclubs");
        }
    }

    @Transactional
    public BookClubResponse createNewBookClub(String name, String description, String username){

        if(name == null || name.isBlank()) {
            log.warn(username + " wollte einen Club ganz ohne Namen gründen");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ohne Namen gibts keinen Club!");
        }

        if(name.length() > 25) {
            log.warn(username + " hat einen Clubnamen mit " + name.length() + " Zeichen versucht, bisschen viel");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ("Eingabe zu lang!"));
        }

        if(description != null && description.length() > 250) {
            log.warn(username + " hat fast einen Roman als Beschreibung geschickt (" + description.length() + " Zeichen)");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ("Eingabe zu lang!"));
        }

        Long userId = userDao.getUserId(username)
                .orElseThrow(() -> {
                    log.warn(username + " gibts hier noch nicht, die User-Queue war wohl noch nicht da");
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Dein Konto wird gerade eingerichtet, versuch es in ein paar Sekunden noch mal");
                });

        BookClub bookclub = bookclubDao.createBookclub(new BookClub(name, description));
        log.info("Club " + bookclub.getId() + " (" + name + ") wurde gegründet");

        bookclubDao.addMember(bookclub.getId(), userId, "ADMIN");
        log.info(username + " ist jetzt Admin von Club " + bookclub.getId());


        amazonSQSService.sendNewMember("MEMBER_JOINED", userId, username, bookclub.getId(), "ADMIN");


        return new BookClubResponse(bookclub.getId(), bookclub.getName(), bookclub.getDescription());

    }
}
