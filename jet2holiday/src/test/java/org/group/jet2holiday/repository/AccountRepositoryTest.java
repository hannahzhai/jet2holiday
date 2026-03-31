package org.group.jet2holiday.repository;

import java.util.Optional;

import org.group.jet2holiday.entity.Account;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountRepositoryTest {

    @Mock
    private AccountRepository accountRepository;

    public AccountRepositoryTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindById() {
        //prepare
        Account account = new Account();
        account.setId(1L);
        account.setAccountName("Test Account");

        // simulate behavior
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        // execute test
        Optional<Account> result = accountRepository.findById(1L);

        // verify result
        assertTrue(result.isPresent());
        assertEquals("Test Account", result.get().getAccountName());

        // verify method call
        verify(accountRepository, times(1)).findById(1L);
    }
}