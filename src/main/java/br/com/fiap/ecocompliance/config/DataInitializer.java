package br.com.fiap.ecocompliance.config;

import br.com.fiap.ecocompliance.entity.Role;
import br.com.fiap.ecocompliance.entity.Usuario;
import br.com.fiap.ecocompliance.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Cria os usuários iniciais (ADMIN e USER) caso ainda não existam.
 * Esta abordagem garante que as senhas em BCrypt sejam geradas pelo
 * PasswordEncoder do Spring, evitando hashes inválidos no SQL.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        criarUsuarioSeNaoExistir(
                "Administrador EcoCompliance",
                "admin@ecocompliance.com",
                "admin123",
                Role.ADMIN
        );
        criarUsuarioSeNaoExistir(
                "Usuário Padro",
                "user@ecocompliance.com",
                "user123",
                Role.USER
        );
    }

    private void criarUsuarioSeNaoExistir(String nome, String email, String senha, Role role) {
        if (usuarioRepository.existsByEmail(email)) {
            log.info("Usuário já existe: {}", email);
            return;
        }

        Usuario usuario = Usuario.builder()
                .nome(nome)
                .email(email)
                .senha(passwordEncoder.encode(senha))
                .role(role)
                .build();

        usuarioRepository.save(usuario);
        log.info("Usuário inicial criado: {} ({})", email, role);
    }
}
