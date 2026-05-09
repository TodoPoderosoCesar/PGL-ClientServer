# Cifrado AES en la aplicación STOP Online

## Clases nuevas (`net.salesianos.utils`)

| Clase | Responsabilidad |
|---|---|
| `SecureManager` | Genera/carga la clave AES y expone `encrypt(byte[])` / `decrypt(byte[])` |
| `FileManager` | Persiste la clave AES en el fichero `./secret` |
| `EncryptedObjectOutputStream` | Serializa un objeto `Serializable`, lo cifra con AES y lo envía con una cabecera de 4 bytes de longitud |
| `EncryptedObjectInputStream` | Lee la cabecera, lee N bytes cifrados, los descifra y deserializa el objeto |

## Protocolo de trama

```
┌──────────────┬─────────────────────────────┐
│  4 bytes     │  N bytes                    │
│  (int, BE)   │  datos AES cifrados         │
│  = longitud  │  (objeto Message serializado)│
└──────────────┴─────────────────────────────┘
```

## Clases modificadas

### `ClientHandler.java` (servidor)
- Sustituye `ObjectOutputStream` / `ObjectInputStream` por `EncryptedObjectOutputStream` / `EncryptedObjectInputStream`.
- El `SecureManager` lee la clave del fichero `./secret`.

### `Client.java` (cliente)
- Mismo cambio en `connect()`: streams cifrados.
- Pasa `EncryptedObjectInputStream` al `ServerListener`.

### `ServerListener.java` (cliente)
- Recibe `EncryptedObjectInputStream` en lugar de `ObjectInputStream`.

## Cómo compartir la clave

1. **Primera ejecución del servidor**: si no existe `./secret`, `SecureManager` genera una clave AES de 128 bits y la guarda.
2. **Copiar** el fichero `secret` al directorio de trabajo del cliente (o al raíz del proyecto si ambos corren en la misma máquina).
3. A partir de ahí, cliente y servidor usan la misma clave → pueden comunicarse.

## Esquema de seguridad basado en roles (escalado)

```
┌──────────────────────────────────────────────────┐
│                  ROLES POSIBLES                   │
├────────────┬─────────────────────────────────────┤
│  GUEST     │  Solo puede conectarse y ver si hay  │
│            │  sala disponible. No puede jugar.    │
├────────────┼─────────────────────────────────────┤
│  PLAYER    │  Puede unirse a partidas, jugar,     │
│            │  cantar STOP y ver puntuaciones.     │
├────────────┼─────────────────────────────────────┤
│  MODERATOR │  Todo lo anterior + expulsar         │
│            │  jugadores y cancelar rondas.        │
├────────────┼─────────────────────────────────────┤
│  ADMIN     │  Control total: gestión de salas,    │
│            │  usuarios y configuración global.    │
└────────────┴─────────────────────────────────────┘
```

Implementación sugerida:
- Añadir campo `role` al `Message` de tipo `JOIN`.
- `GameRoom` comprueba el rol antes de ejecutar `handleStop`, `removePlayer`, etc.
- Clave AES diferente por sala para aislar partidas entre sí.
