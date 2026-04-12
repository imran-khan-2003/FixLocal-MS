package com.fixlocal.service;

import com.fixlocal.dto.ServiceOfferingDTO;
import com.fixlocal.dto.TradespersonDTO;
import com.fixlocal.exception.UserException;
import com.fixlocal.entity.*;
import com.fixlocal.enums.*;
import com.fixlocal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;

public interface TradespersonService {
    public Page<TradespersonDTO> searchTradespersons( String city, String occupation, Double minRating, String tag, Double latitude, Double longitude, Double radiusKm, int page, int size );
    public TradespersonDTO getTradespersonById(String id);
}
