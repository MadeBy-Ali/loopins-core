package com.loopins.core.repository;

import com.loopins.core.domain.entity.PaymentCallbackLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentCallbackLogRepository extends JpaRepository<PaymentCallbackLog, Long> {

    boolean existsByCallbackReference(String callbackReference);

    Optional<PaymentCallbackLog> findByCallbackReference(String callbackReference);

    boolean existsByOrderIdAndCallbackReference(String orderId, String callbackReference);
}

