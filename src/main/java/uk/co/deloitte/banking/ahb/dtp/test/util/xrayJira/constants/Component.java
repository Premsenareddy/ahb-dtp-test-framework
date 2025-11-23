package uk.co.deloitte.banking.ahb.dtp.test.util.xrayJira.constants;


public enum Component {

    ANDROID("Android"),
    CORE_BANKING("Core Banking"),
    CRM("CRM"),
    DTP("Digital Technology Platform"),
    EXPERIENCE_TECHNOLOGY("Experience Technology"),
    IOS("iOS");

    private String component;


    private Component(String component) {
        this.component = component;
    }

    public String getComponent() {
        return this.component;
    }



}
