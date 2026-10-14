package com.kapitelweiseGraphQl.kapitelweiseGraphQl.service;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;

// erzeugt werden die Tokens im Auth-Service, hier wird nur noch geprüft. Der SECRET_KEY muss in beiden Services gleich sein!


@Service
public class JwtService {

    private static final String SECRET_KEY = "mysecretkeymysecretkeymyxwsecretkeymysecretkey"; 
    // muss so lange sein, sonst ist es bei mir auf Fehler gelaufen, später vllt. einen neuen erzeugen lassen
    //bzw. eigentlich Umgebungsvariable, aber für Testzwecke erstmal so
    

    private Key getSignedKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }


/*
prüft den Token ob er valide ist wenn er mitgeschickt wird.
Dabei wird ein Parser gebaut, der den Token zerlegt. in setSigning geben wir ihm den selben geheimen Key mit, build baut den Parser
dann: 1. in drei Teile zerlegt, Signatur wird nachgerechnet, Ablaufdatum wird geprüft
getBody gibt den Body also die Claims zurück, die wir oben definiert haben.
 */
    public Claims verifyTokenAndExtract(String token){
        return Jwts.parserBuilder()
        .setSigningKey(getSignedKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
    }

}