package com.pm.jujutsu.utils;


import com.pm.jujutsu.model.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class Encoder{
    PasswordEncoder passwordEncoder =  new BCryptPasswordEncoder();


    public  String encode(String password){
         return  passwordEncoder.encode(password);
    }


    public  boolean decode(String password, User user){
        return passwordEncoder.matches(password,passwordEncoder.encode(password));
    }
}
