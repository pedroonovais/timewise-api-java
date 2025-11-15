package com.timewise.timewise.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para requisições de criação e atualização de usuários
 * Não inclui o ID, pois é gerado automaticamente
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UsuarioRequestDTO {

    @NotBlank(message = "{usuario.request.nome.notblank}")
    private String nome;

    @NotBlank(message = "{usuario.request.email.notblank}")
    @Email(message = "{usuario.request.email.email.invalid}")
    private String email;

    @NotBlank(message = "{usuario.request.senha.notblank}")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "{usuario.request.senha.pattern}"
    )
    private String senha;
}

