package com.studyolle.settings.form;

import lombok.Data;

@Data
public class ZoneForm {

    private String zoneName;

    public String getCity(String zoneName) {
        return zoneName.split("\\(")[0];
    }

    public String getLocalNameOfCity(String zoneName) {
        return zoneName.split("\\(")[1].split("\\)")[0];
    }
}
