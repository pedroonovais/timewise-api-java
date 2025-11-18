package com.timewise.timewise.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "{usuario.nome.notblank}")
    private String nome;

    @NotBlank(message = "{usuario.email.notblank}")
    @Email(message = "{usuario.email.email.invalid}")
    private String email;

    @NotBlank(message = "{usuario.senha.notblank}")
    // Nota: Validação de padrão (@Pattern) removida porque a senha é criptografada com BCrypt antes de salvar
    // A validação do padrão é feita no DTO (UsuarioRequestDTO) onde a senha ainda está em texto plano
    private String senha;
    
}
