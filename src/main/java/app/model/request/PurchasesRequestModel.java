package app.model.request;

import app.model.Purchase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@ApiModel(description = "Объект закупки/приёмки товаров")
public class PurchasesRequestModel {

    @ApiModelProperty(value = "Список закупок товаров", required = true)
    @Getter
    @Setter
    private List<Purchase> purchases;

}
