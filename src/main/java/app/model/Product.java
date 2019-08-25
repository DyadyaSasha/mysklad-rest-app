package app.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Check;

import javax.persistence.*;
import javax.validation.constraints.Min;
import java.util.Set;

@ApiModel(description = "Товар, хранящийся на складе")
@Entity
@Table(name = "product")
/**
 * {@link Check} будет работать (генирировать соответствующую команду при создании схемы) только если определить эту аннотацию !над классом!
 */
@Check(constraints = "count >= 0")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @ApiModelProperty(value = "Имя товара(уникальное значение)", required = true)
    @Column(name = "name", unique = true, nullable = false)
    @Getter
    @Setter
    private String name;

    @ApiModelProperty(hidden = true)
    @Min(value = 0)
    @Column(name = "count", nullable = false)
    @Getter
    @Setter
    private int count;

    @ApiModelProperty(hidden = true)
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    @Getter
    @Setter
    private Set<Purchase> purchases;

    @ApiModelProperty(hidden = true)
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    @Getter
    @Setter
    private Set<Selling> sellings;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Product product = (Product) o;

        if (getCount() != product.getCount()) {
            return false;
        }
        return getName().equals(product.getName());
    }

    @Override
    public int hashCode() {
        int result = getName().hashCode();
        result = 31 * result + getCount();
        return result;
    }
}
