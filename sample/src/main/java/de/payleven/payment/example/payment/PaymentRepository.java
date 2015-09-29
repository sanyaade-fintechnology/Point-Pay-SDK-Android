package de.payleven.payment.example.payment;

import java.math.BigDecimal;
import java.util.List;

/**
 * Describes a repository which is capable of storing information about sale payments.
 */
public interface PaymentRepository {
    void addPayment(SalePayment payment);
    List<SalePayment> getAll();
    List<SalePayment> getAllApproved();
    boolean updateState(String id, SalePayment.State state);
    boolean updateAmount(String id, BigDecimal amount);
    void clear();
}
