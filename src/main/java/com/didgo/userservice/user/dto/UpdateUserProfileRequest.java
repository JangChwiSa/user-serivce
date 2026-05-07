package com.didgo.userservice.user.dto;

import com.didgo.userservice.user.domain.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(
        @NotBlank(message = "?대쫫? ?꾩닔?낅땲??")
        @Size(max = 100, message = "?대쫫? 100???댄븯?ъ빞 ?⑸땲??")
        String name,

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
