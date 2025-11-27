package com.example.demo;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

import java.util.ArrayList;
import java.util.List;

public class Book {
    @ProtoField(number = 1)
    public String id;

    @ProtoField(number = 2)
    final String title;

    @ProtoField(number = 3)
    final String description;

    @ProtoField(number = 4, defaultValue = "0")
    final int publicationYear;

    @ProtoField(number = 5, collectionImplementation = ArrayList.class)
    final List<Author> authors;

    @ProtoField(number = 6)
    public Language language;

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getPublicationYear() {
        return publicationYear;
    }

    public List<Author> getAuthors() {
        return authors;
    }

    public Language getLanguage() {
        return language;
    }

    @ProtoFactory
    Book(String id, String title, String description, int publicationYear, List<Author> authors, Language language) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.publicationYear = publicationYear;
        this.authors = authors;
        this.language = language;
    }
}