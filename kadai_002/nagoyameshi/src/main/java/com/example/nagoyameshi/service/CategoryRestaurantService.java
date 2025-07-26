package com.example.nagoyameshi.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.CategoryRestaurant;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.repository.CategoryRestaurantRepository;

@Service
public class CategoryRestaurantService {
   private final CategoryRestaurantRepository categoryRestaurantRepository;
   private final CategoryService categoryService;

   public CategoryRestaurantService(CategoryRestaurantRepository categoryRestaurantRepository, CategoryService categoryService) {
       this.categoryRestaurantRepository = categoryRestaurantRepository;
       this.categoryService = categoryService;
   }

   // 指定した店舗のカテゴリのidをリスト形式で取得する
   public List<Integer> findCategoryIdsByRestaurantOrderByIdAsc(Restaurant restaurant) {
       return categoryRestaurantRepository.findCategoryIdsByRestaurantOrderByIdAsc(restaurant);
   }

   @Transactional
   public void createCategoriesRestaurants(List<Integer> categoryIds, Restaurant restaurant) {
       for (Integer categoryId : categoryIds) {
           if (categoryId != null) {
               Optional<Category> optionalCategory = categoryService.findCategoryById(categoryId);

               if (optionalCategory.isPresent()) {
                   Category category = optionalCategory.get();

                   Optional<CategoryRestaurant> optionalCurrentCategoryRestaurant = categoryRestaurantRepository.findByCategoryAndRestaurant(category, restaurant);

                   // 重複するエンティティが存在しない場合は新たにエンティティを作成する
                   if (optionalCurrentCategoryRestaurant.isEmpty()) {
                       CategoryRestaurant categoryRestaurant = new CategoryRestaurant();
                       categoryRestaurant.setRestaurant(restaurant);
                       categoryRestaurant.setCategory(category);

                       categoryRestaurantRepository.save(categoryRestaurant);
                   }
               }
           }
       }
   }

   @Transactional
   public void syncCategoriesRestaurants(List<Integer> newCategoryIds, Restaurant restaurant) {
       List<CategoryRestaurant> currentCategoriesRestaurants = categoryRestaurantRepository.findByRestaurantOrderByIdAsc(restaurant);

       if (newCategoryIds == null) {
           // newCategoryIdsがnullの場合はすべてのエンティティを削除する
           for (CategoryRestaurant currentCategoryRestaurant : currentCategoriesRestaurants) {
               categoryRestaurantRepository.delete(currentCategoryRestaurant);
           }
       } else {
           // 既存のエンティティが新しいリストに存在しない場合は削除する
           for (CategoryRestaurant currentCategoryRestaurant : currentCategoriesRestaurants) {
               if (!newCategoryIds.contains(currentCategoryRestaurant.getCategory().getId())) {
                   categoryRestaurantRepository.delete(currentCategoryRestaurant);
               }
           }

           for (Integer newCategoryId : newCategoryIds) {
               if (newCategoryId != null) {
                   Optional<Category> optionalCategory = categoryService.findCategoryById(newCategoryId);

                   if (optionalCategory.isPresent()) {
                       Category category = optionalCategory.get();

                       Optional<CategoryRestaurant> optionalCurrentCategoryRestaurant = categoryRestaurantRepository.findByCategoryAndRestaurant(category, restaurant);

                       // 重複するエンティティが存在しない場合は新たにエンティティを作成する
                       if (optionalCurrentCategoryRestaurant.isEmpty()) {
                           CategoryRestaurant categoryRestaurant = new CategoryRestaurant();
                           categoryRestaurant.setRestaurant(restaurant);
                           categoryRestaurant.setCategory(category);

                           categoryRestaurantRepository.save(categoryRestaurant);
                       }
                   }
               }
           }
       }
   }
}