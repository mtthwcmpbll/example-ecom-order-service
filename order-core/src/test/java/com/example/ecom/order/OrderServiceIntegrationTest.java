package com.example.ecom.order;

import com.example.ecom.common.dto.ApiResponse;
import com.example.ecom.order.api.dto.OrderDto;
import com.example.ecom.order.client.FraudClient;
import com.example.ecom.order.model.Order;
import com.example.ecom.order.repository.OrderRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
    "FRAUD_DETECTION_SERVICE_URL=http://localhost:8087",
    "PRODUCT_SERVICE_URL=http://localhost:8083",
    "CUSTOMER_SERVICE_URL=http://localhost:8084"
})
@AutoConfigureMockMvc(addFilters = false)
public class OrderServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderRepository orderRepository;

    @MockBean
    private FraudClient fraudClient;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @Test
    public void testCreateOrder() throws Exception {
        OrderDto orderDto = new OrderDto();
        orderDto.setCustomerId(123L);
        orderDto.setTotalAmount(new BigDecimal("100.00"));

        FraudClient.FraudCheckResponse fraudResponse = new FraudClient.FraudCheckResponse();
        fraudResponse.setStatus("APPROVED");

        when(fraudClient.checkFraud(any(FraudClient.FraudCheckRequest.class)))
                .thenReturn(ApiResponse.success(fraudResponse));

        Order order = new Order();
        order.setOrderId("order-uuid");
        order.setCustomerId(123L);
        order.setTotalAmount(new BigDecimal("100.00"));

        when(orderRepository.save(any(Order.class))).thenReturn(order);

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"customerId\":123,\"totalAmount\":100.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.customerId").value(123));
    }

    @Test
    public void testGetOrderById() throws Exception {
        Order order = new Order();
        order.setId(1L);
        order.setOrderId("order-uuid");
        order.setCustomerId(123L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderId").value("order-uuid"));
    }
}
