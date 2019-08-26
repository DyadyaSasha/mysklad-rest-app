package app.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Check;

import javax.persistence.*;
import javax.validation.constraints.Min;
import java.time.LocalDate;

@ApiModel(description = "Продажа товара")
@Entity
@Table(name = "selling", indexes = {
        @Index(name = "selling_date_idx", columnList = "selling_date")
})
@Check(constraints = "product_count > 0 AND product_price >= 0")
@Cacheable
@org.hibernate.annotations.Cache(region = "selling_region", usage = CacheConcurrencyStrategy.TRANSACTIONAL)
public class Selling {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "selling_id")
    private Long id;

    @ApiModelProperty(value = "Количество продоваемого товара", required = true)
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
    @Column(name = "selling_date", nullable = false)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @Getter
    @Setter
    private LocalDate sellingDate;

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

        Selling selling = (Selling) o;

        if (getProductCount() != selling.getProductCount()) return false;
        if (Double.compare(selling.getProductPrice(), getProductPrice()) != 0) return false;
        return getSellingDate().equals(selling.getSellingDate());
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = getProductCount();
        temp = Double.doubleToLongBits(getProductPrice());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + getSellingDate().hashCode();
        return result;
    }
}
