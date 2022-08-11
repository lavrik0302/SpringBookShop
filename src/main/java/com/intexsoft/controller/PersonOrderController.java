package com.intexsoft.controller;

import com.intexsoft.controller.dao.*;
import com.intexsoft.controller.dao.request.findRequest.*;
import com.intexsoft.controller.dao.request.updateRequest.UpdatePersonOrderRequest;
import com.intexsoft.model.*;
import com.intexsoft.model.transfer.*;
import com.intexsoft.parser.JsonDeserializer;
import com.intexsoft.parser.Mapper;
import com.intexsoft.serializer.JsonSerializer;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.UUID;

@Controller
@EnableWebMvc
@ComponentScan("com.intexsoft")
@RequestMapping("/personOrder")
public class PersonOrderController {
    @Autowired
    PersonOrderDAO personOrderDAO;
    @Autowired
    PersonOrderHasBookDAO personOrderHasBookDAO;
    @Autowired
    CartDAO cartDAO;
    @Autowired
    CartHasBookDAO cartHasBookDAO;
    @Autowired
    BookDAO bookDAO;
    JsonSerializer jsonSerializer = new JsonSerializer();
    Mapper mapper = new Mapper();

    @GetMapping
    public void readPersonOrder(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws IOException {
        String stringUUId = request.getParameter("uuid");
        PrintWriter pw = response.getWriter();

        try {
            UUID uuid = UUID.fromString(stringUUId);
            PersonOrder personOrder = personOrderDAO.findWithBooks(new FindPersonOrderRequest().setOrderId(uuid));
            PersonOrderDTO personOrderDTO = new PersonOrderDTO();
            pw.println("<html>");
            pw.println("<h1> orderId = " + personOrder.getOrderId() + "</h1>");
            personOrderDTO.setOrderId(personOrder.getOrderId().toString());
            pw.println("<h1> personId = " + personOrder.getPersonId() + "</h1>");
            personOrderDTO.setPersonId(personOrder.getPersonId().toString());
            pw.println("<h1> adress = " + personOrder.getAdress() + "</h1>");
            personOrderDTO.setAdress(personOrder.getAdress());
            pw.println("<h1> status = " + personOrder.getStringStatusId() + "</h1>");
            personOrderDTO.setStatusId(personOrder.getStatusId());
            pw.println("<h1> books </h1>");
            List<PersonOrderHasBook> list = personOrderHasBookDAO.find(new FindPersonOrderHasBookRequest().setOrderId(personOrder.getOrderId()));
            PersonOrderHasBookDTO[] bookArr = new PersonOrderHasBookDTO[list.size()];
            int bookCounter = 0;
            for (PersonOrderHasBook personOrderHasBook : list) {
                PersonOrderHasBookDTO personOrderHasBookDTO = new PersonOrderHasBookDTO();
                pw.println("<h1> bookId = " + personOrderHasBook.getBookId() + "</h1>");
                personOrderHasBookDTO.setBookId(personOrderHasBook.getBookId().toString());
                pw.println("<h1> bookCount = " + personOrderHasBook.getBookCount() + "</h1>");
                personOrderHasBookDTO.setBookCount(personOrderHasBook.getBookCount());
                personOrderHasBookDTO.setOrderId(personOrder.getOrderId().toString());
                bookArr[bookCounter] = personOrderHasBookDTO;
                bookCounter++;
                pw.println("<h1> -----------------------</h1>");
            }
            personOrderDTO.setBooks(bookArr);
            pw.println("<h1> As JSON = " + jsonSerializer.serialize(personOrderDTO) + "</h1>");
            pw.println("</html>");
        } catch (IllegalArgumentException | NullPointerException e) {
            pw.println("<html>");
            pw.println("<h1> Invalid UUID </h1>");
            pw.println("<h1>" + e + "</h1>");
            response.setStatus(400);
            pw.println("</html>");
        } catch (IndexOutOfBoundsException e) {
            pw.println("<html>");
            pw.println("<h1> No such cart and book UUIDs </h1>");
            pw.println("<h1>" + e + "</h1>");
            response.setStatus(404);
            pw.println("</html>");
        }
    }

    @PostMapping
    public void createPersonOrderFromPersonsCart(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws IOException {
        ServletInputStream servletInputStream = request.getInputStream();
        PrintWriter pw = response.getWriter();
        StringBuilder sb = new StringBuilder();
        while (!servletInputStream.isFinished()) {
            sb.append(Character.valueOf((char) servletInputStream.read()));
        }
        try {
            JsonDeserializer jsonDeserializer = new JsonDeserializer(sb.toString());
            CreatePersonOrderDTO createPersonOrderDTO = mapper.map(jsonDeserializer.parseValue(), CreatePersonOrderDTO.class);
            UUID uuid = UUID.randomUUID();
            Cart finding = cartDAO.find(new FindCartRequest().setCartPersonId(UUID.fromString(createPersonOrderDTO.getPersonId())));

            List<CartHasBook> check = cartHasBookDAO.find(new FindCartHasBookRequest().setCartId(finding.getCartId()));
            boolean availableStatus = true;
            for (CartHasBook cartHasBook : check) {
                if (cartHasBook.getBookCount() > bookDAO.find(new FindBookRequest().setBookId(cartHasBook.getBookId())).get(0).getCountInStock()) {
                    availableStatus = false;
                }
            }
            if (availableStatus) {
                PersonOrder personOrder = new PersonOrder();
                personOrder.setOrderId(uuid);
                personOrder.setPersonId(UUID.fromString(createPersonOrderDTO.getPersonId()));
                personOrder.setAdress(createPersonOrderDTO.getAdress());
                personOrder.setStatusId(createPersonOrderDTO.getStatusId());
                personOrderDAO.createPersonOrder(personOrder);
                Cart cart = cartDAO.findWithBooks(new FindCartRequest().setCartPersonId(personOrder.getPersonId()));
                List<CartHasBook> listOfBooks = cartHasBookDAO.find(new FindCartHasBookRequest().setCartId(cart.getCartId()));
                for (CartHasBook cartHasBook : listOfBooks) {
                    personOrderHasBookDAO.createRow(uuid, cartHasBook.getBookId(), cartHasBook.getBookCount());
                    cartHasBookDAO.delete(new FindCartHasBookRequest().setCartId(cart.getCartId()).setBookId(cartHasBook.getBookId()));
                    Book book = bookDAO.find(new FindBookRequest().setBookId(cartHasBook.getBookId())).get(0);
                    book.setCountInStock(book.getCountInStock() - cartHasBook.getBookCount());
                    bookDAO.updateBook(book);
                }
                PersonOrderDTO personOrderDTO = new PersonOrderDTO();
                pw.println("<html>");
                pw.println("<h1> orderId = " + personOrder.getOrderId() + "</h1>");
                personOrderDTO.setOrderId(personOrder.getOrderId().toString());
                pw.println("<h1> personId = " + personOrder.getPersonId() + "</h1>");
                personOrderDTO.setPersonId(personOrder.getPersonId().toString());
                pw.println("<h1> adress = " + personOrder.getAdress() + "</h1>");
                personOrderDTO.setAdress(personOrder.getAdress());
                pw.println("<h1> status = " + personOrder.getStringStatusId() + "</h1>");
                personOrderDTO.setStatusId(personOrder.getStatusId());
                pw.println("<h1> books </h1>");
                PersonOrderHasBookDTO[] bookArr = new PersonOrderHasBookDTO[listOfBooks.size()];
                int bookCounter = 0;
                for (CartHasBook book : listOfBooks) {
                    PersonOrderHasBookDTO temp = new PersonOrderHasBookDTO();
                    temp.setOrderId(personOrder.getOrderId().toString());
                    pw.println("<h1> bookId = " + book.getBookId() + "</h1>");
                    temp.setBookId(book.getBookId().toString());
                    pw.println("<h1> bookCount = " + book.getBookCount() + "</h1>");
                    temp.setBookCount(book.getBookCount());
                    bookArr[bookCounter] = temp;
                    bookCounter++;
                    pw.println("<h1>---------------------------</h1>");
                }
                personOrderDTO.setBooks(bookArr);
                pw.println("<h1> As JSON = " + jsonSerializer.serialize(personOrderDTO) + "</h1>");
                pw.println("</html>");
            } else {
                pw.println("<html>");
                pw.println("<h1> Can't crete new order. Please wait until we add books to stock</h1>");
                response.setStatus(412);
                pw.println("</html>");
            }
        } catch (Exception e) {
            pw.println("<html>");
            pw.println("<h1> Wrong JSON</h1>");
            pw.println("<h1>" + e + "</h1>");
            response.setStatus(400);
            pw.println("</html>");
        }
    }

    @RequestMapping(method = RequestMethod.PUT)
    public void updateOrderStatus(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws IOException {
        ServletInputStream servletInputStream = request.getInputStream();
        PrintWriter pw = response.getWriter();

        StringBuilder sb = new StringBuilder();
        while (!servletInputStream.isFinished()) {
            sb.append(Character.valueOf((char) servletInputStream.read()));
        }
        try {
            JsonDeserializer jsonDeserializer = new JsonDeserializer(sb.toString());
            UpdateOrderStatusDTO updateOrderStatusDTO = mapper.map(jsonDeserializer.parseValue(), UpdateOrderStatusDTO.class);
            personOrderDAO.update(new UpdatePersonOrderRequest()
                            .setStatusId(updateOrderStatusDTO.getStatusId())
                    , new FindPersonOrderRequest()
                            .setOrderId(UUID.fromString(updateOrderStatusDTO.getOrderId())));
            pw.println("<html>");
            pw.println("<h1> orderId = " + updateOrderStatusDTO.getOrderId() + "</h1>");
            pw.println("<h1> statusId = " + updateOrderStatusDTO.getStatusId() + "</h1>");
            pw.println("<h1> As JSON = " + jsonSerializer.serialize(updateOrderStatusDTO) + "</h1>");
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
    public void patchOrderStatus(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws IOException {
        updateOrderStatus(request, response);
    }

    @RequestMapping(method = RequestMethod.DELETE)
    public void deleteOrder(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws IOException {
        ServletInputStream servletInputStream = request.getInputStream();
        PrintWriter pw = response.getWriter();

        StringBuilder sb = new StringBuilder();
        while (!servletInputStream.isFinished()) {
            sb.append(Character.valueOf((char) servletInputStream.read()));
        }
        try {
            JsonDeserializer jsonDeserializer = new JsonDeserializer(sb.toString());
            DeleteDTO deleteDTO = mapper.map(jsonDeserializer.parseValue(), DeleteDTO.class);
            PersonOrder personOrder = personOrderDAO.find(new FindPersonOrderRequest().setOrderId(UUID.fromString(deleteDTO.getUuid()))).get(0);
            CreatePersonOrderDTO createPersonOrderDTO = new CreatePersonOrderDTO();
            pw.println("<html>");
            pw.println("<h1> orderId = " + personOrder.getOrderId() + "</h1>");
            pw.println("<h1> personId = " + personOrder.getPersonId() + "</h1>");
            createPersonOrderDTO.setPersonId(personOrder.getPersonId().toString());
            pw.println("<h1> adress = " + personOrder.getAdress() + "</h1>");
            createPersonOrderDTO.setAdress(personOrder.getAdress());
            pw.println("<h1> statusId = " + personOrder.getStatusId() + "</h1>");
            createPersonOrderDTO.setStatusId(personOrder.getStatusId());
            pw.println("<h1> As JSON = " + jsonSerializer.serialize(createPersonOrderDTO) + "</h1>");
            pw.println("</html>");
            personOrderDAO.delete(new FindPersonOrderRequest().setOrderId(personOrder.getOrderId()));
        } catch (
                Exception e) {
            pw.println("<html>");
            pw.println("<h1> Wrong JSON</h1>");
            pw.println("<h1>" + e + "</h1>");
            response.setStatus(400);
            pw.println("</html>");
        }
    }
}
