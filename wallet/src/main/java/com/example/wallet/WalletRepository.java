package com.example.wallet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletRepository extends JpaRepository<Wallet,Integer> {

    Wallet findByPhoneNumber(String senderId);

    @Query("UPDATE Wallet w SET w.balance = w.balance + ?2 WHERE w.phoneNumber = ?1")
    void updateWallet(String Id, Double amount);
}
