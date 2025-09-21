package com.timetrak.service.payment.paymentManagement;

import com.timetrak.dto.payment.status.StatusUpdateRequest;
import com.timetrak.dto.payment.status.StatusUpdateResponse;

public interface PaymentManagementService {

    // =============== BASIC STATUS MANAGEMENT ===============

    StatusUpdateResponse updatePaymentStatus(StatusUpdateRequest request, Long companyId, Long modifierId);

}
