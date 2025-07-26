package com.example.nagoyameshi.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.entity.VerificationToken;
import com.example.nagoyameshi.repository.VerificationTokenRepository;

@Service
public class VerificationTokenService {
   private final VerificationTokenRepository verificationTokenRepository;

   public VerificationTokenService(VerificationTokenRepository verificationTokenRepository) {
       this.verificationTokenRepository = verificationTokenRepository;
   }

   @Transactional
   public void createVerificationToken(User user, String token) {
       VerificationToken verificationToken = new VerificationToken();

       verificationToken.setUser(user);
       verificationToken.setToken(token);

       verificationTokenRepository.save(verificationToken);
   }
   
   @Transactional
   public void update(User user, String token) {
       Optional<VerificationToken> tokenOpt = Optional.ofNullable(verificationTokenRepository.findByUser(user));

       if (tokenOpt.isPresent()) {
           VerificationToken verificationToken = tokenOpt.get();
           verificationToken.setToken(token);
           verificationTokenRepository.save(verificationToken);
       } else {
           // トークンが存在しない場合は新規作成する（任意の仕様に応じて調整可能）
           createVerificationToken(user, token);
       }
   }


   // トークンの文字列で検索した結果を返す
   public VerificationToken findVerificationTokenByToken(String token) {
       return verificationTokenRepository.findByToken(token);
   }
}