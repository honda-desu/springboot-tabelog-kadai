package com.example.nagoyameshi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.entity.VerificationToken;
import com.example.nagoyameshi.event.SignupEventPublisher;
import com.example.nagoyameshi.form.PasswordResetForm;
import com.example.nagoyameshi.form.SendEmailInputForm;
import com.example.nagoyameshi.form.SignupForm;
import com.example.nagoyameshi.repository.UserRepository;
import com.example.nagoyameshi.service.UserService;
import com.example.nagoyameshi.service.VerificationTokenService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class AuthController {
   private final UserService userService;
   private final SignupEventPublisher signupEventPublisher;
   private final VerificationTokenService verificationTokenService;
   private final UserRepository userRepository;

   public AuthController(UserService userService, SignupEventPublisher signupEventPublisher, VerificationTokenService verificationTokenService, UserRepository userRepository) {
       this.userService = userService;        
       this.signupEventPublisher = signupEventPublisher;
       this.verificationTokenService = verificationTokenService; 
       this.userRepository = userRepository;
   }

   @GetMapping("/login")
   public String login() {
       return "auth/login";
   }

   @GetMapping("/signup")
   public String signup(Model model) {
       model.addAttribute("signupForm", new SignupForm());
       return "auth/signup";
   }

   @PostMapping("/signup")
   public String signup(@ModelAttribute @Validated SignupForm signupForm,
                        BindingResult bindingResult,
                        RedirectAttributes redirectAttributes,
                        HttpServletRequest httpServletRequest,
                        Model model)
   {
       // メールアドレスが登録済みであれば、BindingResultオブジェクトにエラー内容を追加する
       if (userService.isEmailRegistered(signupForm.getEmail())) {
           FieldError fieldError = new FieldError(bindingResult.getObjectName(), "email", "すでに登録済みのメールアドレスです。");
           bindingResult.addError(fieldError);
       }

       // パスワードとパスワード（確認用）の入力値が一致しなければ、BindingResultオブジェクトにエラー内容を追加する
       if (!userService.isSamePassword(signupForm.getPassword(), signupForm.getPasswordConfirmation())) {
           FieldError fieldError = new FieldError(bindingResult.getObjectName(), "password", "パスワードが一致しません。");
           bindingResult.addError(fieldError);
       }

       if (bindingResult.hasErrors()) {
           model.addAttribute("signupForm", signupForm);

           return "auth/signup";
       }

       User createdUser = userService.createUser(signupForm);
       String requestUrl = new String(httpServletRequest.getRequestURL());
       signupEventPublisher.publishSignupEvent(createdUser, requestUrl);
       redirectAttributes.addFlashAttribute("successMessage", "ご入力いただいたメールアドレスに認証メールを送信しました。メールに記載されているリンクをクリックし、会員登録を完了してください。");        

       return "redirect:/";
   }
   
   @GetMapping("/signup/verify")
   public String verify(@RequestParam(name = "token") String token, Model model) {
       VerificationToken verificationToken = verificationTokenService.findVerificationTokenByToken(token);

       if (verificationToken != null) {
           User user = verificationToken.getUser();
           userService.enableUser(user);
           String successMessage = "会員登録が完了しました。";
           model.addAttribute("successMessage", successMessage);
       } else {
           String errorMessage = "トークンが無効です。";
           model.addAttribute("errorMessage", errorMessage);
       }

       return "auth/verify";
   }    
   
   @GetMapping("/password/sendmail")
   public String passwordSendMail(Model model) {
	   model.addAttribute("sendEmailInputForm", new SendEmailInputForm());
	   
	   return "password/sendmail";
   }
   
   @PostMapping("/password/sendmail")
   public String passwordSendMail(@ModelAttribute @Validated SendEmailInputForm sendEmailInputForm, BindingResult bindingResult, RedirectAttributes redirectAttributes, HttpServletRequest httpServletRequest, Model model) {
	   // メールアドレスが登録されてなければ、BindingResutlオブジェクトにエラー内容を追加する
	   if (!userService.isEmailRegistered(sendEmailInputForm.getEmail())) {
		   FieldError fieldError = new FieldError(bindingResult.getObjectName(), "email", "入力されたメールアドレスは登録されていません。");
		   bindingResult.addError(fieldError);
	   }
	   
	   if (bindingResult.hasErrors()) {
		   return "password/sendmail";
	   }
	   
	   String requestUrl = new String(httpServletRequest.getRequestURL());
	   
	   signupEventPublisher.publishPasswordResetEvent(userRepository.findByEmail(sendEmailInputForm.getEmail()), requestUrl.replace("/sendmail", ""));
	   redirectAttributes.addFlashAttribute("successMessage", "ご入力いただいたメールアドレスにメールを送信しました。メールに記載されているリンクをクリックし、パスワード再設定を行ってください。");
	   
	   return "redirect:/";
   }
   
   @GetMapping("/password/verify")
   public String passwordVerify(@RequestParam(name = "token") String token, RedirectAttributes redirectAttributes, Model model) {
	   VerificationToken verificationToken = verificationTokenService.findVerificationTokenByToken(token);
	   
	   if (verificationToken == null) {
		   redirectAttributes.addFlashAttribute("errorMessage", "メールアドレスの認証が確認できませんでした。再度入力しなおしてください。");
		   return "redirect:/";
	   }
	   
	   User user = verificationToken.getUser();
	   PasswordResetForm passwordResetForm = new PasswordResetForm();
	   passwordResetForm.setUserId(user.getId());
	   
	   model.addAttribute("passwordResetForm", passwordResetForm);
	   return "password/reset";	
	   
   }
   
   @PostMapping("/password/reset")
   public String passwordReset(@ModelAttribute @Validated PasswordResetForm passwordResetForm, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
	   // パスワードとパスワード（確認用）の入力値が一致しなければ、BindingResutlオブジェクトにエラー内容を追加する
	   if(!userService.isSamePassword(passwordResetForm.getPassword(), passwordResetForm.getPasswordConfirmation())) {
		   FieldError fieldError = new FieldError(bindingResult.getObjectName(),"password", "パスワードが一致しません。");
		   bindingResult.addError(fieldError);
		   model.addAttribute("passwordResetForm", passwordResetForm);
		   return "password/reset";
	   }
	   
	   model.addAttribute("passwordResetForm", passwordResetForm);
	   userService.resetPassword(passwordResetForm);
	   
	   redirectAttributes.addFlashAttribute("successMessage", "パスワードの再設定が完了しました。");
	   
	   return "redirect:/";
   }
   
}