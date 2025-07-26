package com.example.nagoyameshi.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.nagoyameshi.entity.RegularHoliday;
import com.example.nagoyameshi.repository.RegularHolidayRepository;

@Service
public class RegularHolidayService {
   private final RegularHolidayRepository regularHolidayRepository;

   public RegularHolidayService(RegularHolidayRepository regularHolidayRepository) {
       this.regularHolidayRepository = regularHolidayRepository;
   }

   // 指定したidを持つ定休日を取得する
   public Optional<RegularHoliday> findRegularHolidayById(Integer id) {
       return regularHolidayRepository.findById(id);
   }

   // すべての定休日をリスト形式で取得する
   public List<RegularHoliday> findAllRegularHolidays() {
       return regularHolidayRepository.findAll();
   }
}