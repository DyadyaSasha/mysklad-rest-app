package app.api.rest;

import app.helpers.HibernateHelper;
import app.model.Product;
import app.model.Purchase;
import app.model.Selling;
import app.model.request.PurchasesRequestModel;
import app.model.request.SellingsRequestModel;
import app.model.response.HttpResponseBody;
import io.swagger.annotations.*;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "Тестовое задание: API для расчета прибыльности",
                description = "Описанные ниже методы предоставляют возможность создавать, закупать, продавать товар, а также расчитывать прибыльность.\n" +
                        "Расчет прибыльности осуществляется по методу FIFO.\n" +
                        "Пример:\n" +
                        "Приемка (закупка) от 01.01.17 – 1 телефон по цене 1000 р. каждый\n" +
                        "Приемка (закупка) от 01.02.17 – 2 телефона по цене 2000 р. каждый\n" +
                        "Отгрузка (продажа) от 01.03.17 – 2 телефона по цене 5000 р. каждый\n" +
                        "Расчет:\n" +
                        "Сумма себестоимости 2 проданных штук = 1000 + 2000 = 3000\n" +
                        "Себестоимость единицы = 3000/2 = 1500\n" +
                        "Прибыль = 2*5000 – 3000 = 7000\n" +
                        "Общение между клиентом и сервером (rest сервисом) осуществляется с помощью сообщений в формате JSON.",
                contact = @Contact(
                        name = "Александр Серебряков",
                        email = "fake_mail@gmail.com"
                )
        ),
        schemes = {SwaggerDefinition.Scheme.HTTP}
)
@Api(value = "api")
/**
 * на каждый запрос будет создаваться новый объект класса RestService, в отличие от обычных сервлетов, у которых создаётся один объект сервлета, который обрабатывает запросы в разных потоках
 */
@Path("/api")
public class RestService {

