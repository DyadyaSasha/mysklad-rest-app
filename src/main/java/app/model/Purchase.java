package app.model;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.Fetch;

import javax.persistence.*;
import javax.validation.constraints.Min;
import java.time.LocalDate;

@ApiModel(description = "Закупка товара")
@Entity
@Table(name = "purchase", indexes = {
        @Index(name = "purchase_date_idx", columnList = "purchase_date")
})
@Check(constraints = "product_count > 0 AND product_price >= 0")
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purchase_id")
    private Long id;

    @ApiModelProperty(value = "Количество закупаемого товара", required = true)
    @Min(value = 1)
    @Column(name = "product_count", nullable = false)
    @Getter
    @Setter
    private int productCount;

    @ApiModelProperty(value = "Цена одной единицы товара", required = true)
    @Min(value = 0)
    @Column(name = "product_price", nullable = false)
    @Getter
    @Setter
    private double productPrice;

    @ApiModelProperty(value = "Дата закупки товара", required = true)
    @Column(name = "purchase_date", nullable = false)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @Getter
    @Setter
    private LocalDate purchaseDate;

    @ApiModelProperty(value = "Ссылка на объект товара", required = true)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @Getter
    @Setter
    private Product product;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Purchase purchase = (Purchase) o;

        if (getProductCount() != purchase.getProductCount()) return false;
        if (Double.compare(purchase.getProductPrice(), getProductPrice()) != 0) return false;
        if (!getPurchaseDate().equals(purchase.getPurchaseDate())) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = getProductCount();
        temp = Double.doubleToLongBits(getProductPrice());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + getPurchaseDate().hashCode();
        result = 31 * result + getProduct().hashCode();
        return result;
    }
}
