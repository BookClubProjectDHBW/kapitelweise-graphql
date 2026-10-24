package com.kapitelweiseGraphQl.kapitelweiseGraphQl.dao;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.domain.BookClub;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.response.BookClubDetailResponse;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.response.BookClubResponse;
import java.util.List;


public interface BookclubDao {
    List<BookClubResponse> getBookClubForMember(String username);
    BookClub createBookclub(BookClub bookClub);
    void addMember(Long bookclubId, Long userId, String role);
    BookClubDetailResponse getClubDetails(Long clubId, String username);
    boolean bookclubExists(Long clubId);
}
