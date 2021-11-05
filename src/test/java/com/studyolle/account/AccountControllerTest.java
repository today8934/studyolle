package com.studyolle.account;

import com.studyolle.WithAccount;
import com.studyolle.domain.Account;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountService accountService;

    @MockBean
    JavaMailSender javaMailSender;

    @DisplayName("인증 메일 확인 - 입력값 오류")
    @Test
    void checkEmailToken_with_wrong_input() throws Exception {
        mockMvc.perform(get("/check-email-token")
                .param("token", "asdfasdf")
                .param("email", "email@email.com"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(view().name("account/checked-email"))
                .andExpect(unauthenticated());
    }

    @DisplayName("인증 메일 확인 - 입력값 정상")
    @Test
    void checkEmailToken() throws Exception {
        Account account = Account.builder()
                .email("today8934@gmail.com")
                .password("q1w2e3r4t5!")
                .nickname("woo")
                .build();

        Account newAccount = accountRepository.save(account);
        newAccount.generateEmailCheckToken();

        mockMvc.perform(get("/check-email-token")
                        .param("token", newAccount.getEmailCheckToken())
                        .param("email", newAccount.getEmail()))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(model().attributeExists("nickname"))
                .andExpect(model().attributeExists("numberOfUser"))
                .andExpect(view().name("account/checked-email"))
                .andExpect(authenticated());
    }

    @DisplayName("회원 가입 화면 보이는지 테스트")
    @Test
    void signUpForm() throws Exception {
        mockMvc.perform(get("/sign-up"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(model().attributeExists("signUpForm"));
    }

    @DisplayName("회원 가입 처리 - 입력값 오류")
    @Test
    void signUpSubmit_with_wrong_input() throws Exception  {
        mockMvc.perform(post("/sign-up")
                .param("nickname", "wooksang")
                .param("email", "today8934@email.com")
                .param("password", "1234567891")
                        .with(csrf())) // 타임리프 템플릿에서 csrf검증을 위해 같이 보내주는 토큰이 없으므로 해당 메소드 추가해준다.
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"));

        Account account = accountRepository.findByEmail("today8934@email.com");
        assertNotNull(account);
        assertNotEquals(account.getPassword(), "1234567891");
        assertNotNull(account.getEmailCheckToken());
        assertTrue(accountRepository.existsByEmail("today8934@email.com"));
        then(javaMailSender).should().send(any(SimpleMailMessage.class));
    }

    @DisplayName("이메일로 로그인 - 메일보내기")
    @Test
    @WithAccount("wook")
    void sendLoginEmail() throws Exception {
        String email = "today8934@gmail.com";

        mockMvc.perform(post("/email-login")
                .param("email", email)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/email-login"))
                .andExpect(flash().attributeExists("message"));

        String emailLoginToken = accountRepository.findByEmail(email).getEmailLoginToken();

        System.out.println("emailLoginToken = " + emailLoginToken);
        assertNotNull(emailLoginToken);
    }

    @DisplayName("이메일로 로그인 - 메일 보내기 실패")
    @Test
    @WithAccount("wook")
    void sendLoginEmail_error() throws Exception {
        String email = "abcde@gmail.com";

        mockMvc.perform(post("/email-login")
                .param("email", email)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/email-login"))
                .andExpect(flash().attributeExists("error"));
    }

    @DisplayName("이메일로 로그인 - 로그인 성공")
    @Test
    void loginByEmail() throws Exception {
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname("wook");
        signUpForm.setEmail("today8934@gmail.com");
        signUpForm.setPassword("12345678");

        Account account = accountService.processNewAccount(signUpForm);

        account.generateEmailLoginToken();

        mockMvc.perform(get("/email-login-token")
                .param("token", account.getEmailLoginToken())
                .param("email", account.getEmail())
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated());
    }

    @DisplayName("이메일로 로그인 - 로그인 실패")
    @Test
    void loginByEmail_error() throws Exception {
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname("wook");
        signUpForm.setEmail("today8934@gmail.com");
        signUpForm.setPassword("12345678");

        Account account = accountService.processNewAccount(signUpForm);

        account.generateEmailLoginToken();

        //토큰이 다를때
        mockMvc.perform(get("/email-login-token")
                .param("token", "12345")
                .param("email", account.getEmail())
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/email-login"))
                .andExpect(unauthenticated());

        //이메일이 다를때
        mockMvc.perform(get("/email-login-token")
                        .param("token", account.getEmailLoginToken())
                        .param("email", "ggg@gmail.com")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/email-login"))
                .andExpect(unauthenticated());
    }

    @DisplayName("이메일로 로그인 - 이메일 토큰으로 한번이상 로그인 시")
    @Test
    void loginByEmail_more_than_once() throws Exception {
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname("wook");
        signUpForm.setEmail("today8934@gmail.com");
        signUpForm.setPassword("12345678");

        Account account = accountService.processNewAccount(signUpForm);

        account.generateEmailLoginToken();

        String token = account.getEmailLoginToken();

        mockMvc.perform(get("/email-login-token")
                        .param("token", token)
                        .param("email", account.getEmail())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated());

        mockMvc.perform(get("/email-login-token")
                .param("token", token)
                .param("email", account.getEmail())
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/email-login"))
                .andExpect(flash().attributeExists("error"))
                .andExpect(unauthenticated());
    }
}