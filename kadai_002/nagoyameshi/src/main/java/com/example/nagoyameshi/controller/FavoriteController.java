package com.example.nagoyameshi.controller;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.Favorite;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.FavoriteService;
import com.example.nagoyameshi.service.RestaurantService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class FavoriteController {
   private final RestaurantService restaurantService;
   private final FavoriteService favoriteService;

   public FavoriteController(RestaurantService restaurantService, FavoriteService favoriteService) {
       this.restaurantService = restaurantService;
       this.favoriteService = favoriteService;
   }

   @GetMapping("/favorites")
   public String index(@PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable,
                       @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
                       RedirectAttributes redirectAttributes,
                       Model model)
   {
       User user = userDetailsImpl.getUser();

       if (user.getRole().getName().equals("ROLE_FREE_MEMBER")) {
           redirectAttributes.addFlashAttribute("subscriptionMessage", "この機能を利用するには有料プランへの登録が必要です。");

           return "redirect:/subscription/register";
       }

       Page<Favorite> favoritePage = favoriteService.findFavoritesByUserOrderByCreatedAtDesc(user, pageable);

       model.addAttribute("favoritePage", favoritePage);

       return "favorites/index";
   }

   @PostMapping("/restaurants/{restaurantId}/favorites/create")
   public String create(@PathVariable(name = "restaurantId") Integer restaurantId,
                        @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
                        RedirectAttributes redirectAttributes,
                        Model model)
   {
       User user = userDetailsImpl.getUser();

       if (user.getRole().getName().equals("ROLE_FREE_MEMBER")) {
           redirectAttributes.addFlashAttribute("subscriptionMessage", "この機能を利用するには有料プランへの登録が必要です。");

           return "redirect:/subscription/register";
       }

       Optional<Restaurant> optionalRestaurant  = restaurantService.findRestaurantById(restaurantId);

       if (optionalRestaurant.isEmpty()) {
           redirectAttributes.addFlashAttribute("errorMessage", "店舗が存在しません。");

           return "redirect:/restaurants";
       }

       Restaurant restaurant = optionalRestaurant.get();

       favoriteService.createFavorite(restaurant, user);
       redirectAttributes.addFlashAttribute("successMessage", "お気に入りに追加しました。");

       return "redirect:/restaurants/{restaurantId}";
   }

   @PostMapping("/favorites/{favoriteId}/delete")
   public String delete(@PathVariable(name = "favoriteId") Integer favoriteId,
                        @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
                        RedirectAttributes redirectAttributes,
                        HttpServletRequest httpServletRequest)
   {
       User user = userDetailsImpl.getUser();

       if (user.getRole().getName().equals("ROLE_FREE_MEMBER")) {
           redirectAttributes.addFlashAttribute("subscriptionMessage", "この機能を利用するには有料プランへの登録が必要です。");

           return "redirect:/subscription/register";
       }

       Optional<Favorite> optionalFavorite  = favoriteService.findFavoriteById(favoriteId);
       String referer = httpServletRequest.getHeader("Referer");

       if (optionalFavorite.isEmpty()) {
           redirectAttributes.addFlashAttribute("errorMessage", "お気に入りが存在しません。");

           return "redirect:" + (referer != null ? referer : "/favorites");
       }

       Favorite favorite = optionalFavorite.get();

       if (!favorite.getUser().getId().equals(user.getId())) {
           redirectAttributes.addFlashAttribute("errorMessage", "不正なアクセスです。");

           return "redirect:" + (referer != null ? referer : "/favorites");
       }

       favoriteService.deleteFavorite(favorite);
       redirectAttributes.addFlashAttribute("successMessage", "お気に入りを解除しました。");

       return "redirect:" + (referer != null ? referer : "/favorites");
   }
}