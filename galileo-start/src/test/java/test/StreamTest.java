package test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.awt.print.Book;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xiaobo
 * @date 2023/12/2 14:02
 */
public class StreamTest {


    public static void main(String[] args) {
        System.out.println("---------->存储的图书信息: ");
        System.out.println(initInfo());
        System.out.println("---------->测试map方法:");
        testMap();
        System.out.println("---------->测试flatMap方法:");
        testFlatMap();

        System.out.println("testFlatMa2p");
        testFlatMa2p();
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Library {
        private String name;
        private List<Book> book;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Book {
        private String name;
        private String author;
        private Integer price;
    }


    private static void testMap() {
        initInfo().stream()
                .map(library -> library.getBook())
                .forEach(book -> System.out.println(book.getClass() + ": " + book));
    }

    private static void testFlatMap() {
        initInfo().stream()
                .flatMap(library -> library.getBook().stream())
                .forEach(book -> System.out.println(book));
    }


    private static void testFlatMa2p() {
        List<Book> books =
                initInfo().stream()
                        .flatMap(library -> library.getBook().stream())
                        .map(e -> e).collect(Collectors.toList());
        System.out.println(books);


        books = initInfo().stream()
                        .flatMap(library -> library.getBook().stream()).collect(Collectors.toList());

        System.out.println(books);
    }

    public static List<Library> initInfo() {
        Library library1 = new Library("新华图书", null);
        Library library2 = new Library("大家图书", null);
        Library library3 = new Library("瀚海图书", null);

        Book book1 = new Book("西游记", "吴承恩", 49);
        Book book2 = new Book("水浒传", "施耐庵", 57);
        Book book3 = new Book("三国演义", "罗贯中", 52);
        Book book4 = new Book("朝花夕拾", "鲁迅", 30);

        List<Book> library1Book = new ArrayList<>();
        List<Book> library2Book = new ArrayList<>();
        List<Book> library3Book = new ArrayList<>();

        library1Book.add(book1);
        library1Book.add(book2);

        library2Book.add(book2);
        library2Book.add(book3);

        library3Book.add(book3);
        library3Book.add(book4);

        library1.setBook(library1Book);
        library2.setBook(library2Book);
        library3.setBook(library3Book);

        return new ArrayList<>(Arrays.asList(library1, library2, library3));
    }
}
