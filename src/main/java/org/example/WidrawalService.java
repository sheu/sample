package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

public class WithdrawalService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private SnsClient snsClient;

    public WithdrawalService() {
        this.snsClient = SnsClient.builder()
                .region(Region.US_WEST_1) // Specify your region
                .build();
    }

   // Consider using some form of locking.  I will go with distributed locking using Redis or zookeeper
    public String withdraw(final long accountId, final BigDecimal amount) {
        final String response;

        if(amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            response = String.format("Invalid amount: %s", amount);
        } else {
            // Check current balance
            // Acquire lock
            final String getBalanceSql = "SELECT balance FROM accounts WHERE id = ?";
            BigDecimal currentBalance = jdbcTemplate.queryForObject(
                    getBalanceSql, new Object[]{accountId}, BigDecimal.class);


            if (currentBalance != null && currentBalance.compareTo(amount) >= 0) {
                // Update balance
                final String updateBalanceSql = "UPDATE accounts SET balance = balance - ? WHERE id = ?";
                int rowsAffected = jdbcTemplate.update(updateBalanceSql, amount, accountId);
                if (rowsAffected == 1) {
                    response = "Withdrawal successful";
                } else {
                    // In case the update fails for reasons other than a balance check
                    response = "Withdrawal failed";
                }
            } else {
                // Insufficient funds
                response = "Insufficient funds for withdrawal";
            }
            // Release lock
        }

        CompletableFuture.runAsync(() -> publicWithdrawalEvent(response, amount, accountId));

        return response;
    }

    private void publicWithdrawalEvent(final String response, final BigDecimal amount, final Long accountId) {
        WithdrawalEvent event = new WithdrawalEvent(amount, accountId, response);
        String eventJson = event.toJson(); // Convert event to JSON
        String snsTopicArn = "arn:aws:sns:YOUR_REGION:YOUR_ACCOUNT_ID:YOUR_TOPIC_NAME";
        PublishRequest publishRequest = PublishRequest.builder()
                .message(eventJson)
                .topicArn(snsTopicArn)
                .build();

        try {
            PublishResponse publishResponse = snsClient.publish(publishRequest);
            // handle the response, emit metric, etc.
        } catch (Exception e) {
            // log error, emit metric, etc.
        }

    }
}

class WithdrawalEvent {
    private BigDecimal amount;
    private Long accountId;
    private String status;

    public WithdrawalEvent(BigDecimal amount, Long accountId, String status) {
        this.amount = amount;
        this.accountId = accountId;
        this.status = status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getStatus() {
        return status;
    }

    // Convert to JSON String
    public String toJson() {
        return String.format("{\"amount\":\"%s\",\"accountId\":%d,\"status\":\"%s\"}",
                amount, accountId, status);
    }


}

