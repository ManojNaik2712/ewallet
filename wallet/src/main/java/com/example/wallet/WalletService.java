package com.example.wallet;

import com.example.utils.UserIdentifier;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import static com.example.utils.CommonConstants.*;

@Service
public class WalletService {
    @Autowired
    WalletRepository walletRepository;

    @Autowired
    KafkaTemplate kafkaTemplate;

    @Autowired
    ObjectMapper objectMapper;

    private static Logger logger = LoggerFactory.getLogger(WalletService.class);

    @KafkaListener(topics = USER_CREATED_TOPIC,groupId = "ewallet_group")
    public void createWallet(String message) throws ParseException {
        logger.debug("In WalletService.createWallet with message: {}", message);
        JSONObject data= (JSONObject) new JSONParser().parse(message);

        long UserId= (long) data.get(USER_CREATED_TOPIC_USER_ID);
        String phoneNumber = (String) data.get(USER_CREATED_TOPIC_PHONE_NUMBER);
        String identifierKey = (String) data.get(USER_CREATED_TOPIC_IDENTIFIER_KEY);
        String identifierValue = (String) data.get(USER_CREATED_TOPIC_IDENTIFIER_VALUE);

        Wallet wallet=Wallet.builder()
                .userId(UserId)
                .phoneNumber(phoneNumber)
                .balance(20.0)
                .userIdentifier(UserIdentifier.valueOf(identifierKey))
                .identifierValue(identifierValue)
                .build();
        walletRepository.save(wallet);
    }
    @KafkaListener(topics = TRANSACTION_CREATED_TOPIC,groupId = "ewallet_group")
    public void updateWalletForTransaction(String message) throws ParseException, JsonProcessingException {
        JSONObject data= (JSONObject) new JSONParser().parse(message);

        String senderId= (String) data.get(TRANSACTION_CREATED_TOPIC_SENDER);
        String receiverId= (String) data.get(TRANSACTION_CREATED_TOPIC_RECEIVER);
        Double amount= (Double) data.get(TRANSACTION_CREATED_TOPIC_AMOUNT);
        String transactionId= (String) data.get(TRANSACTION_CREATED_TOPIC_TRANSACTION_ID);

        Wallet senderWallet=walletRepository.findByPhoneNumber(senderId);
        Wallet receiverWallet=walletRepository.findByPhoneNumber(receiverId);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TRANSACTION_CREATED_TOPIC_SENDER, senderId);
        jsonObject.put(TRANSACTION_CREATED_TOPIC_RECEIVER, receiverId);
        jsonObject.put(TRANSACTION_CREATED_TOPIC_AMOUNT, amount);
        jsonObject.put(TRANSACTION_CREATED_TOPIC_TRANSACTION_ID, transactionId);

        if(senderWallet ==null || receiverWallet == null ||
        senderWallet.getBalance() < amount){
            jsonObject.put(WALLET_UPDATED_TOPIC_STATUS,WALLET_UPDATED_STATUS_FAILED);
        }
        else {
            walletRepository.updateWallet(senderId,0 - amount);
            walletRepository.updateWallet(receiverId,amount);
            jsonObject.put(WALLET_UPDATED_TOPIC_STATUS,WALLET_UPDATED_STATUS_SUCCESS);

        }
        kafkaTemplate.send(WALLET_UPDATED_TOPIC,objectMapper.writeValueAsString(jsonObject));
        logger.debug("Out WalletService.updateWalletForTransaction method");
    }

}
