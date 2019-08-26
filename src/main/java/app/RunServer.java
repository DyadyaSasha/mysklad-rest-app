package app;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;


/**
 * https://github.com/edomingues/jersey2-jetty-example/blob/master/rest-server/src/main/java/edomingues/restserver/Main.java
 * https://stackoverflow.com/questions/29524506/path-to-static-content-with-embedded-jetty
 */
public class RunServer {

    public static void main(String[] args) throws Exception {

        URL resource = RunServer.class.getResource("/swagger-ui");
        File swaggerStaticFilesDir = Paths.get(resource.toURI()).toFile();

//        ResourceConfig config = new RestConfig();
//        config.packages("io.swagger.jaxrs.listing", "app.api.rest");
//        config.register(JacksonFeature.class);
        ServletContainer servletContainer = new ServletContainer(new RestConfig());
        ServletHolder servletHolder = new ServletHolder(servletContainer);

        ServletHolder staticFilesHolder = new ServletHolder(new DefaultServlet());
        staticFilesHolder.setInitParameter("resourceBase", swaggerStaticFilesDir.getAbsolutePath());
        staticFilesHolder.setInitParameter("dirAllowed", "true");
        staticFilesHolder.setInitParameter("pathInfoOnly", "true");


        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.addServlet(servletHolder, "/app/*");
        context.addServlet(staticFilesHolder, "/swagger-ui/*");

        Server server = new Server(8080);
        server.setHandler(context);

        try {
            server.start();
            server.join();
        } finally {
            server.destroy();
        }
    }


}
