package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/bank")
public class BankAccountController {


    @Autowired
    private WithdrawalService withdrawalService;


    @PostMapping("/withdraw")
    public String withdraw(@RequestParam("accountId") final Long accountId,
                           @RequestParam("amount") final BigDecimal amount) {
       return withdrawalService.withdraw(accountId, amount);
    }
}