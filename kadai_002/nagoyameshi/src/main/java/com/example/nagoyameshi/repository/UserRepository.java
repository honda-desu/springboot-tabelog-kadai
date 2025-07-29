package com.example.nagoyameshi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nagoyameshi.entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {
	public User findByEmail(String email);
	public Page<User> findByNameLikeOrFuriganaLike(String nameKeyword, String furiganaKeyword, Pageable pageable);
	public long countByRole_Name(String roleName);
	public Page<User> findByRole_IdNot(Integer excludedRoleId, Pageable pageable);
	public Page<User> findByNameLikeOrFuriganaLikeAndRole_IdNot(String name, String furigana, Integer excludedRoleId, Pageable pageable);

	
}
