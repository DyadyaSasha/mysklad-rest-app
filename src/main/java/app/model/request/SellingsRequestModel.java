package app.model.request;

import app.model.Selling;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@ApiModel(description = "Объект продажи/отгрузки товаров")
public class SellingsRequestModel {

    @Getter
    @Setter
    @ApiModelProperty(value = "Список продаж товаров", required = true)
    private List<Selling> sellings;

}
