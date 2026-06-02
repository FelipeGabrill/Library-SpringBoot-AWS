package com.library.models.entities;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cascade;

@Entity
@Table(name = "tb_book")
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    @Column(nullable = false)
    private String title;

    @Getter
    @Setter
    @Column(nullable = false)
    private String author;

    @Getter
    @Setter
    private String publisher;

    @Getter
    @Setter
    @Column(name = "publication_year")
    private Integer publicationYear;

    @Getter
    @Setter
    @Column(unique = true)
    private String isbn;

    @Getter
    @Setter
    @Column(name = "total_copies", nullable = false)
    private Integer totalCopies;

    @Getter
    @Setter
    @Column(name = "available_copies", nullable = false)
    private Integer availableCopies;

    @ElementCollection
    @CollectionTable(
            name = "tb_book_media",
            joinColumns = @JoinColumn(name = "book_id")
    )
    @Getter
    @OrderBy("position ASC")
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    private List<Media> media = new ArrayList<>();

    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Getter
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Loan> loans = new ArrayList<>();

    @Getter
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reservation> reservations = new ArrayList<>();

    public Book() {
    }

    public boolean isAvailable() {
        return availableCopies > 0;
    }

    public void decrementAvailable() {
        if (!isAvailable()) throw new IllegalStateException("No copies available");
        this.availableCopies--;
    }

    public void incrementAvailable() {
        if (availableCopies >= totalCopies) throw new IllegalStateException("All copies already returned");
        this.availableCopies++;
    }
}