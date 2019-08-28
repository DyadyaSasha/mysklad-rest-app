package app.filters;

import app.helpers.HibernateHelper;
import org.hibernate.SessionFactory;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;

@WebFilter(urlPatterns = "/*")
public class ClearResourcesFilter implements Filter {


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        SessionFactory factory = HibernateHelper.getInstance().getFactory();
        if (factory != null && !factory.isClosed()) {
            factory.close();
        }
    }
}

