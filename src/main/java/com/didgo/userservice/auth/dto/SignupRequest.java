package com.didgo.userservice.auth.dto;

import com.didgo.userservice.user.domain.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record SignupRequest(
        @NotBlank(message = "?꾩씠?붾뒗 ?꾩닔?낅땲??")
        @Size(max = 50, message = "?꾩씠?붾뒗 50???댄븯?ъ빞 ?⑸땲??")
        String loginId,

        @NotBlank(message = "鍮꾨?踰덊샇???꾩닔?낅땲??")
        @Size(min = 8, max = 100, message = "鍮꾨?踰덊샇??8???댁긽 100???댄븯?ъ빞 ?⑸땲??")
        String password,

        @NotBlank(message = "?대쫫? ?꾩닔?낅땲??")
        @Size(max = 100, message = "?대쫫? 100???댄븯?ъ빞 ?⑸땲??")
        String name,

        @NotNull(message = "?앸뀈?붿씪? ?꾩닔?낅땲??")
        LocalDate birthDate,

        @NotNull(message = "?깅퀎? ?꾩닔?낅땲??")
        Gender gender,

        @NotBlank(message = "?대찓?쇱? ?꾩닔?낅땲??")
        @Email(message = "?대찓???뺤떇???щ컮瑜댁? ?딆뒿?덈떎.")
        String email,

        @NotBlank(message = "?щ쭩 吏곷Т???꾩닔?낅땲??")
        @Size(max = 100, message = "?щ쭩 吏곷Т??100???댄븯?ъ빞 ?⑸땲??")
        String desiredJob
) {
}
