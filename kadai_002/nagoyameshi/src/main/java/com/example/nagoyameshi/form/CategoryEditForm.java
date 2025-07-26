package com.example.nagoyameshi.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryEditForm {
	@NotBlank
	private String name;
}
