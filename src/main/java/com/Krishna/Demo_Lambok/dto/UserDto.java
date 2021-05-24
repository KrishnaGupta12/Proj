package com.Krishna.Demo_Lambok.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {

    private int userId;
    private String userEmail;
    private String firstName;
    private String lastName;
    private String middleName;
    private long userMobileNo;

    public Object getUserId() {
    }

    public Object getUserEmail() {
    }
}
