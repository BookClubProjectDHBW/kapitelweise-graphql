package com.kapitelweiseGraphQl.kapitelweiseGraphQl.service;

import com.kapitelweiseGraphQl.kapitelweiseGraphQl.dao.BookclubDao;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.dao.UserDao;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.domain.BookClub;
import com.kapitelweiseGraphQl.kapitelweiseGraphQl.response.BookClubResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BookClubServiceTest {

    private BookclubDao bookclubDao;
    private UserDao userDao;
    private AmazonSQSService amazonSQSService;
    private BookClubService service;

    @BeforeEach
    void setUp() {
        bookclubDao = mock(BookclubDao.class);
        userDao = mock(UserDao.class);
        amazonSQSService = mock(AmazonSQSService.class);
        service = new BookClubService(bookclubDao, userDao, amazonSQSService);
    }

    private void prepareSuccessfulCreation() {
        when(userDao.getUserId("fabrice")).thenReturn(Optional.of(7L));
        when(bookclubDao.createBookclub(any(BookClub.class)))
                .thenAnswer(invocation -> {
                    BookClub input = invocation.getArgument(0);
                    return new BookClub(12L, input.getName(), input.getDescription(), LocalDateTime.now());
                });
    }

    private void assertBadRequest(String name, String description) {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.createNewBookClub(name, description, "fabrice")
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verifyNoInteractions(bookclubDao, userDao, amazonSQSService);
    }

    @Test
    void nameMit25ZeichenWirdAkzeptiert() {
        prepareSuccessfulCreation();
        String name = "A".repeat(25);

        BookClubResponse result = service.createNewBookClub(name, "Beschreibung", "fabrice");

        assertEquals(name, result.name());
        assertEquals(12L, result.id());
        verify(bookclubDao).addMember(12L, 7L, "ADMIN");
    }

    @Test
    void nameMit26ZeichenWirdAbgelehnt() {
        assertBadRequest("A".repeat(26), "Beschreibung");
    }

    @Test
    void beschreibungMit250ZeichenWirdAkzeptiert() {
        prepareSuccessfulCreation();
        String description = "B".repeat(250);

        BookClubResponse result = service.createNewBookClub("Leserunde", description, "fabrice");

        assertEquals(description, result.description());
        verify(bookclubDao).addMember(12L, 7L, "ADMIN");
    }

    @Test
    void beschreibungMit251ZeichenWirdAbgelehnt() {
        assertBadRequest("Leserunde", "B".repeat(251));
    }

    @Test
    void leererNameWirdAbgelehnt() {
        assertBadRequest("", "Beschreibung");
    }

    @Test
    void nameNurAusLeerzeichenWirdAbgelehnt() {
        assertBadRequest("   ", "Beschreibung");
    }

    @Test
    void fehlenderNameWirdAbgelehnt() {
        assertBadRequest(null, "Beschreibung");
    }

    @Test
    void beschreibungIstOptional() {
        prepareSuccessfulCreation();

        BookClubResponse result = service.createNewBookClub("Leserunde", null, "fabrice");

        assertNull(result.description());
        verify(bookclubDao).addMember(12L, 7L, "ADMIN");
    }

    @Test
    void gruenderWirdAlsAdminZugeordnet() {
        prepareSuccessfulCreation();

        service.createNewBookClub("Leserunde", "Beschreibung", "fabrice");

        var order = inOrder(bookclubDao);
        order.verify(bookclubDao).createBookclub(any(BookClub.class));
        order.verify(bookclubDao).addMember(12L, 7L, "ADMIN");
        verify(amazonSQSService).sendNewMember("MEMBER_JOINED", 7L, "fabrice", 12L, "ADMIN");
    }

    @Test
    void unbekannterBenutzerKannKeinenClubGruenden() {
        when(userDao.getUserId("fabrice")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.createNewBookClub("Leserunde", null, "fabrice")
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(bookclubDao, never()).createBookclub(any(BookClub.class));
        verify(bookclubDao, never()).addMember(anyLong(), anyLong(), anyString());
        verifyNoInteractions(amazonSQSService);
    }

    @Test
    void fehlerBeimSpeichernFuehrtZuKeinerAdminZuordnung() {
        when(userDao.getUserId("fabrice")).thenReturn(Optional.of(7L));
        when(bookclubDao.createBookclub(any(BookClub.class)))
                .thenThrow(new IllegalStateException("Datenbankfehler"));

        assertThrows(
                IllegalStateException.class,
                () -> service.createNewBookClub("Leserunde", null, "fabrice")
        );

        verify(bookclubDao, never()).addMember(anyLong(), anyLong(), anyString());
        verifyNoInteractions(amazonSQSService);
    }
}
