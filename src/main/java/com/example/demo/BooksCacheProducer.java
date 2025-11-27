package com.example.demo;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;

@Component
public class BooksCacheProducer {

    private final RemoteCacheManager cacheManager;
    private RemoteCache<String, Book> booksCache;

    public BooksCacheProducer(RemoteCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Scheduled(fixedRate = 1000) // ogni 1 secondo
    public void pushBook() {
        try {
            if (booksCache == null) {
                booksCache = cacheManager.getCache("books");
                if (booksCache == null) {
                    System.err.println("Cache 'books' does not exist yet in Data Grid!");
                    return;
                }
            }

            Author author = new Author("Isaac", "Asimov");

            Book book = new Book(
                    UUID.randomUUID().toString(),
                    "Foundation",
                    "Classic Sci-Fi Novel",
                    1951,
                    Collections.singletonList(author),
                    Language.ENGLISH
            );

            booksCache.put(book.getId(), book);

            System.out.println("Added book to cache: " + book.getId());

        } catch (Exception e) {
            System.err.println("Error writing to cache: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
