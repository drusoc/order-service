package by.java.enterprise.orderservice.exception

class OrderNotFoundException(orderId: java.util.UUID) :
    RuntimeException("Order not found: $orderId")
