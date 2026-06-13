package by.java.enterprise.orderservice.service

import by.java.enterprise.orderservice.repository.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class OrderService(
    private val orderRepository: OrderRepository,
    private val totalCalculator: OrderTotalCalculator,
    private val statusMachine: OrderStatusMachine
) {

}