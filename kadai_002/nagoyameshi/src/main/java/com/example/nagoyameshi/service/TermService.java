package com.example.nagoyameshi.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.Term;
import com.example.nagoyameshi.form.TermEditForm;
import com.example.nagoyameshi.repository.TermRepository;

@Service
public class TermService {
   private final TermRepository termRepository;

   public TermService(TermRepository termRepository) {
       this.termRepository = termRepository;
   }

   // idが最も大きい利用規約を取得する
   public Term findFirstTermByOrderByIdDesc() {
       return termRepository.findFirstByOrderByIdDesc();
   }

   @Transactional
   public void updateTerm(TermEditForm termEditForm, Term term) {
       term.setContent(termEditForm.getContent());

       termRepository.save(term);
   }
}