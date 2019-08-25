package app.helpers;

public class Constants {

    public static final String ENTITIES_PACKAGE = "app.model";
    public static final String DIALECT = "org.hibernate.dialect.H2Dialect";
    public static final String DRIVER = "org.h2.Driver";
    /**
     * если запускать на не встроенном(not embedded) сервере, например Tomcat, то файлы с базой !создадутся в папке <PATH_TO_SERVER>/bin!
     */
    public static final String URL = "jdbc:h2:./my_sklad";
    public static final String USER = "user";
    public static final String PASS = "pass";
    public static final boolean SHOW_SQL = true;
    public static final boolean FORMAT_SQL = true;
    public static final String HBM2DDL_AUTO = "create";
//    private static final String USE_STRUCTURED_CACHE = "";
//    private static final String USE_QUERY_CACHE = "";
//    private static final String USE_SECOND_LEVEL_CACHE = "";
//    private static final String CACHE_REGION_FACTORY = "";

}
