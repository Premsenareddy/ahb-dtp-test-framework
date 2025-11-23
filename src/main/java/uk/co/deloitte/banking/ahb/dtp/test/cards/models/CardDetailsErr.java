package uk.co.deloitte.banking.ahb.dtp.test.cards.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class CardDetailsErr {

        private String Code;
        private String Message;
}
