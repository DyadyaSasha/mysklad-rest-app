package rest;

import app.RestConfig;
import app.model.Product;
import app.model.Purchase;
import app.model.Selling;
import app.model.request.PurchasesRequestModel;
import app.model.request.SellingsRequestModel;
import app.model.response.HttpResponseBody;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityGraph;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Properties;

/**
 * https://memorynotfound.com/test-jersey-rest-service-with-junit/
 * https://www.baeldung.com/jersey-test
 * для каждого метода с аннотацией {@link Test} создаётся новый объект класса {@link RestServiceTest}
 */
public class RestServiceTest extends JerseyTest {

    @Override
    protected Application configure() {

        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        return new RestConfig();
    }

    private SessionFactory factory;
    private Product product;
    private Purchase purchase;
    private Selling selling;

    @Before
    public void initializeSessionFactory() {

        product = new Product();
        product.setName("test");

        purchase = new Purchase();
        purchase.setProductCount(5);
        purchase.setProductPrice(3.2);
        purchase.setPurchaseDate(LocalDate.parse("2019-08-24"));
        purchase.setProduct(product);

        selling = new Selling();
        selling.setProductCount(3);
        selling.setProductPrice(7.2);
        selling.setSellingDate(LocalDate.parse("2019-08-24"));
        selling.setProduct(product);

        Properties hbnProperties = new Properties();
        hbnProperties.put(Environment.DIALECT, "org.hibernate.dialect.H2Dialect");
        hbnProperties.put(Environment.DRIVER, "org.h2.Driver");
        hbnProperties.put(Environment.URL, "jdbc:h2:./my_sklad");
        hbnProperties.put(Environment.USER, "user");
        hbnProperties.put(Environment.PASS, "pass");
        hbnProperties.put(Environment.SHOW_SQL, true);
        hbnProperties.put(Environment.FORMAT_SQL, true);
        hbnProperties.put(Environment.HBM2DDL_AUTO, "create-drop");

        factory = new Configuration()
                .setProperties(hbnProperties)
                .addAnnotatedClass(Product.class)
                .addAnnotatedClass(Purchase.class)
                .addAnnotatedClass(Selling.class)
                .buildSessionFactory();

    }

    @Test
    public void testGetAllowedMethods() {

        String expected = "POST,GET,OPTIONS";

        Response serviceResponse = target("/api")
                .request()
                .options();

        String actual = serviceResponse.getHeaderString("allow");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testCreateProduct() {

        HttpResponseBody expected = HttpResponseBody.builder()
                .code(200)
                .codeMessage("OK")
                .message("Product [test] saved")
                .build();

        Response response = target("/api/newproduct")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(product, MediaType.APPLICATION_JSON));

        HttpResponseBody actual = response.readEntity(HttpResponseBody.class);

        Product productFromDb;
        try (Session session = factory.openSession()) {
            Transaction transaction = session.beginTransaction();

            productFromDb = session.createQuery("from Product p where p.name= :name", Product.class)
                    .setParameter("name", product.getName())
                    .getSingleResult();

            transaction.commit();
        }

        Assert.assertEquals(product, productFromDb);
        Assert.assertEquals(expected, actual);

    }

    @Test
    public void testBuyProduct() {

        HttpResponseBody expected = HttpResponseBody.builder()
                .code(200)
                .message("Purchase for product [test] saved.")
                .codeMessage("OK")
                .build();

        PurchasesRequestModel requestModel = new PurchasesRequestModel();
        requestModel.setPurchases(Arrays.asList(purchase));

        target("/api/newproduct")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(product, MediaType.APPLICATION_JSON));

