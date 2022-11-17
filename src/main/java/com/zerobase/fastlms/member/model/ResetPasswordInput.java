package com.zerobase.fastlms.member.model;

import lombok.Data;

@Data
public class ResetPasswordInput {
    private String userId;
    private String userName;

    private String id;
    private String password;
}
