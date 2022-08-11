package com.intexsoft.controller;

import com.intexsoft.controller.dao.BookDAO;
import com.intexsoft.controller.dao.CartHasBookDAO;
import com.intexsoft.controller.dao.request.findRequest.FindBookRequest;
import com.intexsoft.controller.dao.request.findRequest.FindCartHasBookRequest;
import com.intexsoft.controller.dao.request.updateRequest.UpdateCartHasBookRequest;
import com.intexsoft.model.Book;
import com.intexsoft.model.CartHasBook;
import com.intexsoft.model.transfer.CartHasBookDTO;
import com.intexsoft.model.transfer.DeleteBookFromCartDTO;
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
import java.util.List;
import java.util.UUID;

@Controller
@EnableWebMvc
@ComponentScan("com.intexsoft")
@RequestMapping("bookInCart")
public class BookInCartController {
    @Autowired
    CartHasBookDAO cartHasBookDAO;
    @Autowired
    BookDAO bookDAO;
    JsonSerializer jsonSerializer = new JsonSerializer();
    Mapper mapper = new Mapper();

    @GetMapping
    public void readCart(@NotNull HttpServletRequest request,
                         @NotNull HttpServletResponse response
    ) throws IOException {
        String stringUUId = request.getParameter("uuid");
        PrintWriter pw = response.getWriter();

        try {
            UUID uuid = UUID.fromString(stringUUId);
            List<CartHasBook> list = cartHasBookDAO.find(new FindCartHasBookRequest().setCartId(uuid));
            CartHasBookDTO[] cartHasBookDTOarr = new CartHasBookDTO[list.size()];
            pw.println("<html>");
            int cartHasBookCounter = 0;
            for (CartHasBook cur : list) {
                CartHasBookDTO cartHasBookDTO = new CartHasBookDTO();
                cartHasBookDTO.setCartId(cur.getCartId().toString());
                cartHasBookDTO.setBookId(cur.getBookId().toString());
                cartHasBookDTO.setBookCount(cur.getBookCount());
                cartHasBookDTOarr[cartHasBookCounter] = cartHasBookDTO;
                pw.println("<h1> bookId = " + cur.getBookId() + "</h1>");
                Book book = bookDAO.find(new FindBookRequest().setBookId(cur.getBookId())).get(0);
                pw.println("<h1> bookname = " + book.getBookname() + "</h1>");
                pw.println("<h1> author = " + book.getAuthor() + "</h1>");
                pw.println("<h1> costInByn = " + book.getCostInByn() + "</h1>");
                pw.println("<h1> bookCount = " + cur.getBookCount() + "</h1>");
                pw.println("<h1> -----------------------</h1>");
                cartHasBookCounter++;
            }
            pw.println("<h1> As JSON= " + jsonSerializer.serialize(cartHasBookDTOarr) + "</h1>");
            pw.println("</html>");
        } catch (IllegalArgumentException | NullPointerException e) {
            pw.println("<html>");
            pw.println("<h1> Invalid UUID </h1>");
            pw.println("<h1>" + e + "</h1>");
            response.setStatus(400);
            pw.println("</html>");
        } catch (IndexOutOfBoundsException e) {
            pw.println("<html>");
            pw.println("<h1> No such cart an book UUIDs </h1>");
            pw.println("<h1>" + e + "</h1>");
            response.setStatus(404);
            pw.println("</html>");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping
    public void addBookToCart(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws IOException {
        ServletInputStream servletInputStream = request.getInputStream();
        PrintWriter pw = response.getWriter();

        StringBuilder sb = new StringBuilder();
        while (!servletInputStream.isFinished()) {
            sb.append(Character.valueOf((char) servletInputStream.read()));
        }
        try {
            JsonDeserializer jsonDeserializer = new JsonDeserializer(sb.toString());
            CartHasBookDTO cartHasBookDTO = mapper.map(jsonDeserializer.parseValue(), CartHasBookDTO.class);
            cartHasBookDAO.createRow(UUID.fromString(cartHasBookDTO.getCartId()), UUID.fromString(cartHasBookDTO.getBookId()), cartHasBookDTO.getBookCount());
            pw.println("<html>");
            pw.println("<h1> cartId = " + cartHasBookDTO.getCartId() + "</h1>");
            pw.println("<h1> bookId = " + cartHasBookDTO.getBookId() + "</h1>");
            pw.println("<h1> bookCount = " + cartHasBookDTO.getBookCount() + "</h1>");
            pw.println("<h1> As JSON = " + jsonSerializer.serialize(cartHasBookDTO) + "</h1>");
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
    public void updateBookCount(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws IOException {
        ServletInputStream servletInputStream = request.getInputStream();
        PrintWriter pw = response.getWriter();

        StringBuilder sb = new StringBuilder();
        while (!servletInputStream.isFinished()) {
            sb.append(Character.valueOf((char) servletInputStream.read()));
        }
        try {
            JsonDeserializer jsonDeserializer = new JsonDeserializer(sb.toString());
            CartHasBookDTO cartHasBookDTO = mapper.map(jsonDeserializer.parseValue(), CartHasBookDTO.class);
            cartHasBookDAO.update(new UpdateCartHasBookRequest()
                            .setCartId(UUID.fromString(cartHasBookDTO.getCartId()))
                            .setBookId(UUID.fromString(cartHasBookDTO.getBookId()))
                            .setBookCount(cartHasBookDTO.getBookCount())
                    , new FindCartHasBookRequest()
                            .setCartId(UUID.fromString(cartHasBookDTO.getCartId()))
                            .setBookId(UUID.fromString(cartHasBookDTO.getBookId())));
            pw.println("<html>");
            pw.println("<h1> cartId = " + cartHasBookDTO.getCartId() + "</h1>");
            pw.println("<h1> bookId = " + cartHasBookDTO.getBookId() + "</h1>");
            pw.println("<h1> bookCount = " + cartHasBookDTO.getBookCount() + "</h1>");
            pw.println("<h1> As JSON = " + jsonSerializer.serialize(cartHasBookDTO) + "</h1>");
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
    public void patchBookCount(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws IOException {
        updateBookCount(request, response);

    }

    @RequestMapping(method = RequestMethod.DELETE)
    public void deleteBookFromCart(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws IOException {
        ServletInputStream servletInputStream = request.getInputStream();
        PrintWriter pw = response.getWriter();

        StringBuilder sb = new StringBuilder();
        while (!servletInputStream.isFinished()) {
            sb.append(Character.valueOf((char) servletInputStream.read()));
        }
        try {
            JsonDeserializer jsonDeserializer = new JsonDeserializer(sb.toString());
            DeleteBookFromCartDTO deleteBookFromCartDTO = mapper.map(jsonDeserializer.parseValue(), DeleteBookFromCartDTO.class);
            CartHasBook cartHasBook = cartHasBookDAO
                    .find(new FindCartHasBookRequest()
                            .setCartId(UUID.fromString(deleteBookFromCartDTO.getCartId()))
                            .setBookId(UUID.fromString(deleteBookFromCartDTO.getBookId())))
                    .get(0);
            CartHasBookDTO cartHasBookDTO = new CartHasBookDTO();
            cartHasBookDTO.setCartId(cartHasBook.getCartId().toString());
            cartHasBookDTO.setBookId(cartHasBook.getBookId().toString());
            cartHasBookDTO.setBookCount(cartHasBook.getBookCount());
            pw.println("<html>");
            pw.println("<h1> cartId = " + cartHasBookDTO.getCartId() + "</h1>");
            pw.println("<h1> bookId = " + cartHasBookDTO.getBookId() + "</h1>");
            pw.println("<h1> bookCount = " + cartHasBookDTO.getBookCount() + "</h1>");
            pw.println("<h1> As JSON = " + jsonSerializer.serialize(cartHasBookDTO) + "</h1>");
            pw.println("</html>");
            cartHasBookDAO.delete(new FindCartHasBookRequest().setCartId(cartHasBook.getCartId()).setBookId(cartHasBook.getBookId()));
        } catch (Exception e) {
            pw.println("<html>");
            pw.println("<h1> Wrong JSON</h1>");
            pw.println("<h1>" + e + "</h1>");
            response.setStatus(400);
            pw.println("</html>");
        }
    }
}
