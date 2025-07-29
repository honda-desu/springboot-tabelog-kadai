package com.example.nagoyameshi.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.Role;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.PasswordResetForm;
import com.example.nagoyameshi.form.SignupForm;
import com.example.nagoyameshi.form.UserEditForm;
import com.example.nagoyameshi.repository.RoleRepository;
import com.example.nagoyameshi.repository.UserRepository;

@Service
public class UserService {
   private final UserRepository userRepository;
   private final RoleRepository roleRepository;
   private final PasswordEncoder passwordEncoder;

   public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
       this.userRepository = userRepository;
       this.roleRepository = roleRepository;
       this.passwordEncoder = passwordEncoder;
   }

   @Transactional
   public User createUser(SignupForm signupForm) {
       User user = new User();
       Role role = roleRepository.findByName("ROLE_FREE_MEMBER");

       user.setName(signupForm.getName());
       user.setFurigana(signupForm.getFurigana());
       user.setPostalCode(signupForm.getPostalCode());
       user.setAddress(signupForm.getAddress());
       user.setPhoneNumber(signupForm.getPhoneNumber());

       if (!signupForm.getBirthday().isEmpty()) {
           user.setBirthday(LocalDate.parse(signupForm.getBirthday(), DateTimeFormatter.ofPattern("yyyyMMdd")));
       } else {
           user.setBirthday(null);
       }

       if (!signupForm.getOccupation().isEmpty()) {
           user.setOccupation(signupForm.getOccupation());
       } else {
           user.setOccupation(null);
       }

       user.setEmail(signupForm.getEmail());
       user.setPassword(passwordEncoder.encode(signupForm.getPassword()));
       user.setRole(role);
       user.setEnabled(false);

       return userRepository.save(user);
   }
   
   @Transactional
   public void updateUser(UserEditForm userEditForm, User user) {
       user.setName(userEditForm.getName());
       user.setFurigana(userEditForm.getFurigana());
       user.setPostalCode(userEditForm.getPostalCode());
       user.setAddress(userEditForm.getAddress());
       user.setPhoneNumber(userEditForm.getPhoneNumber());

       if (!userEditForm.getBirthday().isEmpty()) {
           user.setBirthday(LocalDate.parse(userEditForm.getBirthday(), DateTimeFormatter.ofPattern("yyyyMMdd")));
       } else {
           user.setBirthday(null);
       }

       if (!userEditForm.getOccupation().isEmpty()) {
           user.setOccupation(userEditForm.getOccupation());
       } else {
           user.setOccupation(null);
       }

       user.setEmail(userEditForm.getEmail());

       userRepository.save(user);
   }
   
   @Transactional
   public void resetPassword(PasswordResetForm passwordResetForm) {
	   User user = userRepository.getReferenceById(passwordResetForm.getUserId());
	   
	   user.setPassword(passwordEncoder.encode(passwordResetForm.getPassword()));
	   
	   userRepository.save(user);
   }

   // メールアドレスが登録済みかどうかをチェックする
   public boolean isEmailRegistered(String email) {
       User user = userRepository.findByEmail(email);
       return user != null;
   }

   // パスワードとパスワード（確認用）の入力値が一致するかどうかをチェックする
   public boolean isSamePassword(String password, String passwordConfirmation) {
	    return Objects.equals(password, passwordConfirmation);
	}

   
   // ユーザーを有効にする
   @Transactional
   public void enableUser(User user) {
       user.setEnabled(true);
       userRepository.save(user);
   }    
   
   // メールアドレスが変更されたかどうかをチェックする
   public boolean isEmailChanged(UserEditForm userEditForm, User user) {
       return !userEditForm.getEmail().equals(user.getEmail());
   }

   // 指定したメールアドレスを持つユーザーを取得する
   public User findUserByEmail(String email) {
       return userRepository.findByEmail(email);
   } 
   
   //すべてのユーザーをページングされた状態で取得する
   public Page<User> findAllUsers(Pageable pageable) {
	   return userRepository.findAll(pageable);
   }
   
   //指定されたキーワードを氏名またはフリガナに含むユーザーを、ページングされた状態で取得する
   public Page<User> findUsersByNameLikeOrFuriganaLike(String nameKeyword, String furiganaKeyword, Pageable pageable) {
	   return userRepository.findByNameLikeOrFuriganaLike("%" + nameKeyword + "%", "%" + furiganaKeyword + "%", pageable);
   }
   
   //指定したidを持つユーザーを取得する
   public Optional<User> findUserById(Integer id) {
	   return userRepository.findById(id);
   }

	// 指定したロール名に紐づくユーザーのレコード数を取得する
	public long countUsersByRole_Name(String roleName) {
		return userRepository.countByRole_Name(roleName);
	}
	
	public Page<User> findAllNonAdminUsers(Pageable pageable) {
	    return userRepository.findByRole_IdNot(3, pageable); // role_id=3 が管理者
	}

	public Page<User> findUsersByNameLikeOrFuriganaLikeExcludingAdmin(String nameKeyword, String furiganaKeyword, Pageable pageable) {
	    return userRepository.findByNameLikeOrFuriganaLikeAndRole_IdNot(nameKeyword, furiganaKeyword, 3, pageable);
	}


   
   @Transactional
   public void saveStripeCustomerId(User user, String stripeCustomerId) {
       user.setStripeCustomerId(stripeCustomerId);
       userRepository.save(user);
   }

   @Transactional
   public void updateRole(User user, String roleName) {
       Role role = roleRepository.findByName(roleName);
       user.setRole(role);
       userRepository.save(user);
   }
   
	// 認証情報のロールを更新する
	public void refreshAuthenticationByRole(String newRole) {
		// 現在の認証情報を取得する
		Authentication currentAuthentication = SecurityContextHolder.getContext().getAuthentication();

		// 新しい認証情報を作成する
		List<SimpleGrantedAuthority> simpleGrantedAuthorities = new ArrayList<>();
		simpleGrantedAuthorities.add(new SimpleGrantedAuthority(newRole));
		Authentication newAuthentication = new UsernamePasswordAuthenticationToken(currentAuthentication.getPrincipal(),
				currentAuthentication.getCredentials(), simpleGrantedAuthorities);

		// 認証情報を更新する
		SecurityContextHolder.getContext().setAuthentication(newAuthentication);
	}
}
