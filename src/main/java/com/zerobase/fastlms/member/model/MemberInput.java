package com.zerobase.fastlms.member.model;

import lombok.Data;

@Data
public class MemberInput {
    private String userId;
    private String userName;
    private String password;
    private String phone;
}
