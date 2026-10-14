package com.kapitelweiseGraphQl.kapitelweiseGraphQl.dao;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.response.BookClubResponse;
import java.util.List;


public interface BookclubDao {
    List<BookClubResponse> getBookClubForMember(String username);
}
