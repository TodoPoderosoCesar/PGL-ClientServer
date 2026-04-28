# 🎯 PGL - Juego STOP Cliente-Servidor

## 📌 Descripción general

Este proyecto implementa un juego multijugador tipo **STOP** utilizando una arquitectura **cliente-servidor** en Java.

El sistema permite que varios jugadores se conecten a un servidor central, participen en rondas del juego y compitan obteniendo puntos en función de la originalidad y validez de sus respuestas.

---

## 🏗️ Arquitectura del sistema

El proyecto está dividido en tres módulos principales:
PGL-ClientServer/
│
├── client/ → Lógica del cliente (jugador)
├── server/ → Lógica del servidor (control del juego)
└── common/ → Clases compartidas (comunicación)


---

## 🖥️ Servidor

### 📌 Función principal
El servidor es el **núcleo del sistema**. Se encarga de gestionar jugadores, controlar el flujo del juego y garantizar la coherencia de la partida.

### ⚙️ Responsabilidades

- Aceptar conexiones de múltiples clientes mediante sockets
- Crear un hilo (`ClientHandler`) por cada jugador
- Gestionar el estado del juego:
    - Espera de jugadores
    - Inicio de ronda
    - Procesamiento de respuestas
    - Fin del juego
- Generar la letra de cada ronda
- Recibir respuestas de los jugadores
- Validar respuestas
- Calcular puntuaciones
- Enviar resultados a todos los jugadores

### 🔄 Estados del juego

- `WAITING` → Esperando jugadores
- `IN_GAME` → Ronda activa
- `PROCESSING` → Validando respuestas
- `FINISHED` → Juego terminado

### 🧠 Características técnicas

- Uso de `ExecutorService` para manejar múltiples clientes
- Comunicación mediante `ObjectInputStream` y `ObjectOutputStream`
- Estructuras concurrentes (`CopyOnWriteArrayList`)
- Sistema de difusión (`broadcast`) para enviar mensajes a todos los clientes

---

## 💻 Cliente

### 📌 Función principal
El cliente representa a un jugador. Su función es interactuar con el usuario y comunicarse con el servidor.

### ⚙️ Responsabilidades

- Conectarse al servidor
- Enviar acciones del jugador:
    - Nombre
    - Respuestas
    - STOP
- Recibir eventos del servidor:
    - Inicio de ronda
    - Letra asignada
    - Resultados
    - Fin del juego
- Mostrar la interfaz por consola
- Gestionar la entrada del usuario

### 🧵 Multihilo

El cliente utiliza varios hilos para:
- Escuchar constantemente al servidor (`ServerListener`)
- Leer entrada del usuario sin bloquear el programa

---

## 🔁 Comunicación

La comunicación se realiza mediante objetos serializados usando la clase `Message`.

### 📦 Tipos de mensajes (`MessageType`)

- `JOIN` → Un jugador se conecta
- `JOINED_OK` → Confirmación del servidor
- `START_ROUND` → Inicio de ronda
- `SUBMIT_ANSWERS` → Envío de respuestas
- `STOP_CALLED` → Un jugador ha dicho STOP
- `ROUND_RESULT` → Resultados de la ronda
- `GAME_OVER` → Fin de la partida

---

## 🧩 Lógica del juego

### 🎮 Mecánica

1. Los jugadores se conectan al servidor
2. El servidor inicia una ronda con una letra aleatoria
3. Los jugadores deben completar categorías:
    - Nombre
    - Animal
    - Color
    - País
    - Cosa
4. Un jugador puede pulsar **STOP**
5. Se recogen todas las respuestas
6. El servidor valida y puntúa
7. Se muestran resultados

---

## 🧮 Sistema de puntuación

- ✅ 10 puntos → Respuesta válida y única
- ⚠️ 5 puntos → Respuesta repetida
- ❌ 0 puntos → Respuesta inválida o incorrecta

### ✔️ Validación

- La palabra debe comenzar por la letra asignada
- No puede estar vacía
- Se compara con el resto de jugadores

---

## 📊 Gestión de datos

### 📁 Clases principales

- `Message` → Comunicación entre cliente y servidor
- `PlayerAnswers` → Respuestas de un jugador
- `PlayerScore` → Puntuación acumulada
- `GameRoom` → Estado global del juego
- `GameValidator` → Validación y puntuación

---

## 🎨 Interfaz de usuario

El cliente utiliza una interfaz por consola con:
- Mensajes estructurados
- Uso de caracteres Unicode
- Indicadores visuales (iconos y separadores)

---

## 🔐 Concurrencia

El sistema está diseñado para manejar múltiples jugadores simultáneamente:

- Un hilo por cliente en el servidor
- Acceso seguro a estructuras compartidas
- Sincronización implícita mediante colecciones concurrentes

---