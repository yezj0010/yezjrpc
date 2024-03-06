package cc.yezj.rpc.demo.api;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Order {
    private long id;
    private BigDecimal amount;
}
