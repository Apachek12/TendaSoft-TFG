# TendaSoft TPV

Sistema de Punto de Venta con integración VeriFactu (AEAT).

## Requisitos

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) instalado y en ejecución
- [Node.js](https://nodejs.org/) v18 o superior

## Arranque

**1. Backend + Base de datos**

Desde la raíz del proyecto:

```bash
docker-compose up --build
```

**2. Frontend**

Desde la carpeta `tendasoft-frontend`:

```bash
npm install
npm run electron:dev
```

## Credenciales

| Usuario    | Contraseña | Rol      |
|------------|------------|----------|
| `admin`    | `1234`     | Admin    |
| `vendedor` | `1234`     | Vendedor |

**Base de datos MySQL**
- Host: `localhost:3306`
- Base de datos: `tendasoft`
- Usuario: `root` / Contraseña: `root`

**Certificado VeriFactu**
- Archivo: `Certificado_RPJ_A39200019_CERTIFICADO_ENTIDAD_PRUEBAS_5_Pre.p12`
- Contraseña: `1234`
- Es un certificado de **pruebas** de la AEAT (pre-producción)
- Se configura desde la app en **Configuración del Negocio**

## Datos iniciales

Al arrancar por primera vez, la aplicación carga automáticamente:
- 2 categorías (Bebidas, Alimentación)
- 3 productos de ejemplo
- Usuarios admin y vendedor listos para usar