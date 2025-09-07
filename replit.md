# HyserCore - Plugin Completo para Spigot 1.8.8

## Resumen del Proyecto

HyserCore es un plugin completo de Minecraft desarrollado para Spigot 1.8.8 que integra múltiples sistemas avanzados:

- **ChatGames**: Sistema de minijuegos en el chat con recompensas
- **Sword Enchantments**: Encantamientos personalizados para espadas con efectos únicos
- **LunarWaypoints**: Sistema de waypoints integrado con UltimateClans tipo F3 rally
- **LunarTeamViewer**: Visor de miembros del clan con separación por mundos
- **Anti-AutoArmor**: Sistema mejorado de detección de auto-armor
- **PrisonPunch**: Sistema de punch para prisión con configuración de AzuriteSpigot

## Cambios y Mejoras Implementadas

### 📁 Estructura del Proyecto
- **COMPLETADO**: Reorganización completa de archivos YML en `src/main/resources/`
- **COMPLETADO**: Estructura de proyecto Maven estándar con paquetes organizados
- **COMPLETADO**: Configuración de compilación automática con workflow

### ❄️ IceAspect Mejorado
- **NUEVO**: Evita colocar bloques de hielo en el piso del jugador
- **NUEVO**: Altura mínima de jaula configurable para evitar bloqueo completo
- **NUEVO**: Creación de huecos de aire para escapar
- **NUEVO**: No reemplaza bloques sólidos importantes
- **MEJORADO**: Radio y duración optimizados para mejor balance PvP

### 🛡️ Sistema Anti-AutoArmor Mejorado
- **NUEVO**: Detección de cambios extremadamente rápidos (150ms)
- **NUEVO**: Ventana de tiempo más corta para mayor precisión (200ms)
- **NUEVO**: Ignora modo creativo para evitar falsos positivos
- **NUEVO**: Ignora re-equipar la misma armadura
- **MEJORADO**: Seguimiento detallado de patrones de equipamiento

### 🏛️ Integración UltimateClans Corregida
- **CORREGIDO**: Múltiples métodos de detección de clanes
- **NUEVO**: Fallback automático si falla el primer método
- **MEJORADO**: Logs detallados para debugging
- **NUEVO**: Compatibilidad con diferentes versiones de UltimateClans

### 📍 LunarWaypoints Tipo Rally
- **NUEVO**: Actualizaciones automáticas cada 3 segundos tipo F3
- **NUEVO**: Separación estricta por mundos (solo muestra del mismo mundo)
- **NUEVO**: Sistema de timestamp para waypoints
- **MEJORADO**: Interfaz visual mejorada con mejor formato
- **NUEVO**: Actualización silenciosa en tiempo real

### 👥 LunarTeamViewer con Separación por Mundos
- **NUEVO**: Sistema completamente nuevo de visualización de clan
- **CRÍTICO**: Solo muestra jugadores del mismo mundo
- **NUEVO**: Actualizaciones automáticas cada 5 segundos
- **NUEVO**: Interfaz dedicada con información de distancia y dirección
- **NUEVO**: Toggle individual por jugador

### 📚 Sistema de Libros de Encantamiento Corregido
- **CORREGIDO**: Los libros ahora aplican efectos correctamente
- **NUEVO**: Detección automática de espadas en el inventario
- **NUEVO**: Tasa de éxito configurable para libros
- **NUEVO**: Consumo automático de libros después del uso
- **MEJORADO**: Validación completa de libros de encantamiento

### 🥊 Sistema PrisonPunch (NUEVO)
- **NUEVO**: Configuración exacta de AzuriteSpigot para knockback realista
- **NUEVO**: Modo boost tipo flecha en lugar de hit tradicional
- **NUEVO**: Configuración simple y avanzada de knockback personalizable
- **NUEVO**: Sistema de delay entre hits para prevenir spam
- **NUEVO**: Soporte para zonas seguras y de minería
- **NUEVO**: Partículas y sonidos personalizables
- **NUEVO**: Comandos completos de gestión y configuración

## Estructura Técnica

### Comandos Disponibles
- `/chatgames [reload|start|stop|status|stats]` - Gestión de ChatGames
- `/swordenchant <tipo>` - Aplicar encantamientos a espadas
- `/enchantbook <tipo>` - Crear libros de encantamiento
- `/waypoints [set|list|remove]` - Gestión de waypoints de clan
- `/teamviewer [on|off|list]` - Control del teamviewer lunar
- `/prisonpunch [reload|enable|disable|status|test|config]` - Sistema de punch para prisión

### Archivos de Configuración
- `config.yml` - Configuración principal del plugin
- `chatgames.yml` - Configuración completa de ChatGames
- `swordenchants.yml` - Configuración de encantamientos y anti-autoarmor
- `prisonpunch.yml` - Configuración de sistema de punch con valores de AzuriteSpigot

### Permisos
- `hysercore.admin` - Acceso completo a todos los comandos
- `hysercore.participate` - Participar en ChatGames
- `hysercore.swordenchant` - Usar encantamientos de espada
- `hysercore.waypoints` - Usar waypoints de clan
- `hysercore.teamviewer` - Usar teamviewer lunar
- `hysercore.prisonpunch` - Gestionar sistema de punch para prisión

## Compatibilidad

- **Spigot**: 1.8.8 (optimizado)
- **Java**: 8+
- **Dependencias Opcionales**: UltimateClans (para waypoints y teamviewer)
- **Maven**: 3.6+

## Instalación y Uso

1. Coloca el archivo `hysercore-1.0.0.jar` en la carpeta `plugins/` de tu servidor
2. Reinicia el servidor para generar archivos de configuración
3. Configura `UltimateClans` si deseas usar waypoints y teamviewer
4. Ajusta configuraciones en los archivos YML según tus necesidades
5. Recarga el plugin con `/chatgames reload`

## Características Únicas

- **Separación por Mundos**: Los sistemas no muestran información entre mundos diferentes
- **Integración Inteligente**: Múltiples métodos de detección para UltimateClans
- **Anti-Griefing**: IceAspect no puede griefear estructuras importantes
- **Rendimiento Optimizado**: Actualizaciones eficientes y código optimizado para 1.8.8

---

*Plugin desarrollado con arquitectura modular y código limpio para máximo rendimiento en Minecraft 1.8.8*