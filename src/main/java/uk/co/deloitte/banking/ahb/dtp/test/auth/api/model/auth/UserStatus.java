package uk.co.deloitte.banking.ahb.dtp.test.auth.api.model.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.co.deloitte.alpha.error.exception.InternalServerException;

import java.util.Arrays;

import static org.apache.commons.lang3.StringUtils.isBlank;

@AllArgsConstructor
@Getter
public enum UserStatus {
    PENDING("PENDING"),
    REGISTRATION("REGISTRATION"),
    CUSTOMER("CUSTOMER"),
    ACCOUNTS("ACCOUNTS CUSTOMER");


    @JsonProperty("Description")
    private final String description;

    public static UserStatus from(String description) {
        if (isBlank(description)) {
            throw new InternalServerException("User status not recognised");
        }
        return Arrays.stream(UserStatus.values()).filter(value -> description.equalsIgnoreCase(value.getDescription()))
                .findAny().orElseThrow(() -> new InternalServerException("User status not recognised"));
    }

}
