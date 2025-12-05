package com.example.ecom.order.model;

import org.junit.Test;
import java.math.BigDecimal;
import java.util.Collections;
import static org.junit.Assert.*;

public class OrderTest {

    @Test
    public void testOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setCustomerId(100L);
        order.setTotalAmount(new BigDecimal("150.00"));

        Order.OrderItem item = new Order.OrderItem("prod-1", 2);
        order.setItems(Collections.singletonList(item));

        assertEquals(Long.valueOf(1L), order.getId());
        assertEquals(Long.valueOf(100L), order.getCustomerId());
        assertEquals(new BigDecimal("150.00"), order.getTotalAmount());
        assertEquals(1, order.getItems().size());
        assertEquals("prod-1", order.getItems().get(0).getProductId());
    }
}
