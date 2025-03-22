package com.example.user_auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import com.example.user_auth.repository.UserTokenRepository;
import com.example.user_auth.model.UserToken;
import java.util.Date;
import org.springframework.stereotype.Component;


@Component
public class TokenUtils {

    @Autowired
    private UserTokenRepository userTokenRepository;

    public boolean isTokenValid(String token) {
        UserToken userToken = userTokenRepository.findByToken(token);
        if (userToken == null) {
            return false;
        }
        Date expirationDate = userToken.getExpirationDate();
        return expirationDate != null && expirationDate.after(new Date());
    }
}
