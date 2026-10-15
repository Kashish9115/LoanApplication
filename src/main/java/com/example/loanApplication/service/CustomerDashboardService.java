package com.example.loanApplication.service;

import com.example.loanApplication.dto.dashboard.CustomerDashboardDto;

public interface CustomerDashboardService {

    /**
     * Fetches complete dynamic customer dashboard aggregations by Customer ID.
     *
     * @param customerId Primary Key of the Customer
     * @return Fully populated CustomerDashboardDto
     */
    CustomerDashboardDto getCustomerDashboard(Integer customerId);
}
