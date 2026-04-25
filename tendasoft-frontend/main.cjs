const { app, BrowserWindow, ipcMain } = require('electron');
const path = require('path');
const fs = require('fs');
const os = require('os');
const { exec } = require('child_process');

// Importamos la librería de la impresora térmica
const ThermalPrinter = require("node-thermal-printer").printer;
const PrinterTypes = require("node-thermal-printer").types;

console.log("1. Iniciando el motor de Electron...");

let mainWindow;

function createWindow() {
  // 1. Creamos la ventana con "show: false" para que nazca invisible
  mainWindow = new BrowserWindow({
    width: 1024,
    height: 768,
    show: false,           // Nace oculta
    autoHideMenuBar: true, // Oculta el menú superior (Archivo, Editar...) para que parezca más una app nativa
    icon: path.join(__dirname, '/src/logo-tendasoft.png'),
    webPreferences: {
      nodeIntegration: true,
      contextIsolation: false
    }
  });

  if (!app.isPackaged) {
    mainWindow.loadURL('http://localhost:5173');
  } else {
    mainWindow.loadFile(path.join(__dirname, 'dist/index.html'));
  }

  // 2. Cuando el HTML ya está cargado y listo, la maximizamos y la mostramos de golpe
  mainWindow.once('ready-to-show', () => {
    mainWindow.maximize(); // La hace grande respetando la barra de tareas
    mainWindow.show();     // La hace visible
  });
}

app.whenReady().then(createWindow);

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') app.quit();
});

// --- LÓGICA DE IMPRESORAS ---

ipcMain.handle('buscar-impresoras', async () => {
    try {
        const impresoras = await mainWindow.webContents.getPrintersAsync();
        return impresoras.map(p => ({ nombre: p.name, descripcion: p.displayName || p.name }));
    } catch (e) { return []; }
});

ipcMain.handle('imprimir-ticket', async (event, datosTicket, config) => {
    console.log(`Generando ticket para: ${config.nombreImpresora}`);

    try {
        let printer = new ThermalPrinter({
            type: config.ancho === '58mm' ? PrinterTypes.STAR : PrinterTypes.EPSON,
            interface: 'tcp://127.0.0.1:9100', // Dummy para generar el buffer local
            characterSet: 'PC858_EURO',
        });

        // --- DISEÑO DEL TICKET ---
        printer.alignCenter();
        printer.bold(true);
        printer.setTextQuadArea();
        printer.println(datosTicket.nombreEmpresa || "TendaSoft");
        printer.setTextNormal();
        printer.bold(false);
        printer.println("CIF: " + (datosTicket.cif || "---"));
        printer.drawLine();

        datosTicket.lineas.forEach(linea => {
            printer.tableCustom([
                { text: `${linea.cantidad}x ${linea.concepto}`, align: "LEFT", width: 0.7 },
                { text: `${linea.precio}€`, align: "RIGHT", width: 0.3 }
            ]);
        });

        printer.drawLine();
        printer.alignRight();
        printer.bold(true);
        printer.setTextQuadArea();
        printer.println(`TOTAL: ${datosTicket.total}€`);
        printer.setTextNormal();
        printer.bold(false);

        // --- CÓDIGO QR DE VERIFACTU ---
        if (datosTicket.urlVerifactu) {
            printer.drawLine();
            printer.alignCenter();
            printer.println("Sistema VeriFactu - Escanee para verificar:");
            printer.newLine();
            printer.printQR(datosTicket.urlVerifactu, {
                cellSize: 5,
                correction: 'M',
                model: 2
            });
            printer.newLine();
        }

        // Cortar el papel
        printer.cut();

        // 1. Guardamos los comandos RAW (ESC/POS) en un archivo temporal
        const tempPath = path.join(os.tmpdir(), `ticket_${Date.now()}.bin`);
        fs.writeFileSync(tempPath, printer.getBuffer());

        // 2. ENVIAR A IMPRESORA VIA POWERSHELL
        const psCommand = `powershell -Command "Get-Content -Path '${tempPath}' -Raw | Out-Printer -Name '${config.nombreImpresora}'"`;

        exec(psCommand, (error) => {
            if (error) {
                console.error("Error en PowerShell:", error);
            } else {
                console.log("Ticket enviado correctamente a la cola de impresión.");
                if (fs.existsSync(tempPath)) fs.unlinkSync(tempPath);
            }
        });

        printer.clear();
        return { success: true };

    } catch (error) {
        console.error("Error general:", error);
        return { success: false, error: error.message };
    }
});