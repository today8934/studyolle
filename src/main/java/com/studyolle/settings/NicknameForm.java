package com.studyolle.settings;

import com.studyolle.domain.Account;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NicknameForm {

    private String nickname;

    public NicknameForm(Account account) {
        this.nickname = account.getNickname();
    }
}
