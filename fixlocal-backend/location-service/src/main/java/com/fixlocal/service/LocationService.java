package com.fixlocal.service;

import com.fixlocal.dto.InternalLiveLocationUpsertRequest;
import com.fixlocal.dto.LiveLocationEvent;

public interface LocationService {

    void upsertLocation(String bookingId, InternalLiveLocationUpsertRequest request);

    LiveLocationEvent getLocation(String bookingId);

    void deleteLocation(String bookingId);
}
