package com.pm.jujutsu.dtos;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDTO {


    private  String accessToken;
    private String refreshToken;




}
