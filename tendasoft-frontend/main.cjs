const { app, BrowserWindow, ipcMain } = require('electron'); // Añade ipcMain
const path = require('path');
const isDev = require('electron-is-dev');

let mainWindow;

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1200,
    height: 800,
    webPreferences: {
      nodeIntegration: false, // Por seguridad debe ser false
      contextIsolation: true, // Debe ser true
      preload: path.join(__dirname, 'preload.cjs') // <-- EL PUENTE QUE CREAMOS
    },
  });

  if (isDev) {
    mainWindow.loadURL('http://localhost:5173');
    mainWindow.webContents.openDevTools();
  } else {
    mainWindow.loadFile(path.join(__dirname, 'dist/index.html'));
  }
}

// ... (tus app.whenReady() y app.on('window-all-closed') se quedan igual) ...

// --- LÓGICA DE IMPRESORAS (NUEVO) ---
ipcMain.handle('buscar-impresoras', async (event) => {
    try {
        // Le pedimos a Windows/Mac la lista de TODAS sus impresoras
        const impresorasDelSistema = await mainWindow.webContents.getPrintersAsync();

        // Devolvemos a React una lista limpia
        return impresorasDelSistema.map(p => ({
            nombre: p.name,
            descripcion: p.displayName || p.name,
            estado: p.status
        }));
    } catch (error) {
        console.error("Error buscando impresoras:", error);
        return [];
    }
});

// El esqueleto para cuando vayas a imprimir (lo llenaremos más adelante)
ipcMain.handle('imprimir-ticket', async (event, datos, config) => {
    console.log("Imprimiendo ticket en:", config.nombre);
    return { success: true };
});