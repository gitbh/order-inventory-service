package com.itccompliance.oi.persistence;

import com.itccompliance.oi.domain.model.Order;
import com.itccompliance.oi.domain.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatus(OrderStatus status);
}