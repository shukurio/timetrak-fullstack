package com.timetrak.service.dashboard;

import com.timetrak.dto.company.AdminDashboardDTO;

public interface DashboardService {
    AdminDashboardDTO getAdminDashboardInfo(Long companyId);
}
