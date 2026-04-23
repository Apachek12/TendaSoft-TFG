const { contextBridge, ipcRenderer } = require('electron');

// Exponemos una API segura al mundo de React
contextBridge.exposeInMainWorld('impresoraAPI', {
    buscarImpresoras: () => ipcRenderer.invoke('buscar-impresoras'),
    imprimirTicket: (datosTicket, configuracion) => ipcRenderer.invoke('imprimir-ticket', datosTicket, configuracion)
});