    @ApiOperation(value = "Доступные операции в рамках данного сервиса")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK",
                    responseHeaders = {@ResponseHeader(name = "Allow", description = "GET, POST, OPTIONS", response = String.class)})
    })
    @Path("")
    @OPTIONS
    public Response getAllowedMethods() {
        Response response = Response.status(Response.Status.OK)
                .allow("GET", "POST", "OPTIONS")
                .build();
        return response;
    }


    @ApiOperation(
            value = "Отчёт о прибыли, полученной с конкретного товара на указанную дату",
            notes = "Прибыль расчитывается по методу FIFO. Значение даты должно иметь следующий формат: YYYY-MM-DD",
            response = HttpResponseBody.class
    )
    @ApiResponses(
            value = {
                    @ApiResponse(code = 400, message = "Date must have format: YYYY-MM-DD"),
                    @ApiResponse(code = 500, message = "Exception occurred while sales report calculation")
            }
    )
    @GET
    @Path("/salesreport/{name}/{date}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSalesReport(
            @ApiParam(value = "Название товара")
            @PathParam("name") String name,
            @ApiParam(value = "Дата отчёта")
            @PathParam("date") String date
    ) throws Exception {

        HttpResponseBody responseBody = new HttpResponseBody();

        LocalDate reportDate = null;

        try {
            reportDate = LocalDate.parse(date);
        } catch (DateTimeException e) {
            responseBody.setMessage("Date must have format: YYYY-MM-DD");
            responseBody.setCode(Response.Status.BAD_REQUEST.getStatusCode());
            responseBody.setCodeMessage(Response.Status.BAD_REQUEST.getReasonPhrase());
        }

        if (reportDate != null) {
            Transaction transaction = null;
            try (Session session = HibernateHelper.getInstance().getFactory().openSession()) {
                transaction = session.beginTransaction();

                CriteriaBuilder builder = session.getCriteriaBuilder();
                CriteriaQuery<Purchase> purchaseQuery = builder.createQuery(Purchase.class);
                Root<Purchase> purchaseRoot = purchaseQuery.from(Purchase.class);

                List<Purchase> purchases = session.createQuery(purchaseQuery.select(purchaseRoot)
                        .where(builder.and(builder.lessThanOrEqualTo(purchaseRoot.get("purchaseDate"), reportDate),
                                builder.equal(purchaseRoot.get("product").get("name"), name)))
                        .orderBy(builder.asc(purchaseRoot.get("purchaseDate")))
                )
                        .list();


                builder = session.getCriteriaBuilder();
                CriteriaQuery<Tuple> sellingQuery = builder.createTupleQuery();
                Root<Selling> sellingRoot = sellingQuery.from(Selling.class);

                Tuple sellingCountAndSum = session.createQuery(sellingQuery.multiselect(
                        builder.sumAsLong(sellingRoot.get("productCount")),
                        builder.sumAsDouble(
                                builder.prod(sellingRoot.get("productCount"), sellingRoot.get("productPrice"))
                        )
                        )
                                .where(builder.and(builder.lessThanOrEqualTo(sellingRoot.get("sellingDate"), reportDate),
                                        builder.equal(sellingRoot.get("product").get("name"), name)))
                )
                        .getSingleResult();

                long sellingProductCount = sellingCountAndSum.get(0, Long.class);
                double sellingProductFullPrice = sellingCountAndSum.get(1, Double.class);

                long productCount = -sellingProductCount;
                double fullPrice = sellingProductFullPrice;
                for (Purchase purchase : purchases) {
                    if (productCount == 0) {
                        break;
                    } else {
                        if (productCount + purchase.getProductCount() <= 0) {
                            productCount += purchase.getProductCount();
                            fullPrice -= purchase.getProductCount() * purchase.getProductPrice();
                        } else {
                            fullPrice -= -productCount * purchase.getProductPrice();
                            productCount = 0;
                        }
                    }
                }


                responseBody.setCode(Response.Status.OK.getStatusCode());
                responseBody.setCodeMessage(Response.Status.OK.getReasonPhrase());

                NumberFormat formatter = new DecimalFormat("#0.00");
                responseBody.setMessage("Income on " + reportDate + ": [" + formatter.format(fullPrice) + "]");

                transaction.commit();
            } catch (Exception e) {
                if (transaction != null && transaction.isActive()) {
                    transaction.rollback();
                }

                responseBody.setCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
                responseBody.setCodeMessage(Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
                responseBody.setMessage("Exception occurred while sales report calculation");
            }
        }

        Response response = Response.status(responseBody.getCode())
                .entity(responseBody)
                .build();

        return response;
    }


    @ApiOperation(
            value = "Создание нового товара",
            notes = "Нельзя повторно создать товар с тем же именем",
            response = HttpResponseBody.class
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Product [product_name] already exists"),
            @ApiResponse(code = 500, message = "Exception occurred while saving product")
    })
    @POST
    @Path("/newproduct")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createProduct(
            @ApiParam(value = "Объект товара (указывается только наименование товара)", required = true)
                    Product product
    ) {
        HttpResponseBody responseBody = new HttpResponseBody();

        Transaction transaction = null;

        try (Session session = HibernateHelper.getInstance().getFactory().openSession()) {

            transaction = session.beginTransaction();

            Product productFromDb = session.createQuery("from Product p where p.name=:product_name", Product.class)
                    .setParameter("product_name", product.getName())
                    .setCacheable(true)
                    .setCacheRegion("productByName_query_region")
                    .uniqueResult();

            if (productFromDb == null) {
                session.save(product);
                responseBody.setMessage("Product [" + product.getName() + "] saved");
                responseBody.setCode(Response.Status.OK.getStatusCode());
                responseBody.setCodeMessage(Response.Status.OK.getReasonPhrase());
            } else {
                responseBody.setMessage("Product [" + product.getName() + "] already exists");
                responseBody.setCode(Response.Status.BAD_REQUEST.getStatusCode());
                responseBody.setCodeMessage(Response.Status.BAD_REQUEST.getReasonPhrase());
            }

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            responseBody.setMessage("Exception occurred while saving product");
            responseBody.setCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            responseBody.setCodeMessage(Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
        }

        Response response = Response.status(responseBody.getCode())
                .entity(responseBody)
                .build();

        return response;
    }

    @ApiOperation(
            value = "Закупка(приёмка) товара/товаров",
            notes = "Количество закупаемого товара должно быть больше нуля. Минимальная возможная цена товара равна нулю. Дата закупки должна иметь следующий формат: YYYY-MM-DD",
            response = HttpResponseBody.class
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Products does not specified"),
            @ApiResponse(code = 500, message = "Exception occurred while saving purchases")
    })
    @POST
    @Path("/purchase")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response buyProduct(
            @ApiParam(value = "Список закупленных товаров", required = true)
                    PurchasesRequestModel requestModel
    ) {

        List<Purchase> purchases = requestModel.getPurchases();
        HttpResponseBody responseBody = new HttpResponseBody();

        if (purchases == null || purchases.isEmpty()) {
            responseBody.setMessage("Products does not specified");
            responseBody.setCode(Response.Status.BAD_REQUEST.getStatusCode());
            responseBody.setCodeMessage(Response.Status.BAD_REQUEST.getReasonPhrase());
        } else {
            Transaction transaction = null;
            try (Session session = HibernateHelper.getInstance().getFactory().openSession()) {
                transaction = session.beginTransaction();

                CriteriaBuilder builder = session.getCriteriaBuilder();
                CriteriaQuery<Product> productNamesQuery = builder.createQuery(Product.class);
                Root<Product> productRoot = productNamesQuery.from(Product.class);
                productNamesQuery.select(productRoot);

                List<Product> productsFromDb = session.createQuery(productNamesQuery).list();
                List<String> productNames = productsFromDb
                        .stream()
                        .map(Product::getName)
                        .collect(Collectors.toList());

                StringBuilder sb = new StringBuilder();

                purchases.forEach(purchase -> {
                    if (productNames.contains(purchase.getProduct().getName())) {
                        if (purchase.getProductCount() > 0 && purchase.getProductPrice() >= 0 && purchase.getPurchaseDate() != null) {

                            Product actualProduct = productsFromDb
                                    .stream()
                                    .filter(product -> product.getName().equals(purchase.getProduct().getName()))
                                    .findFirst()
                                    .get();
                            actualProduct.setCount(actualProduct.getCount() + purchase.getProductCount());

                            purchase.setProduct(actualProduct);

                            session.save(purchase);

                            sb.append("Purchase for product [")
                                    .append(actualProduct.getName())
                                    .append("] saved. ");
                        } else {
                            sb.append("Error: in product [")
                                    .append(purchase.getProduct().getName())
                                    .append("] purchase data incorrect: productCount must be > 0, productPrice must be >=0, productDate must be not null and have format YYYY-MM-DD. ");
                        }
                    } else {
                        sb.append("Error: product [")
                                .append(purchase.getProduct().getName())
                                .append("] does not exist. ");
                    }
                });

                responseBody.setMessage(sb.toString().trim());
                responseBody.setCode(Response.Status.OK.getStatusCode());
                responseBody.setCodeMessage(Response.Status.OK.getReasonPhrase());

                transaction.commit();
            } catch (Exception e) {
                if (transaction != null && transaction.isActive()) {
                    transaction.rollback();
                }
                responseBody.setMessage("Exception occurred while saving purchases");
                responseBody.setCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
                responseBody.setCodeMessage(Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
            }
        }

        Response response = Response.status(responseBody.getCode())
                .entity(responseBody)
                .build();

        return response;
    }

    @ApiOperation(
            value = "Продажа(отгрузка) товара/товаров",
            notes = "Количество продоваемого товара должно быть больше нуля. Минимальная возможная цена товара равна нулю. Дата продажи должна иметь следующий формат: YYYY-MM-DD",
            response = HttpResponseBody.class
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Products does not specified"),
            @ApiResponse(code = 500, message = "Exception occurred while saving sellings")
    })
    @POST
    @Path("/demand")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response sellProduct(
            @ApiParam(value = "Список проданных товаров", required = true)
                    SellingsRequestModel requestModel
    ) {

        List<Selling> sellings = requestModel.getSellings();
        HttpResponseBody responseBody = new HttpResponseBody();

        if (sellings == null || sellings.isEmpty()) {
            responseBody.setMessage("Products does not specified");
            responseBody.setCode(Response.Status.BAD_REQUEST.getStatusCode());
            responseBody.setCodeMessage(Response.Status.BAD_REQUEST.getReasonPhrase());
        } else {
            Transaction transaction = null;
            try (Session session = HibernateHelper.getInstance().getFactory().openSession()) {
                transaction = session.beginTransaction();

                List<Product> productsFromDb = session.createQuery("from Product", Product.class).list();
                List<String> productNames = productsFromDb
                        .stream()
                        .map(Product::getName)
                        .collect(Collectors.toList());

                StringBuilder sb = new StringBuilder();

                sellings.forEach(selling -> {
                    if (productNames.contains(selling.getProduct().getName())) {
                        if (selling.getProductCount() > 0 && selling.getProductPrice() >= 0 && selling.getSellingDate() != null) {

                            Product actualProduct = productsFromDb
                                    .stream()
                                    .filter(product -> product.getName().equals(selling.getProduct().getName()))
                                    .findFirst()
                                    .get();

                            if (actualProduct.getCount() >= selling.getProductCount()) {

                                actualProduct.setCount(actualProduct.getCount() - selling.getProductCount());
                                selling.setProduct(actualProduct);
                                session.save(selling);

                                sb.append("Selling for product [")
                                        .append(actualProduct.getName())
                                        .append("] saved. ");

                            } else {
                                sb.append("Error: in product [")
                                        .append(actualProduct.getName())
                                        .append("] - actual product count must be equal or greater productCount in selling");
                            }


                        } else {
                            sb.append("Error: in product [")
                                    .append(selling.getProduct().getName())
                                    .append("] selling data incorrect: productCount must be > 0, productPrice must be >=0, productDate must be not null and have format YYYY-MM-DD. ");
                        }
                    } else {
                        sb.append("Error: product [")
                                .append(selling.getProduct().getName())
                                .append("] does not exist. ");
                    }
                });

                responseBody.setMessage(sb.toString().trim());
                responseBody.setCode(Response.Status.OK.getStatusCode());
                responseBody.setCodeMessage(Response.Status.OK.getReasonPhrase());

                transaction.commit();
            } catch (Exception e) {
                if (transaction != null && transaction.isActive()) {
                    transaction.rollback();
                }
                responseBody.setMessage("Exception occurred while saving sellings");
                responseBody.setCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
                responseBody.setCodeMessage(Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
            }
        }

        Response response = Response.status(responseBody.getCode())
                .entity(responseBody)
                .build();

        return response;
    }


}
