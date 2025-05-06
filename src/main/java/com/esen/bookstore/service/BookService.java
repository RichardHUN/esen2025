package com.esen.bookstore.service;

import com.esen.bookstore.model.Book;
import com.esen.bookstore.model.Bookstore;
import com.esen.bookstore.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final BookstoreService bookstoreService;

    public void save(Book book){
        bookRepository.save(book);
    }

    public List<Book> findAll(){
        return bookRepository.findAll();
    }

    public void delete(Long id){
        if(!bookRepository.existsById(id)){
            throw new IllegalArgumentException("Cannot find book with id " + id);
        }

        var book = bookRepository.findById(id).get();
        bookstoreService.removeBookFromInventories(book);
        bookRepository.delete(book);
    }


    public Book update(Long id, String title, String author, String publisher, Double price){
        if( Stream.of(title, author, publisher, price).allMatch(Objects::isNull) ){
            throw new IllegalArgumentException("At least one input is required");
        }

        if(!bookRepository.existsById(id)){
            throw new IllegalArgumentException("Cannot find book with id " + id);
        }

        var book = bookRepository.findById(id).get();

        if (title != null)book.setTitle(title);
        if (author != null)book.setAuthor(author);
        if (price != null)book.setPrice(price);
        if (publisher != null)book.setPublisher(publisher);

        return bookRepository.save(book);
    }

    public Map<String, Double> findPrices(long id){
        if(!bookRepository.existsById(id)){
            throw new IllegalArgumentException("Cannot find book with id " + id);
        }

        Double bookPrice = bookRepository.findById(id).get().getPrice();
        return bookstoreService.findAll()
                .stream()
                .map(bookstore -> Pair.of(bookstore.getLocation(), bookstore.getPriceModifier()))
                .collect(Collectors.toMap(Pair::getFirst, p -> p.getSecond() * bookPrice));

        /*Double priceOfBook = bookRepository.findById(id).get().getPrice();
        List<Bookstore> bookstores = bookstoreService.findAll();
        Map<String, Double> pricesByBookstores = new HashMap<>();

        for (Bookstore bookstore: bookstores){
            pricesByBookstores.put(bookstore.getLocation(), bookstore.getPriceModifier() * priceOfBook);
        }
        return pricesByBookstores;*/
    }

    public List<Book> findByPublisherOrAuthorOrTitle(String publisher, String author, String title){
        return bookRepository.findAll()
                .stream()
                .filter(book -> {
                    if(title != null){
                        return Objects.equals(book.getTitle(), title);
                    }if (publisher != null){
                        return Objects.equals(book.getPublisher(), publisher);
                    }if (author != null){
                        return Objects.equals(author, book.getAuthor());
                    }
                    return true;
                })
                .toList();
    }

}
