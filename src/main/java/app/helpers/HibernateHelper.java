package app.helpers;

import app.RunServer;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;

import javax.persistence.Entity;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

public class HibernateHelper {

    private SessionFactory factory;

    public SessionFactory getFactory() {
        return factory;
    }

    private static volatile HibernateHelper instance;

    private HibernateHelper() {

        Properties hibernateConnectionProperties = new Properties();
        hibernateConnectionProperties.put(Environment.DIALECT, Constants.DIALECT);
        hibernateConnectionProperties.put(Environment.DRIVER, Constants.DRIVER);
        hibernateConnectionProperties.put(Environment.URL, Constants.URL);
        hibernateConnectionProperties.put(Environment.USER, Constants.USER);
        hibernateConnectionProperties.put(Environment.PASS, Constants.PASS);
        hibernateConnectionProperties.put(Environment.CACHE_REGION_FACTORY, Constants.CACHE_REGION_FACTORY);
        hibernateConnectionProperties.put(Environment.USE_SECOND_LEVEL_CACHE, Constants.USE_SECOND_LEVEL_CACHE);
        hibernateConnectionProperties.put(Environment.USE_QUERY_CACHE, Constants.USE_QUERY_CACHE);
        /**
         * FOR DEBUG:
         */
        hibernateConnectionProperties.put(Environment.SHOW_SQL, Constants.SHOW_SQL);
        hibernateConnectionProperties.put(Environment.FORMAT_SQL, Constants.FORMAT_SQL);
        hibernateConnectionProperties.put(Environment.GENERATE_STATISTICS, Constants.GENERATE_STATISTICS);
        hibernateConnectionProperties.put(Environment.USE_STRUCTURED_CACHE, Constants.USE_STRUCTURED_CACHE);
        /**
         * автоматическая работа со схемой(генерация схемы) происходит, когда инициализируется {@link org.hibernate.SessionFactory}
         */
        hibernateConnectionProperties.put(Environment.HBM2DDL_AUTO, Constants.HBM2DDL_AUTO);

        Configuration hbnConfiguration = new Configuration().setProperties(hibernateConnectionProperties);
        List<Class> annotatedClasses = getEntityClasses(Constants.ENTITIES_PACKAGE);

        if (annotatedClasses != null) {
            annotatedClasses.forEach(hbnConfiguration::addAnnotatedClass);
        }

        factory = hbnConfiguration.buildSessionFactory();

    }

    private static List<Class> getEntityClasses(String packageName) {

        List<Class> classes = new ArrayList<>();

//      можно получить ClassLoader через объект текущего потока: объекты ClassLoader-ов в обоих случаях будут одинаковыми
//      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader classLoader = RunServer.class.getClassLoader();
        String path = packageName.replaceAll("\\.", "/");
        Enumeration<URL> lst;

        try {
            lst = classLoader.getResources(path);
        } catch (IOException e) {
            return null;
        }

        while (lst.hasMoreElements()) {
            File file = new File(lst.nextElement().getPath().replaceAll("%20", " "));
            if (file.isDirectory()) {
                for (File f : file.listFiles()) {
                    if (f.isDirectory()) {
                        classes.addAll(getEntityClasses(packageName + "." + f.getName()));
                    } else {
                        try {
                            Class clazz = Class.forName(packageName + "." + f.getName().substring(0, f.getName().length() - 6));
                            Annotation annotation = clazz.getAnnotation(Entity.class);
                            if(annotation != null){
                                classes.add(clazz);
                            }
                        } catch (ClassNotFoundException e) {
                        }

                    }
                }
            }
        }
        return classes;
    }

    public static HibernateHelper getInstance() {
        HibernateHelper result = instance;
        if (result == null) {
            synchronized (HibernateHelper.class) {
                result = instance;
                if (result == null) {
                    instance = new HibernateHelper();
                }
            }
        }
        return instance;
    }

}
