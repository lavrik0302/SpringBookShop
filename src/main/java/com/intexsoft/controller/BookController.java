package com.intexsoft.controller;

import com.intexsoft.controller.dao.BookDAO;
import com.intexsoft.controller.dao.request.findRequest.FindBookRequest;
import com.intexsoft.model.Book;
import com.intexsoft.model.transfer.BookDTO;
import com.intexsoft.model.transfer.CreateBookDTO;
import com.intexsoft.model.transfer.DeleteDTO;
import com.intexsoft.parser.JsonDeserializer;
import com.intexsoft.parser.Mapper;
import com.intexsoft.serializer.JsonSerializer;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.UUID;

@Controller
@EnableWebMvc
@ComponentScan("com.intexsoft")
@RequestMapping("/book")
public class BookController {
    @Autowired
    BookDAO bookDAO;
    Mapper mapper = new Mapper();
    JsonSerializer jsonSerializer = new JsonSerializer();

    @GetMapping
    public void readBook(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws IOException {
        String stringUUId = request.getParameter("uuid");

        PrintWriter pw = response.getWriter();
        try {
            UUID uuid = UUID.fromString(stringUUId);
            Book book = bookDAO.find(new FindBookRequest().setBookId(uuid)).get(0);
            BookDTO bookDTO = new BookDTO();
            bookDTO.setBookId(book.getBookId().toString());
            bookDTO.setBookname(book.getBookname());
            bookDTO.setAuthor(book.getAuthor());
            bookDTO.setCostInByn(book.getCostInByn());
            bookDTO.setCountInStock(book.getCountInStock());
            String json = jsonSerializer.serialize(bookDTO);
            pw.println("<html>");
            pw.println("<h1> book_id = " + book.getBookId() + "</h1>");
            pw.println("<h1> bookname = " + book.getBookname() + "</h1>");
            pw.println("<h1> author = " + book.getAuthor() + "</h1>");
            pw.println("<h1> cost_in_byn = " + book.getCostInByn() + "</h1>");
            pw.println("<h1> count_in_stock = " + book.getCountInStock() + "</h1>");
            pw.println("<h1> As JSON = " + json + "</h1>");
            pw.println("</html>");
        } catch (IllegalArgumentException | NullPointerException e) {
            pw.println("<html>");
            pw.println("<h1> Invalid UUID </h1>");
            pw.println("<h1>" + e + "</h1>");
            response.setStatus(400);
            pw.println("</html>");
        } catch (IndexOutOfBoundsException e) {
            pw.println("<html>");
            pw.println("<h1> No such bookId </h1>");
            pw.println("<h1>" + e + "</h1>");
            e.printStackTrace(pw);
            response.setStatus(404);
            pw.println("</html>");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @PostMapping
    public void createBook(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws IOException {
        ServletInputStream servletInputStream = request.getInputStream();
        PrintWriter pw = response.getWriter();

        StringBuilder sb = new StringBuilder();
        while (!servletInputStream.isFinished()) {
            sb.append(Character.valueOf((char) servletInputStream.read()));
        }
        try {
            JsonDeserializer jsonDeserializer = new JsonDeserializer(sb.toString());
            CreateBookDTO createBookDTO = mapper.map(jsonDeserializer.parseValue(), CreateBookDTO.class);
            pw.println("<html>");
            Book book = bookDAO.createRow(createBookDTO.getBookname(), createBookDTO.getAuthor(), createBookDTO.getCostInByn(), createBookDTO.getCountInStock());
            pw.println("<h1> bookid = " + book.getBookId() + "</h1>");
            pw.println("<h1> bookname = " + book.getBookname() + "</h1>");
            pw.println("<h1> author = " + book.getAuthor() + "</h1>");
            pw.println("<h1> costInByn = " + book.getCostInByn() + "</h1>");
            pw.println("<h1> countInStock = " + book.getCountInStock() + "</h1>");
            BookDTO bookDTO = new BookDTO();
            bookDTO.setAuthor(book.getAuthor());
            bookDTO.setBookname(book.getBookname());
            bookDTO.setCountInStock(book.getCountInStock());
            bookDTO.setCostInByn(book.getCostInByn());
            bookDTO.setBookId(book.getBookId().toString());
            pw.println("<h1> As JSON = " + jsonSerializer.serialize(bookDTO) + "</h1>");
            pw.println("</html>");
        } catch (Exception e) {
            pw.println("<html>");
            pw.println("<h1> Wrong JSON</h1>");
            pw.println("<h1>" + e + "</h1>");
            response.setStatus(400);
            pw.println("</html>");
        }
    }

    @RequestMapping(method = RequestMethod.PUT)

    public void updateBook(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws IOException {
        ServletInputStream servletInputStream = request.getInputStream();
        PrintWriter pw = response.getWriter();
        StringBuilder sb = new StringBuilder();
        while (!servletInputStream.isFinished()) {
            sb.append(Character.valueOf((char) servletInputStream.read()));
        }
        try {
            JsonDeserializer jsonDeserializer = new JsonDeserializer(sb.toString());
            BookDTO bookDTO = mapper.map(jsonDeserializer.parseValue(), BookDTO.class);
            Book book = new Book();
            pw.println("<html>");
            book.setBookId(UUID.fromString(bookDTO.getBookId()));
            pw.println("<h1> bookId = " + book.getBookId() + "</h1>");
            book.setBookname(bookDTO.getBookname());
            pw.println("<h1> bookname = " + book.getBookname() + "</h1>");
            book.setAuthor(bookDTO.getAuthor());
            pw.println("<h1> author = " + book.getAuthor() + "</h1>");
            book.setCostInByn(bookDTO.getCostInByn());
            pw.println("<h1> costInByn = " + book.getCostInByn() + "</h1>");
            book.setCountInStock(bookDTO.getCountInStock());
            pw.println("<h1> countInStock = " + book.getCountInStock() + "</h1>");
            bookDAO.updateBook(book);
            pw.println("<h1> As JSON = " + jsonSerializer.serialize(bookDTO) + "</h1>");
            pw.println("</html>");
        } catch (Exception e) {
            pw.println("<html>");
            pw.println("<h1> Wrong JSON</h1>");
            pw.println("<h1>" + e + "</h1>");
            response.setStatus(400);
            pw.println("</html>");
        }
    }

    @RequestMapping(method = RequestMethod.PATCH)
    public void patchBook(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws IOException {
        updateBook(request, response);
    }

    @RequestMapping(method = RequestMethod.DELETE)
    public void deleteBook(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws IOException {
        ServletInputStream servletInputStream = request.getInputStream();
        PrintWriter pw = response.getWriter();
        StringBuilder sb = new StringBuilder();
        while (!servletInputStream.isFinished()) {
            sb.append(Character.valueOf((char) servletInputStream.read()));
        }
        try {
            JsonDeserializer jsonDeserializer = new JsonDeserializer(sb.toString());
            DeleteDTO deleteDTO = mapper.map(jsonDeserializer.parseValue(), DeleteDTO.class);
            Book book = bookDAO.find(new FindBookRequest().setBookId(UUID.fromString(deleteDTO.getUuid()))).get(0);
            pw.println("<html>");
            BookDTO bookDTO = new BookDTO();
            bookDTO.setBookId(book.getBookId().toString());
            pw.println("<h1> bookId = " + book.getBookId() + "</h1>");
            bookDTO.setBookname(book.getBookname());
            pw.println("<h1> bookname = " + book.getBookname() + "</h1>");
            bookDTO.setAuthor(book.getAuthor());
            pw.println("<h1> author = " + book.getAuthor() + "</h1>");
            bookDTO.setCostInByn(book.getCostInByn());
            pw.println("<h1> costInByn = " + book.getCostInByn() + "</h1>");
            bookDTO.setCountInStock(book.getCountInStock());
            pw.println("<h1> countInStock = " + book.getCountInStock() + "</h1>");
            bookDAO.delete(new FindBookRequest().setBookId(book.getBookId()));
            pw.println("<h1> As JSON = " + jsonSerializer.serialize(bookDTO) + "</h1>");
            pw.println("</html>");

        } catch (Exception e) {
            pw.println("<html>");
            pw.println("<h1> Wrong JSON</h1>");
            pw.println("<h1>" + e + "</h1>");
            response.setStatus(400);
            pw.println("</html>");
        }
    }
}
