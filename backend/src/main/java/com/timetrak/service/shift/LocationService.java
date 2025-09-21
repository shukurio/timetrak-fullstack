package com.timetrak.service.shift;

import com.timetrak.dto.company.CompanyResponseDTO;
import com.timetrak.service.company.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final CompanyService companyService;

    //all measurement in metric

    private static final double EARTH_RADIUS = 6371000;

    // Haversine formula
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    public boolean isWithinAllowedRadius(double employeeLat, double employeeLng, Long companyId) {

        CompanyResponseDTO company = companyService.getCompanyDTOById(companyId);
        double companyLat = company.getLatitude();
        double companyLng = company.getLongitude();
        double allowedRadiusMeters = company.getAllowedRadius();

        double distance = calculateDistance(employeeLat, employeeLng, companyLat, companyLng);
        return distance <= allowedRadiusMeters;
    }
}
