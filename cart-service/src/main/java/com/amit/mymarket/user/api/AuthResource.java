package com.amit.mymarket.user.api;

import com.amit.mymarket.user.api.web.RegisterForm;
import com.amit.mymarket.user.service.RegistrationService;
import com.amit.mymarket.user.service.exception.UsernameAlreadyExistsException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Controller
public class AuthResource {

    private final RegistrationService registrationService;

    @Autowired
    public AuthResource(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/login")
    public Mono<Rendering> loginPage(@RequestParam(name = "error", required = false) String error,
                                     @RequestParam(name = "logout", required = false) String logout,
                                     @RequestParam(name = "registered", required = false) String registered) {
        return Mono.just(
                Rendering.view("auth/login")
                        .modelAttribute("showError", error != null)
                        .modelAttribute("showLogout", logout != null)
                        .modelAttribute("showRegistered", registered != null)
                        .build()
        );
    }

    @GetMapping(value = "/register")
    public Mono<Rendering> registerPage() {
        return Mono.just(Rendering.view("auth/register")
                .modelAttribute("form", RegisterForm.EMPTY())
                .build());
    }

    @PostMapping(value = "/register")
    public Mono<Rendering> register(@Valid @ModelAttribute(value = "form") RegisterForm form, BindingResult bindingResult) {
        if (!form.password().equals(form.confirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "mismatch", "Пароли не совпадают");
        }
        if (bindingResult.hasErrors()) {
            return Mono.just(Rendering.view("auth/register").modelAttribute("form", form).build());
        }
        return this.registrationService.registerUser(form.username(), form.password())
                .thenReturn(Rendering.redirectTo("/login?registered").build())
                .onErrorResume(UsernameAlreadyExistsException.class, exception ->
                        Mono.just(Rendering.view("auth/register")
                                .modelAttribute("form", form)
                                .modelAttribute("error", "Пользователь с таким логином уже существует")
                                .build()
                        )
                );
    }

}
