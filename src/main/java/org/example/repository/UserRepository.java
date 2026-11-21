package org.example.repository;

import jakarta.persistence.LockModeType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.example.repository.Entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    boolean existsByEmail(@NotBlank @Email String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from UserEntity u where u.email = :email")
    Optional<UserEntity> findByEmailForCartUpdate(@NotBlank String email);
    //Заблочили пользователя пока он выполняет одну операцию в своей корзине. убрали конкурентную запись

    Optional<UserEntity> findByEmail(@NotBlank @Email String email);
}
