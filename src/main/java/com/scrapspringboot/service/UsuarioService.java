package com.scrapspringboot.service;

import com.scrapspringboot.dto.*;
import com.scrapspringboot.exception.RecursoNoEncontrado;
import com.scrapspringboot.model.Perfil;
import com.scrapspringboot.model.Usuario;
import com.scrapspringboot.model.VerificationToken; // Import new model
import com.scrapspringboot.repository.PerfilRepository;
import com.scrapspringboot.repository.UsuarioRepository;
import com.scrapspringboot.repository.VerificationTokenRepository; // Import new repository
import com.scrapspringboot.security.JWTService;
import lombok.AllArgsConstructor; // Keep or replace with @RequiredArgsConstructor if fields are final
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import Transactional
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;


@Service
@Validated
@AllArgsConstructor
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PerfilService perfilService;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final PerfilRepository perfilRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        return usuarioRepository.findTopByUsername(username).orElseThrow(() ->
                new RecursoNoEncontrado("Usuario no encontrado con el nombre: " + username));
    }

    public UsuarioDTO obtenerUsuarioPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id).orElseThrow(() ->
                new RecursoNoEncontrado("Usuario no encontrado con el id: " + id));

        Perfil perfil = perfilRepository.findByUsuarioId(usuario.getId());

        UsuarioDTO usuarioDTO = new UsuarioDTO();
        usuarioDTO.setId(usuario.getId());
        usuarioDTO.setUsername(usuario.getUsername());
        usuarioDTO.setEmail(usuario.getEmail());
        usuarioDTO.setRol(usuario.getRol().name());


        return usuarioDTO;
    }

    @Transactional
    public Usuario registrarUsuario(RegistroDTO dto) {
        if (usuarioRepository.findTopByUsername(dto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("El nombre de usuario '" + dto.getUsername() + "' ya está en uso.");
        }
        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("El email '" + dto.getEmail() + "' ya está en uso.");
        }

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setEmail(dto.getEmail());
        nuevoUsuario.setUsername(dto.getUsername());
        nuevoUsuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        nuevoUsuario.setRol(dto.getRol());

        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);


        PerfilDTO perfilDTO = new PerfilDTO();
        perfilDTO.setNombre(dto.getNombre());
        perfilDTO.setImagen(dto.getImagen());
        perfilDTO.setBaneado(false);
        perfilDTO.setActivado(false); //
        perfilDTO.setUsuario(usuarioGuardado.getId());

        perfilService.guardar(perfilDTO, usuarioGuardado.getId());

        String tokenString = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(tokenString, usuarioGuardado);
        verificationTokenRepository.save(verificationToken);


        emailService.sendVerificationEmail(
                usuarioGuardado.getEmail(),
                usuarioGuardado.getUsername(),
                tokenString
        );

        return usuarioGuardado;
    }

    @Transactional
    public String verifyUser(String token) {
        Optional<VerificationToken> verificationTokenOpt = verificationTokenRepository.findByToken(token);

        if (verificationTokenOpt.isEmpty()) {
            return "Token de verificación inválido.";
        }

        VerificationToken verificationToken = verificationTokenOpt.get();

        if (verificationToken.isExpired()) {
            return "Token de verificación ha expirado.";
        }

        Usuario usuario = verificationToken.getUsuario();
        Perfil perfil = perfilRepository.findByUsuarioId(usuario.getId());

        if (perfil == null) {
            return "Error: Perfil de usuario no encontrado.";
        }

        if (perfil.isActivado()) {
            return "Esta cuenta ya ha sido activada.";
        }

        perfil.setActivado(true);
        perfilRepository.save(perfil);

        verificationTokenRepository.delete(verificationToken);

        return "Cuenta activada exitosamente. Ahora puedes iniciar sesión.";
    }


    public ResponseEntity<RespuestaDTO> login(LoginDTO dto) {
        Optional<Usuario> usuarioOpcional = usuarioRepository.findTopByUsername(dto.getUsername());

        if (usuarioOpcional.isPresent()) {
            Usuario usuario = usuarioOpcional.get();
            Perfil perfil = perfilRepository.findByUsuarioId(usuario.getId());

            if (perfil == null) {
                throw new BadCredentialsException("Error de cuenta. Contacte al administrador.");
            }
            if (!perfil.isActivado()) {
                throw new BadCredentialsException("La cuenta no está activada. Por favor, revisa tu correo electrónico para el enlace de activación.");
            }

            if (perfil.isBaneado()) {
                throw new BadCredentialsException("Esta cuenta ha sido baneada.");
            }

            if (passwordEncoder.matches(dto.getPassword(), usuario.getPassword())) {
                String token = jwtService.generateToken(usuario);
                return ResponseEntity
                        .ok(RespuestaDTO
                                .builder()
                                .estado(HttpStatus.OK.value())
                                .token(token).build());
            } else {
                throw new BadCredentialsException("Nombre de usuario o contraseña incorrecta.");
            }
        } else {
            throw new BadCredentialsException("Nombre de usuario o contraseña incorrecta.");
        }
    }


}