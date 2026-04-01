//package org.group.jet2holiday.config;
//
//
//import org.group.jet2holiday.entity.Account;
//import org.group.jet2holiday.repository.AccountRepository;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.math.BigDecimal;
//
//@Configuration
//public class DataInitializer {
//
//    @Bean
//    public CommandLineRunner initDefaultAccount(AccountRepository accountRepository) {
//        // test mvp, init default account if not exist
//        return args -> {
//            if (accountRepository.count() == 0) {
//                Account defaultAccount = new Account();
//                defaultAccount.setAccountName("Default Portfolio Account");
//                defaultAccount.setCashBalance(new BigDecimal("10000.0000"));
//                defaultAccount.setCurrency("USD");
//
//                accountRepository.save(defaultAccount);
//            }
//        };
//    }
//}