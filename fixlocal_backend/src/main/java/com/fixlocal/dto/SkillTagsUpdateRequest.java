package com.fixlocal.dto;

import java.util.List;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SkillTagsUpdateRequest {

    @Size(max = 15, message = "You can provide up to 15 tags")
    private List<@Size(max = 50, message = "Tag length must be <= 50 characters") String> tags;
}
