
package com.hand.demo.model.Dtos.appuser_dtos;

import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class AppUserRegisterDTO {
    @NotBlank(message = "Username is required")
    @Email(message = "Invalid email format")
    private String username;
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$", message = "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, and one number")
    @NotBlank(message = "Password is required")
    private String password;
    private boolean deleted;
    @NotBlank(message = "Name is required")
    private String name;
    private String urlLocation;
    private String phone;
    private Set<Long> addressId;
    private UUID mediaId;
}
