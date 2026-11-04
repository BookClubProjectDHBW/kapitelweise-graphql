package com.kapitelweiseGraphQl.kapitelweiseGraphQl.dao;
import java.util.List;

import com.kapitelweiseGraphQl.kapitelweiseGraphQl.domain.BookClub;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.domain.MemberEvent;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.response.BookClubDetailResponse;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.response.BookClubResponse;


public interface BookclubDao {
    List<BookClubResponse> getBookClubForMember(String username);
    BookClub createBookclub(BookClub bookClub);
    void addMember(Long bookclubId, Long userId, String role);
    BookClubDetailResponse getClubDetails(Long clubId, String username);
    boolean bookclubExists(Long clubId);
    List<MemberEvent> getUnsentMembers();
    void markMemberSent(Long userId, Long bookclubId);
    int removeMember(Long clubId, Long userId);
    List<MemberEvent> getUnsentDepartures();
    void markDepartureSent(Long userId, Long bookclubId);
}
