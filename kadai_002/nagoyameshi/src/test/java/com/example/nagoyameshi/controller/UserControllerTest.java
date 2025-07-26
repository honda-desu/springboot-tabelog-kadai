package com.example.nagoyameshi.controller;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserControllerTest {
   @Autowired
   private MockMvc mockMvc;

   @Autowired
   private UserService userService;

   @Test
   public void 未ログインの場合は会員用の会員情報ページからログインページにリダイレクトする() throws Exception {
       mockMvc.perform(get("/user"))
              .andExpect(status().is3xxRedirection())
              .andExpect(redirectedUrl("http://localhost/login"));
   }

   @Test
   @WithUserDetails("taro.samurai@example.com")
   public void ログイン済みの場合は会員用の会員情報ページが正しく表示される() throws Exception {
       mockMvc.perform(get("/user"))
              .andExpect(status().isOk())
              .andExpect(view().name("user/index"));
   }

   @Test
   public void 未ログインの場合は会員用の会員情報編集ページからログインページにリダイレクトする() throws Exception {
       mockMvc.perform(get("/user/edit"))
              .andExpect(status().is3xxRedirection())
              .andExpect(redirectedUrl("http://localhost/login"));
   }

   @Test
   @WithUserDetails("taro.samurai@example.com")
   public void ログイン済みの場合は会員用の会員情報編集ページが正しく表示される() throws Exception {
       mockMvc.perform(get("/user/edit"))
              .andExpect(status().isOk())
              .andExpect(view().name("user/edit"));
   }

   @Test
   @Transactional
   public void 未ログインの場合は会員情報を更新せずにログインページにリダイレクトする() throws Exception {
       mockMvc.perform(post("/user/update")
               .with(csrf())
               .param("name", "テスト氏名")
               .param("furigana", "テストフリガナ")
               .param("postalCode", "0000000")
               .param("address", "テスト住所")
               .param("phoneNumber", "00011112222")
               .param("birthday", "20001111")
               .param("occupation", "テスト職業")
               .param("email", "test@example.com"))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("http://localhost/login"));
   }

   @Test
   @WithUserDetails("taro.samurai@example.com")
   @Transactional
   public void ログイン済みの場合は会員情報更新後に会員用の会員情報ページにリダイレクトする() throws Exception {
       User user = userService.findUserByEmail("taro.samurai@example.com");

       mockMvc.perform(post("/user/update")
               .with(csrf())
               .param("name", "テスト氏名")
               .param("furigana", "テストフリガナ")
               .param("postalCode", "0000000")
               .param("address", "テスト住所")
               .param("phoneNumber", "00011112222")
               .param("birthday", "20001111")
               .param("occupation", "テスト職業")
               .param("email", "test@example.com"))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/user"));

       assertThat(user.getName()).isEqualTo("テスト氏名");
       assertThat(user.getFurigana()).isEqualTo("テストフリガナ");
       assertThat(user.getPostalCode()).isEqualTo("0000000");
       assertThat(user.getAddress()).isEqualTo("テスト住所");
       assertThat(user.getPhoneNumber()).isEqualTo("00011112222");
       assertThat(user.getBirthday()).isEqualTo(LocalDate.parse("20001111", DateTimeFormatter.ofPattern("yyyyMMdd")));
       assertThat(user.getOccupation()).isEqualTo("テスト職業");
       assertThat(user.getEmail()).isEqualTo("test@example.com");
   }
}