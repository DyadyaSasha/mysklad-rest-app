package app.model.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "Объект ответа REST сервиса на входящий запрос")
public class HttpResponseBody {

    @ApiModelProperty(value = "Ответ REST сервиса", required = true)
    @Getter
    @Setter
    private String message;

    @ApiModelProperty(value = "Код ответа", required = true)
    @Getter
    @Setter
    private int code;

    @ApiModelProperty(value = "Описание кода ответа", required = true)
    @Getter
    @Setter
    private String codeMessage;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HttpResponseBody that = (HttpResponseBody) o;

        if (getCode() != that.getCode()) return false;
        if (getMessage() != null ? !getMessage().equals(that.getMessage()) : that.getMessage() != null) return false;
        return getCodeMessage() != null ? getCodeMessage().equals(that.getCodeMessage()) : that.getCodeMessage() == null;
    }

    @Override
    public int hashCode() {
        int result = getMessage() != null ? getMessage().hashCode() : 0;
        result = 31 * result + getCode();
        result = 31 * result + (getCodeMessage() != null ? getCodeMessage().hashCode() : 0);
        return result;
    }
}
