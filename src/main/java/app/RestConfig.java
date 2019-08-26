package app;


import io.swagger.jaxrs.config.BeanConfig;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

/**
 * Данный класс позволяет не определять в web.xml сервлет, обрабатывающий запросы на rest api(org.glassfish.jersey.servlet.ServletContainer),
 * а также пакет, в котором находятся классы, обрабатывающие rest запросы. (может использоваться вместе с дескриптором развёртования web.xml)
 * аннотация {@link ApplicationPath} указывает url, который обрабатывает сервлет, обрабатывающий запросы на rest api(org.glassfish.jersey.servlet.ServletContainer)
 */
@ApplicationPath("/app")
public class RestConfig extends ResourceConfig{

    public RestConfig(){
        /**
         * указываем пакет, в котором находятся классы, обрабатывающие rest запросы, а также пакет,
         * необходимый для работы swagger
         */
        packages("io.swagger.jaxrs.listing", "app.api.rest");
//        packages( "app.api.rest");
        /**
         * настройки swagger
         */
        BeanConfig config = new BeanConfig();
        /**
         * закомментированный ниже код выполняет ту же функциональность что и аннотация {@link io.swagger.annotations.SwaggerDefinition} в классе {@link app.api.rest.RestService}
         */
//        config.setVersion("1.0.0");
//        config.setTitle("Тестовое задание: API для расчета прибыльности");
//        config.setDescription(
//                "Описанные ниже методы предоставляют возможность создавать, закупать, продавать товар, а также расчитывать прибыльность.\n" +
//                "Расчет прибыльности осуществляется по методу FIFO.\n" +
//                "Пример:\n" +
//                "Приемка (закупка) от 01.01.17 – 1 телефон по цене 1000 р. каждый\n" +
//                "Приемка (закупка) от 01.02.17 – 2 телефона по цене 2000 р. каждый\n" +
//                "Отгрузка (продажа) от 01.03.17 – 2 телефона по цене 5000 р. каждый\n" +
//                "Расчет:\n" +
//                "Сумма себестоимости 2 проданных штук = 1000 + 2000 = 3000\n" +
//                "Себестоимость единицы = 3000/2 = 1500\n" +
//                "Прибыль = 2*5000 – 3000 = 7000\n" +
//                "Общение между клиентом и сервером (rest сервисом) осуществляется с помощью сообщений в формате JSON."
//        );
//        config.setSchemes(new String[]{"http"});
        config.setHost("localhost:8080");
        config.setBasePath("/app");
        /**
         * пакет, в котором находятся классы, описывающие rest-сервисы
         */
        config.setResourcePackage("app.api.rest");
        config.setPrettyPrint(true);
        config.setScan(true);


    }

}
