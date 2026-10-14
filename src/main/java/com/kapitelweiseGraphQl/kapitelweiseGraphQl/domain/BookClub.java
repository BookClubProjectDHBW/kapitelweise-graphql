package com.kapitelweiseGraphQl.kapitelweiseGraphQl.domain;

import java.time.LocalDateTime;
import java.util.Objects;

public class BookClub {

    private Long id;
    private String name;
    private String description;
    private LocalDateTime created_at;


    public BookClub(String bookClubName, String description){
        this.name = bookClubName;
        this.description = description;
    }

    public BookClub(Long id, String bookClubName, String description, LocalDateTime created_at){
        this.id = id;
        this.name = bookClubName;
        this.description = description;
        this.created_at=created_at;
    }



    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Long getId() {
        return id;
    }


    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setBookClubName(String bookClubName) {
        this.name = bookClubName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BookClub bookclub = (BookClub) o;
        return Objects.equals(name, bookclub.name) && Objects.equals(description, bookclub.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description);
    }
}