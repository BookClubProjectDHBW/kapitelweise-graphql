package com.kapitelweiseGraphQl.kapitelweiseGraphQl.service;

import com.kapitelweiseGraphQl.kapitelweiseGraphQl.dao.BookclubDao;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.response.BookClubResponse;
import org.springframework.stereotype.Service;

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
}