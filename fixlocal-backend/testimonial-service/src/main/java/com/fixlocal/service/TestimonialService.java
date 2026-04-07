package com.fixlocal.service;

import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.fixlocal.dto.TestimonialRequest;
import com.fixlocal.entity.Testimonial;
import com.fixlocal.repository.TestimonialRepository;
import lombok.RequiredArgsConstructor;

public interface TestimonialService {
    public List<Testimonial> findLatest(int limit);
    public Testimonial addTestimonial(TestimonialRequest request);
}
