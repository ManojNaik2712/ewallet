package com.example.transaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import static com.example.utils.CommonConstants.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

@Slf4j
@Service
public class TransactionService {
    @Autowired
    KafkaTemplate kafkaTemplate;
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    TransactionRepository transactionRepository;

    private final Map<String, CompletableFuture<String>> pendingRequests = new ConcurrentHashMap<>();

    public String transact(String sender, String reciever, Double amount, String reason) throws JsonProcessingException {
        Transaction transaction=Transaction.builder()
                .senderId(sender)
                .receiverId(reciever)
                .transactionId(UUID.randomUUID().toString()).amount(amount)
                .reason(reason)
                .transactionStatusEnum(TransactionStatusEnum.PENDING).build();
        transactionRepository.save(transaction);

        CompletableFuture<String> future=new CompletableFuture<>();
        pendingRequests.put(transaction.getTransactionId(),future);

        log.info("sending a request to kafka");
        JSONObject jsonObject=new JSONObject();
        jsonObject.put(TRANSACTION_CREATED_TOPIC_SENDER,sender);
        jsonObject.put(TRANSACTION_CREATED_TOPIC_RECEIVER,reciever);
        jsonObject.put(TRANSACTION_CREATED_TOPIC_AMOUNT,amount);
        jsonObject.put(TRANSACTION_CREATED_TOPIC_TRANSACTION_ID,transaction.getTransactionId());

        kafkaTemplate.send(TRANSACTION_CREATED_TOPIC,objectMapper.writeValueAsString(jsonObject));

        try{
            String response= future.get(60, TimeUnit.MINUTES);
            return response;
        } catch (Exception e){
            pendingRequests.remove(transaction.getTransactionId());
            throw new RuntimeException("wallet service timeout",e);
        }

    }
    @KafkaListener(topics = WALLET_UPDATED_TOPIC,groupId = "ewallet_group")
    public void getTransactionupdate(String message) throws ParseException {
        JSONObject data= (JSONObject) new JSONParser().parse(message);
        String response= (String) data.get(WALLET_UPDATED_TOPIC_STATUS);
        String transactionId= (String) data.get(TRANSACTION_CREATED_TOPIC_TRANSACTION_ID);
        CompletableFuture<String> future=pendingRequests.remove(transactionId);
        if(future != null) {
            future.complete(response);
            log.info("future is completed");
        }
    }
}
