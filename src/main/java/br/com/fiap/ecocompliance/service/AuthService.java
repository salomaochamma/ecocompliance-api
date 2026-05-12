package br.com.fiap.ecocompliance.service;

import br.com.fiap.ecocompliance.dto.AuthResponseDTO;
import br.com.fiap.ecocompliance.dto.LoginRequestDTO;
import br.com.fiap.ecocompliance.dto.RegisterRequestDTO;
import br.com.fiap.ecocompliance.entity.Usuario;
import br.com.fiap.ecocompliance.exception.EmailAlreadyExistsException;
import br.com.fiap.ecocompliance.repository.UsuarioRepository;
import br.com.fiap.ecocompliance.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO dto) {
        if (usuarioRepository.existsByEmail(dto.email())) {
            throw new EmailAlreadyExistsException("E-mail já cadastrado: " + dto.email());
        }

        Usuario usuario = Usuario.builder()
                .nome(dto.nome())
                .email(dto.email())
                .senha(passwordEncoder.encode(dto.senha()))
                .role(dto.role())
                .build();

        usuarioRepository.save(usuario);

        String token = jwtService.gerarToken(usuario);
        return AuthResponseDTO.of(token, usuario.getEmail(), usuario.getNome(), usuario.getRole());
    }

    public AuthResponseDTO login(LoginRequestDTO dto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.email(), dto.senha())
        );

        Usuario usuario = usuarioRepository.findByEmail(dto.email())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String token = jwtService.gerarToken(usuario);
        return AuthResponseDTO.of(token, usuario.getEmail(), usuario.getNome(), usuario.getRole());
    }
}
