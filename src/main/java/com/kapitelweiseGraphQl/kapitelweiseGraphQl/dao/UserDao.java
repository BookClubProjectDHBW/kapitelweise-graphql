package com.kapitelweiseGraphQl.kapitelweiseGraphQl.dao;
import java.util.Optional;

//unser Interface für das UserDAO, ist ein Pattern das quasi eine Zwischenschicht zwischen Service und Persistence darstellt
// so könnten mehrere Services dieses Zeug nutzen, und wir könnten die Implementierung austauschen, ohne dass die Services was davon merken
// Die Tabelle users gehört dem Auth-Service (Registrierung, Login), das Backend hält in club_user eine Kopie, die über die Queue aktualisiert wird.

public interface UserDao {
    Optional<Long> getUserId(String username);
    void upsertUser(Long id, String username);

}
