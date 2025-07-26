package com.example.nagoyameshi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.CategoryRestaurant;
import com.example.nagoyameshi.entity.Restaurant;

public interface CategoryRestaurantRepository extends JpaRepository<CategoryRestaurant, Integer> {
   @Query("SELECT cr.category.id FROM CategoryRestaurant cr WHERE cr.restaurant = :restaurant ORDER BY cr.id ASC")
   public List<Integer> findCategoryIdsByRestaurantOrderByIdAsc(@Param("restaurant") Restaurant restaurant);

   public Optional<CategoryRestaurant> findByCategoryAndRestaurant(Category category, Restaurant restaurant);
   public List<CategoryRestaurant> findByRestaurantOrderByIdAsc(Restaurant restaurant);
}