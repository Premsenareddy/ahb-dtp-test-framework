package uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Bharat Gatty on 04/02/2021
 *
 * TODO:: Use the User object
 */

@Deprecated
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSetAccountPasscodeRequest {
    private String password;
    private String phoneNumber;

}
