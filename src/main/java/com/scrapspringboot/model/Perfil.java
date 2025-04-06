package com.scrapspringboot.model;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "perfil", schema = "indexer")
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@ToString

public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "imagen")
    private String imagen;

    @Column(name = "activado")
    private boolean activado;

    @Column(name = "baneado")
    private boolean baneado;

    @OneToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;
}


