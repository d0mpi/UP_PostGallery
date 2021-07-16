package by.bsu.d0mpi.UP_PostGallery.servlet;

import by.bsu.d0mpi.UP_PostGallery.dao.impl.MySqlUserDao;
import by.bsu.d0mpi.UP_PostGallery.model.User;
import by.bsu.d0mpi.UP_PostGallery.service.UserService;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@WebServlet(name = "sign", value = "/sign")
public class SignInServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        RequestDispatcher requestDispatcher = req.getRequestDispatcher("/WEB-INF/views/sign.jsp");
        requestDispatcher.forward(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String respType;
        String login = req.getParameter("username");
        String password = req.getParameter("password");
        UserService userService = UserService.simple();
        User user;

        resp.setContentType("text/plain");
        resp.setCharacterEncoding("UTF-8");
        OutputStream outStream = resp.getOutputStream();

        if(!userService.isLoginPresented(login)) {
            respType = "No user with this login:( Try again.";
            outStream.write(respType.getBytes(StandardCharsets.UTF_8));

            outStream.flush();
            outStream.close();
            return;
        } else {
            user = new User(login,password);
            if (!userService.canLogIn(user)) {
                respType = "Wrong password:( Try again.";
                outStream.write(respType.getBytes(StandardCharsets.UTF_8));
                outStream.flush();
                outStream.close();
                return;
            }
        }
        req.getSession().setAttribute("logged", true);
        req.getSession().setAttribute("login", login);

        respType = "success";

        outStream.write(respType.getBytes(StandardCharsets.UTF_8));
        outStream.flush();
        outStream.close();

        resp.sendRedirect("/home");
    }
}
