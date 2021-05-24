package com.Krishna.Demo_Lambok.entity;

import lombok.Builder;
import lombok.Data;
@Builder
@Data
public class UserEntity {
    private int userId;
    private String userEmail;
    private String firstName;
    private String lastName;
    private String middleName;
    private long userMobileNo;

    public static net.bytebuddy.agent.VirtualMachine.ForOpenJ9.Dispatcher builder() {
        return null;
    }


    public String getUserId() {
    }
}
