package uk.co.deloitte.banking.ahb.dtp.test.cards.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@lombok.Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class ReadCardDetails {
    private Data data;
}
