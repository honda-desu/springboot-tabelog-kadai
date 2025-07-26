package com.example.nagoyameshi.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.Favorite;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.repository.FavoriteRepository;

@Service
public class FavoriteService {
   private final FavoriteRepository favoriteRepository;

   public FavoriteService(FavoriteRepository favoriteRepository) {
       this.favoriteRepository = favoriteRepository;
   }

   // 指定したidを持つお気に入りを取得する
   public Optional<Favorite> findFavoriteById(Integer id) {
       return favoriteRepository.findById(id);
   }

   // 指定した店舗とユーザーが紐づいたお気に入りを取得する
   public Favorite findFavoriteByRestaurantAndUser(Restaurant restaurant, User user) {
       return favoriteRepository.findByRestaurantAndUser(restaurant, user);
   }

   // 指定したユーザーのすべてのお気に入りを作成日時が新しい順に並べ替え、ページングされた状態で取得する
   public Page<Favorite> findFavoritesByUserOrderByCreatedAtDesc(User user, Pageable pageable) {
       return favoriteRepository.findByUserOrderByCreatedAtDesc(user, pageable);
   }

   // お気に入りのレコード数を取得する
   public long countFavorites() {
       return favoriteRepository.count();
   }

   @Transactional
   public void createFavorite(Restaurant restaurant, User user) {
       Favorite favorite = new Favorite();

       favorite.setRestaurant(restaurant);
       favorite.setUser(user);

       favoriteRepository.save(favorite);
   }

   @Transactional
   public void deleteFavorite(Favorite favorite) {
       favoriteRepository.delete(favorite);
   }

   // 指定したユーザーが指定した店舗をすでにお気に入りに追加済みかどうかをチェックする
   public boolean isFavorite(Restaurant restaurant, User user) {
       return favoriteRepository.findByRestaurantAndUser(restaurant, user) != null;
   }
}