package com.fixlocal.service;

import com.fixlocal.dto.ReviewRequest;
import com.fixlocal.dto.ReviewSummaryDTO;
import com.fixlocal.entity.Review;
import com.fixlocal.repository.ReviewRepository;
import com.fixlocal.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public interface ReviewService {
    public Review addReview(String bookingId, ReviewRequest request, Authentication authentication);
    public List<ReviewSummaryDTO> getTradespersonReviews(String tradespersonId);
}