        Response response = target("/api/purchase")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(requestModel, MediaType.APPLICATION_JSON));


        HttpResponseBody actual = response.readEntity(HttpResponseBody.class);

        Purchase purchaseFromDb;
        try (Session session = factory.openSession()) {
            Transaction transaction = session.beginTransaction();

            /**
             * с помощью Fetch Profile можно выставить на время запроса FetchType равным EAGER у тех полей-ассоциаций, у которых FetchType изначально равен LAZY
             */
            session.enableFetchProfile("purchase_product");
            purchaseFromDb = session.createQuery("from Purchase", Purchase.class).getSingleResult();
            /**
             * подтягиваем "ленивый" объект из БД(purchaseFromDb.getProduct() вернёт !proxy! объект для класса {@link Product}: app.model.Product$HibernateProxy)
             */
//            Hibernate.initialize(purchaseFromDb.getProduct());

            session.disableFetchProfile("purchase_product");


            transaction.commit();
        }

        purchase.getProduct().setCount(5);

        Assert.assertEquals(expected, actual);
        Assert.assertEquals(purchase, purchaseFromDb);
        Assert.assertEquals(purchase.getProduct().getName(), purchaseFromDb.getProduct().getName());
        Assert.assertEquals(purchase.getProduct().getCount(), purchaseFromDb.getProduct().getCount());
    }

    @Test
    public void testSellProduct() {

        HttpResponseBody expected = HttpResponseBody.builder()
                .code(200)
                .codeMessage("OK")
                .message("Selling for product [test] saved.")
                .build();

        PurchasesRequestModel purchaseRequestModel = new PurchasesRequestModel();
        purchaseRequestModel.setPurchases(Arrays.asList(purchase));

        SellingsRequestModel sellingRequestModel = new SellingsRequestModel();
        sellingRequestModel.setSellings(Arrays.asList(selling));

        target("/api/newproduct")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(product, MediaType.APPLICATION_JSON));

        target("/api/purchase")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(purchaseRequestModel, MediaType.APPLICATION_JSON));

        Response response = target("/api/demand")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(sellingRequestModel, MediaType.APPLICATION_JSON));

        HttpResponseBody actual = response.readEntity(HttpResponseBody.class);

        Selling sellingFromDb;
        try (Session session = factory.openSession()) {
            Transaction transaction = session.beginTransaction();

            session.enableFetchProfile("selling_product");
            sellingFromDb = session.createQuery("from Selling", Selling.class)
                    .getSingleResult();

            session.disableFetchProfile("selling_product");
//            Hibernate.initialize(sellingFromDb.getProduct());

            transaction.commit();
        }

        selling.getProduct().setCount(2);

        Assert.assertEquals(expected, actual);
        Assert.assertEquals(selling, sellingFromDb);
        Assert.assertEquals(selling.getProduct().getName(), sellingFromDb.getProduct().getName());
        Assert.assertEquals(selling.getProduct().getCount(), sellingFromDb.getProduct().getCount());

    }

    @Test
    public void testGetSalesReport() {

        LocalDate reportDate = LocalDate.parse("2019-08-24");

        NumberFormat formatter = new DecimalFormat("#0.00");
        HttpResponseBody expected = HttpResponseBody.builder()
                .code(200)
                .codeMessage("OK")
                .message("Income on " + reportDate + ": [" + formatter.format(selling.getProductCount() * (selling.getProductPrice() - purchase.getProductPrice())) + "]")
                .build();

        PurchasesRequestModel purchaseRequestModel = new PurchasesRequestModel();
        purchaseRequestModel.setPurchases(Arrays.asList(purchase));

        SellingsRequestModel sellingRequestModel = new SellingsRequestModel();
        sellingRequestModel.setSellings(Arrays.asList(selling));

        target("/api/newproduct")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(product, MediaType.APPLICATION_JSON));

        target("/api/purchase")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(purchaseRequestModel, MediaType.APPLICATION_JSON));

        target("/api/demand")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(sellingRequestModel, MediaType.APPLICATION_JSON));

        HttpResponseBody actual = target("/api/salesreport/test/" + reportDate)
                .request(MediaType.APPLICATION_JSON)
                .get(HttpResponseBody.class);

        System.out.println(actual.getCode());
        System.out.println(actual.getCodeMessage());
        System.out.println(actual.getMessage());


        Assert.assertEquals(expected, actual);

    }

    @After
    public void clearResources() {
        product = null;
        purchase = null;
        selling = null;
        if (factory != null && !factory.isClosed()) {
            factory.close();
        }
    }

}
