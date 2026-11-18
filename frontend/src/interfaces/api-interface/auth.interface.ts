import type { Rol } from "../common";

export interface RegisterUserPayload {
    nombre:     string;
    email:      string;
    contrasena: string;
    apellido:   string;
    telefono:   string;
    codigoPais: string;
}

export interface LoginAuthUserResponse {
    token:     string;
    nombre:    string;
    apellido:  string;
    rol:       Rol;
    idUsuario: number;
}

export interface RecoveryResponse {
  message?: string;
}