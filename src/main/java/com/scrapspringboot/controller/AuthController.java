package com.scrapspringboot.controller;

import com.scrapspringboot.dto.*;
import com.scrapspringboot.dto.RespuestaDTO;
import com.scrapspringboot.exception.RecursoNoEncontrado;
import com.scrapspringboot.model.Usuario;
import com.scrapspringboot.service.UsuarioService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;

    @PostMapping("/registro")
    public ResponseEntity<?> registro(@RequestBody RegistroDTO registroDTO){
        try {
            Usuario usuario = usuarioService.registrarUsuario(registroDTO);

            String message = "Registro exitoso para el usuario: " + usuario.getUsername() +
                    ". Por favor, revisa tu email (" + usuario.getEmail() +
                    ") para activar tu cuenta.";
            return ResponseEntity.status(HttpStatus.CREATED).body(message);
        } catch (IllegalArgumentException e) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error durante el registro.");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<RespuestaDTO> login(@RequestBody LoginDTO dto){

        try {
            return usuarioService.login(dto);
        } catch (BadCredentialsException e) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(RespuestaDTO.builder()
                            .estado(HttpStatus.UNAUTHORIZED.value())
                            .mensaje(e.getMessage())
                            .build());
        } catch (RecursoNoEncontrado e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(RespuestaDTO.builder()
                            .estado(HttpStatus.NOT_FOUND.value())
                            .mensaje("Usuario no encontrado.")
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(RespuestaDTO.builder()
                            .estado(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .mensaje("Error interno del servidor durante el login.")
                            .build());
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyAccount(@RequestParam("token") String token) {
        try {
            String result = usuarioService.verifyUser(token);
            if (result.startsWith("Cuenta activada")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error durante la verificación de la cuenta.");
        }
    }
}