package com.studyolle.settings.form;

import lombok.Data;

@Data
public class ZoneForm {

    private String zoneName;

    public String getCity() {
        return this.zoneName.split("\\(")[0];
    }

    public String getLocalNameOfCity() {
        return this.zoneName.split("\\(")[1].split("\\)/")[0];
    }

    public String getProvince() {
        return this.zoneName.split("\\(")[1].split("\\)/")[1];
    }
}
