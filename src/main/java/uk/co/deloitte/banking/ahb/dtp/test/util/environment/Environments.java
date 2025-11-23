package uk.co.deloitte.banking.ahb.dtp.test.util.environment;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum Environments {

    ALL("all", "all"),
    NONE("none", "none"),
    DEV("dev", "dev"),
    CIT("cit", "cit"),
    SIT("sit", "sit"),
    NFT("nft", "nft"),
    STG("stg", "stg");

    @Getter
    private String text;

    @Getter
    private String value;
}
